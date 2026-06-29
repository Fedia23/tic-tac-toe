package com.fyre.tictactoe.session.simulator;

import com.fyre.tictactoe.session.client.GameEngineClient;
import com.fyre.tictactoe.session.dto.GameStateResponse;
import com.fyre.tictactoe.session.dto.MoveRequest;
import com.fyre.tictactoe.session.model.GameSession;
import com.fyre.tictactoe.session.model.Move;
import com.fyre.tictactoe.session.model.Player;
import com.fyre.tictactoe.session.repository.MoveRepository;
import com.fyre.tictactoe.session.service.MoveGenerationService;
import com.fyre.tictactoe.session.util.BoardSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class GameSimulator {

    private final GameEngineClient gameEngineClient;
    private final MoveGenerationService moveGenerationService;
    private final MoveRepository moveRepository;
    private final BoardSerializer boardSerializer;

    public SimulationResult simulate(GameSession session) {
        log.info("Starting game simulation for session: {}", session.getId());

        int moveCount = 0;
        GameStateResponse gameState = gameEngineClient.getGameState(session.getGameId());

        while ("IN_PROGRESS".equals(gameState.getStatus())) {
            moveCount++;
            log.info("Move {}: Player {}'s turn", moveCount, gameState.getCurrentPlayer());

            gameState = executeMove(session, gameState, moveCount);
        }

        log.info("Game simulation completed. Status: {}, Winner: {}, Total moves: {}",
                gameState.getStatus(), gameState.getWinner(), moveCount);

        return SimulationResult.builder()
                .finalGameState(gameState)
                .totalMoves(moveCount)
                .build();
    }

    private GameStateResponse executeMove(GameSession session, GameStateResponse gameState, int moveCount) {
        Integer position = moveGenerationService.generateRandomMove(gameState.getBoard());

        MoveRequest moveRequest = MoveRequest.builder()
                .position(position)
                .player(gameState.getCurrentPlayer())
                .build();

        GameStateResponse newGameState = gameEngineClient.makeMove(session.getGameId(), moveRequest);

        recordMove(session, moveCount, moveRequest.getPlayer(), position);
        updateSessionBoard(session, newGameState);

        return newGameState;
    }

    private void recordMove(GameSession session, int moveCount, String player, Integer position) {
        Move move = Move.builder()
                .session(session)
                .moveNumber(moveCount)
                .player(Player.valueOf(player))
                .position(position)
                .timestamp(LocalDateTime.now())
                .build();

        moveRepository.save(move);
    }

    private void updateSessionBoard(GameSession session, GameStateResponse gameState) {
        session.setCurrentBoard(boardSerializer.serialize(gameState.getBoard()));
        if (gameState.getCurrentPlayer() != null) {
            session.setCurrentPlayer(Player.valueOf(gameState.getCurrentPlayer()));
        }
    }
}