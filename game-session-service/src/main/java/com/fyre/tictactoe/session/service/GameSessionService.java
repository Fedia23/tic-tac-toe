package com.fyre.tictactoe.session.service;

import com.fyre.tictactoe.session.client.GameEngineClient;
import com.fyre.tictactoe.session.dto.*;
import com.fyre.tictactoe.session.exception.SessionNotFoundException;
import com.fyre.tictactoe.session.model.*;
import com.fyre.tictactoe.session.repository.GameSessionRepository;
import com.fyre.tictactoe.session.simulator.GameSimulator;
import com.fyre.tictactoe.session.simulator.SimulationResult;
import com.fyre.tictactoe.session.util.BoardSerializer;
import com.fyre.tictactoe.session.util.GameResultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameSessionService {

    private final GameSessionRepository sessionRepository;
    private final GameEngineClient gameEngineClient;
    private final GameSimulator gameSimulator;
    private final BoardSerializer boardSerializer;
    private final GameResultMapper gameResultMapper;

    @Transactional
    public CreateSessionResponse createSession() {
        log.info("Creating new game session");

        GameStateResponse gameState = gameEngineClient.createGame();

        GameSession session = GameSession.builder()
            .gameId(gameState.getGameId())
            .status(GameStatus.CREATED)
            .result(GameResult.IN_PROGRESS)
            .createdAt(LocalDateTime.now())
            .currentBoard(boardSerializer.serialize(gameState.getBoard()))
            .currentPlayer(Player.valueOf(gameState.getCurrentPlayer()))
            .build();

        session = sessionRepository.save(session);
        log.info("Game session created with ID: {}", session.getId());

        return CreateSessionResponse.builder()
            .sessionId(session.getId())
            .gameId(session.getGameId())
            .status(session.getStatus().name())
            .message("Game session created successfully")
            .build();
    }

    @Transactional
    public SimulateGameResponse simulateGame(String sessionId) {
        log.info("Starting game simulation for session: {}", sessionId);

        GameSession session = getSessionForSimulation(sessionId);

        session.setStatus(GameStatus.IN_PROGRESS);
        sessionRepository.save(session);

        SimulationResult simulationResult = gameSimulator.simulate(session);

        updateSessionWithResult(session, simulationResult);

        log.info("Game simulation completed. Result: {}, Total moves: {}",
                session.getResult(), simulationResult.getTotalMoves());

        return SimulateGameResponse.builder()
            .sessionId(session.getId())
            .status(session.getStatus().name())
            .result(session.getResult().name())
            .totalMoves(simulationResult.getTotalMoves())
            .message("Game simulation completed")
            .build();
    }

    private GameSession getSessionForSimulation(String sessionId) {
        GameSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new SessionNotFoundException("Session not found: " + sessionId));

        if (session.getStatus() == GameStatus.COMPLETED) {
            throw new IllegalStateException("Game session is already completed");
        }

        return session;
    }

    private void updateSessionWithResult(GameSession session, SimulationResult simulationResult) {
        session.setStatus(GameStatus.COMPLETED);
        session.setCompletedAt(LocalDateTime.now());

        GameResult result = gameResultMapper.mapWinnerToResult(
                simulationResult.getFinalGameState().getWinner());
        session.setResult(result);

        sessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public SessionDetailsResponse getSessionDetails(String sessionId) {
        log.info("Retrieving session details for: {}", sessionId);

        GameSession session = sessionRepository.findByIdWithMoves(sessionId)
            .orElseThrow(() -> new SessionNotFoundException("Session not found: " + sessionId));

        List<SessionDetailsResponse.MoveDto> moveDtos = session.getMoves().stream()
            .map(move -> SessionDetailsResponse.MoveDto.builder()
                .moveNumber(move.getMoveNumber())
                .player(move.getPlayer().name())
                .position(move.getPosition())
                .timestamp(move.getTimestamp())
                .build())
            .collect(Collectors.toList());

        return SessionDetailsResponse.builder()
            .sessionId(session.getId())
            .gameId(session.getGameId())
            .status(session.getStatus().name())
            .result(session.getResult().name())
            .currentBoard(session.getCurrentBoard())
            .currentPlayer(session.getCurrentPlayer() != null ? session.getCurrentPlayer().name() : null)
            .createdAt(session.getCreatedAt())
            .completedAt(session.getCompletedAt())
            .moves(moveDtos)
            .build();
    }

    @Transactional(readOnly = true)
    public Page<SessionSummaryResponse> getAllSessions(Pageable pageable) {
        log.info("Retrieving all sessions with pagination: {}", pageable);

        return sessionRepository.findAllByOrderByCreatedAtDesc(pageable)
            .map(this::mapToSummaryResponse);
    }

    @Transactional(readOnly = true)
    public Page<SessionSummaryResponse> getSessionsByStatus(GameStatus status, Pageable pageable) {
        log.info("Retrieving sessions with status {} and pagination: {}", status, pageable);

        return sessionRepository.findByStatus(status, pageable)
            .map(this::mapToSummaryResponse);
    }

    private SessionSummaryResponse mapToSummaryResponse(GameSession session) {
        return SessionSummaryResponse.builder()
            .sessionId(session.getId())
            .gameId(session.getGameId())
            .status(session.getStatus().name())
            .result(session.getResult().name())
            .createdAt(session.getCreatedAt())
            .completedAt(session.getCompletedAt())
            .totalMoves(session.getMoves().size())
            .build();
    }
}