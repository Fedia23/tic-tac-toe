package com.fyre.tictactoe.session.service;

import com.fyre.tictactoe.session.client.GameEngineClient;
import com.fyre.tictactoe.session.dto.CreateSessionResponse;
import com.fyre.tictactoe.session.dto.GameStateResponse;
import com.fyre.tictactoe.session.dto.SessionDetailsResponse;
import com.fyre.tictactoe.session.dto.SimulateGameResponse;
import com.fyre.tictactoe.session.exception.SessionNotFoundException;
import com.fyre.tictactoe.session.model.GameResult;
import com.fyre.tictactoe.session.model.GameSession;
import com.fyre.tictactoe.session.model.GameStatus;
import com.fyre.tictactoe.session.model.Player;
import com.fyre.tictactoe.session.repository.GameSessionRepository;
import com.fyre.tictactoe.session.simulator.GameSimulator;
import com.fyre.tictactoe.session.simulator.SimulationResult;
import com.fyre.tictactoe.session.util.BoardSerializer;
import com.fyre.tictactoe.session.util.GameResultMapper;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("Game Session Service Tests")
class GameSessionServiceTest {

    @Mock
    private GameSessionRepository sessionRepository;

    @Mock
    private GameEngineClient gameEngineClient;

    @Mock
    private GameSimulator gameSimulator;

    @Mock
    private BoardSerializer boardSerializer;

    @Mock
    private GameResultMapper gameResultMapper;

    @InjectMocks
    private GameSessionService gameSessionService;

    private GameStateResponse mockGameState;

    @BeforeEach
    void setUp() {
        mockGameState = GameStateResponse.builder()
            .gameId("game-123")
            .status("IN_PROGRESS")
            .currentPlayer("X")
            .board(Arrays.asList(" ", " ", " ", " ", " ", " ", " ", " ", " "))
            .build();

        lenient().when(boardSerializer.serialize(any())).thenReturn(" , , , , , , , , ");
    }

    @Test
    @DisplayName("createSession - Should create session successfully")
    void createSession_Success() {
        when(gameEngineClient.createGame()).thenReturn(mockGameState);
        when(sessionRepository.save(any(GameSession.class))).thenAnswer(invocation -> {
            GameSession session = invocation.getArgument(0);
            session.setId("session-123");
            return session;
        });

        CreateSessionResponse response = gameSessionService.createSession();

        assertThat(response).isNotNull();
        assertThat(response.getSessionId()).isEqualTo("session-123");
        assertThat(response.getGameId()).isEqualTo("game-123");
        assertThat(response.getStatus()).isEqualTo("CREATED");
        assertThat(response.getMessage()).contains("successfully");

        verify(gameEngineClient, times(1)).createGame();
        verify(sessionRepository, times(1)).save(any(GameSession.class));
    }

    @Test
    @DisplayName("createSession - Should handle Game Engine Service failure")
    void createSession_GameEngineFailure() {
        Request request = Request.create(Request.HttpMethod.POST, "/games",
            Collections.emptyMap(), null, StandardCharsets.UTF_8, new RequestTemplate());

        when(gameEngineClient.createGame())
            .thenThrow(new FeignException.ServiceUnavailable("Service unavailable", request, null, null));

        assertThatThrownBy(() -> gameSessionService.createSession())
            .isInstanceOf(FeignException.class);

        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("simulateGame - Should complete game with X winning")
    void simulateGame_XWins() {
        GameSession existingSession = createMockSession("session-123", "game-123");

        GameStateResponse finalState = createGameState("COMPLETED", null,
            Arrays.asList("X", "O", " ", " ", "X", "O", " ", " ", "X"));
        finalState.setWinner("X");

        SimulationResult simulationResult = SimulationResult.builder()
            .finalGameState(finalState)
            .totalMoves(3)
            .build();

        when(sessionRepository.findById("session-123")).thenReturn(Optional.of(existingSession));
        when(sessionRepository.save(any(GameSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(gameSimulator.simulate(any(GameSession.class))).thenReturn(simulationResult);
        when(gameResultMapper.mapWinnerToResult("X")).thenReturn(GameResult.X_WINS);

        SimulateGameResponse response = gameSessionService.simulateGame("session-123");

        assertThat(response).isNotNull();
        assertThat(response.getSessionId()).isEqualTo("session-123");
        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getResult()).isEqualTo("X_WINS");
        assertThat(response.getTotalMoves()).isEqualTo(3);

        verify(gameSimulator, times(1)).simulate(any(GameSession.class));
        verify(gameResultMapper, times(1)).mapWinnerToResult("X");
    }

    @Test
    @DisplayName("simulateGame - Should complete game with O winning")
    void simulateGame_OWins() {
        GameSession existingSession = createMockSession("session-123", "game-123");

        GameStateResponse finalState = createGameState("COMPLETED", null,
            Arrays.asList("O", "O", "O", "X", "X", " ", " ", " ", " "));
        finalState.setWinner("O");

        SimulationResult simulationResult = SimulationResult.builder()
            .finalGameState(finalState)
            .totalMoves(5)
            .build();

        when(sessionRepository.findById("session-123")).thenReturn(Optional.of(existingSession));
        when(sessionRepository.save(any(GameSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(gameSimulator.simulate(any(GameSession.class))).thenReturn(simulationResult);
        when(gameResultMapper.mapWinnerToResult("O")).thenReturn(GameResult.O_WINS);

        SimulateGameResponse response = gameSessionService.simulateGame("session-123");

        assertThat(response.getResult()).isEqualTo("O_WINS");
    }

    @Test
    @DisplayName("simulateGame - Should complete game with draw")
    void simulateGame_Draw() {
        GameSession existingSession = createMockSession("session-123", "game-123");

        GameStateResponse finalState = createGameState("COMPLETED", null,
            Arrays.asList("X", "O", "X", "O", "X", "O", "O", "X", "O"));
        finalState.setWinner("DRAW");

        SimulationResult simulationResult = SimulationResult.builder()
            .finalGameState(finalState)
            .totalMoves(9)
            .build();

        when(sessionRepository.findById("session-123")).thenReturn(Optional.of(existingSession));
        when(sessionRepository.save(any(GameSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(gameSimulator.simulate(any(GameSession.class))).thenReturn(simulationResult);
        when(gameResultMapper.mapWinnerToResult("DRAW")).thenReturn(GameResult.DRAW);

        SimulateGameResponse response = gameSessionService.simulateGame("session-123");

        assertThat(response.getResult()).isEqualTo("DRAW");
    }

    @Test
    @DisplayName("simulateGame - Should throw exception when session not found")
    void simulateGame_SessionNotFound() {
        when(sessionRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameSessionService.simulateGame("non-existent"))
            .isInstanceOf(SessionNotFoundException.class)
            .hasMessageContaining("Session not found");

        verify(gameSimulator, never()).simulate(any());
    }

    @Test
    @DisplayName("simulateGame - Should throw exception when game already completed")
    void simulateGame_AlreadyCompleted() {
        GameSession completedSession = createMockSession("session-123", "game-123");
        completedSession.setStatus(GameStatus.COMPLETED);
        completedSession.setResult(GameResult.X_WINS);

        when(sessionRepository.findById("session-123")).thenReturn(Optional.of(completedSession));

        assertThatThrownBy(() -> gameSessionService.simulateGame("session-123"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("already completed");

        verify(gameSimulator, never()).simulate(any());
    }

    @Test
    @DisplayName("simulateGame - Should update session status correctly")
    void simulateGame_UpdatesSessionStatus() {
        GameSession existingSession = createMockSession("session-123", "game-123");

        GameStateResponse finalState = createGameState("COMPLETED", null,
            Arrays.asList("X", "O", " ", " ", "X", "O", " ", " ", "X"));
        finalState.setWinner("X");

        SimulationResult simulationResult = SimulationResult.builder()
            .finalGameState(finalState)
            .totalMoves(5)
            .build();

        when(sessionRepository.findById("session-123")).thenReturn(Optional.of(existingSession));
        when(sessionRepository.save(any(GameSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(gameSimulator.simulate(any(GameSession.class))).thenReturn(simulationResult);
        when(gameResultMapper.mapWinnerToResult("X")).thenReturn(GameResult.X_WINS);

        gameSessionService.simulateGame("session-123");

        verify(sessionRepository, atLeast(2)).save(any(GameSession.class));

        assertThat(existingSession.getStatus()).isEqualTo(GameStatus.COMPLETED);
        assertThat(existingSession.getResult()).isEqualTo(GameResult.X_WINS);
        assertThat(existingSession.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("getSessionDetails - Should return session with moves")
    void getSessionDetails_Success() {
        GameSession session = createMockSession("session-123", "game-123");
        session.setStatus(GameStatus.COMPLETED);
        session.setResult(GameResult.X_WINS);
        session.setCompletedAt(LocalDateTime.now());

        when(sessionRepository.findByIdWithMoves("session-123")).thenReturn(Optional.of(session));

        SessionDetailsResponse response = gameSessionService.getSessionDetails("session-123");

        assertThat(response).isNotNull();
        assertThat(response.getSessionId()).isEqualTo("session-123");
        assertThat(response.getGameId()).isEqualTo("game-123");
        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getResult()).isEqualTo("X_WINS");
        assertThat(response.getCurrentBoard()).isNotNull();
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getCompletedAt()).isNotNull();
        assertThat(response.getMoves()).isNotNull();
    }

    @Test
    @DisplayName("getSessionDetails - Should throw exception when session not found")
    void getSessionDetails_SessionNotFound() {
        when(sessionRepository.findByIdWithMoves("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameSessionService.getSessionDetails("non-existent"))
            .isInstanceOf(SessionNotFoundException.class)
            .hasMessageContaining("Session not found");
    }

    private GameSession createMockSession(String sessionId, String gameId) {
        return GameSession.builder()
            .id(sessionId)
            .gameId(gameId)
            .status(GameStatus.CREATED)
            .result(GameResult.IN_PROGRESS)
            .createdAt(LocalDateTime.now())
            .currentBoard(" , , , , , , , , ")
            .currentPlayer(Player.X)
            .moves(new ArrayList<>())
            .build();
    }

    private GameStateResponse createGameState(String status, String currentPlayer, List<String> board) {
        return GameStateResponse.builder()
            .gameId("game-123")
            .status(status)
            .currentPlayer(currentPlayer)
            .board(board)
            .build();
    }
}