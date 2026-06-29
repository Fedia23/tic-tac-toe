package com.fyre.tictactoe.session.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fyre.tictactoe.session.client.GameEngineClient;
import com.fyre.tictactoe.session.dto.CreateSessionResponse;
import com.fyre.tictactoe.session.dto.GameStateResponse;
import com.fyre.tictactoe.session.dto.SessionDetailsResponse;
import com.fyre.tictactoe.session.dto.SimulateGameResponse;
import com.fyre.tictactoe.session.model.GameSession;
import com.fyre.tictactoe.session.repository.GameSessionRepository;
import com.fyre.tictactoe.session.repository.MoveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Game Session Controller Integration Tests")
class GameSessionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GameSessionRepository sessionRepository;

    @Autowired
    private MoveRepository moveRepository;

    @MockBean
    private GameEngineClient gameEngineClient;

    @BeforeEach
    void setUp() {
        moveRepository.deleteAll();
        sessionRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /sessions - Should create a new game session successfully")
    void createSession_Success() throws Exception {
        GameStateResponse mockGameState = GameStateResponse.builder()
            .gameId("game-123")
            .status("IN_PROGRESS")
            .currentPlayer("X")
            .board(Arrays.asList(" ", " ", " ", " ", " ", " ", " ", " ", " "))
            .build();

        when(gameEngineClient.createGame()).thenReturn(mockGameState);

        MvcResult result = mockMvc.perform(post("/sessions")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.sessionId").exists())
            .andExpect(jsonPath("$.gameId").value("game-123"))
            .andExpect(jsonPath("$.status").value("CREATED"))
            .andExpect(jsonPath("$.message").value("Game session created successfully"))
            .andReturn();

        verify(gameEngineClient, times(1)).createGame();

        String content = result.getResponse().getContentAsString();
        CreateSessionResponse response = objectMapper.readValue(content, CreateSessionResponse.class);

        List<GameSession> sessions = sessionRepository.findAll();
        assertThat(sessions).hasSize(1);
        assertThat(sessions.get(0).getGameId()).isEqualTo("game-123");
        assertThat(sessions.get(0).getId()).isEqualTo(response.getSessionId());
    }

    @Test
    @DisplayName("POST /sessions/{id}/simulate - Should simulate game until X wins")
    void simulateGame_XWins() throws Exception {
        String sessionId = createTestSession();

        GameStateResponse inProgressState = createGameState("game-123", "IN_PROGRESS", "X",
            Arrays.asList(" ", " ", " ", " ", " ", " ", " ", " ", " "), null);

        GameStateResponse afterMove1 = createGameState("game-123", "IN_PROGRESS", "O",
            Arrays.asList("X", " ", " ", " ", " ", " ", " ", " ", " "), null);

        GameStateResponse afterMove2 = createGameState("game-123", "IN_PROGRESS", "X",
            Arrays.asList("X", "O", " ", " ", " ", " ", " ", " ", " "), null);

        GameStateResponse afterMove3 = createGameState("game-123", "IN_PROGRESS", "O",
            Arrays.asList("X", "O", " ", " ", "X", " ", " ", " ", " "), null);

        GameStateResponse afterMove4 = createGameState("game-123", "IN_PROGRESS", "X",
            Arrays.asList("X", "O", " ", " ", "X", "O", " ", " ", " "), null);

        GameStateResponse finalState = createGameState("game-123", "COMPLETED", null,
            Arrays.asList("X", "O", " ", " ", "X", "O", " ", " ", "X"), "X");

        when(gameEngineClient.getGameState("game-123")).thenReturn(inProgressState);
        when(gameEngineClient.makeMove(eq("game-123"), any()))
            .thenReturn(afterMove1)
            .thenReturn(afterMove2)
            .thenReturn(afterMove3)
            .thenReturn(afterMove4)
            .thenReturn(finalState);

        mockMvc.perform(post("/sessions/{sessionId}/simulate", sessionId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sessionId").value(sessionId))
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.result").value("X_WINS"))
            .andExpect(jsonPath("$.totalMoves").value(5))
            .andExpect(jsonPath("$.message").value("Game simulation completed"));

        verify(gameEngineClient, times(1)).getGameState("game-123");
        verify(gameEngineClient, times(5)).makeMove(eq("game-123"), any());

        GameSession session = sessionRepository.findByIdWithMoves(sessionId).orElseThrow();
        assertThat(session.getStatus().name()).isEqualTo("COMPLETED");
        assertThat(session.getResult().name()).isEqualTo("X_WINS");
        assertThat(session.getMoves()).hasSize(5);
    }

    @Test
    @DisplayName("POST /sessions/{id}/simulate - Should simulate game until O wins")
    void simulateGame_OWins() throws Exception {
        String sessionId = createTestSession();

        GameStateResponse inProgressState = createGameState("game-123", "IN_PROGRESS", "X",
            Arrays.asList(" ", " ", " ", " ", " ", " ", " ", " ", " "), null);

        GameStateResponse finalState = createGameState("game-123", "COMPLETED", null,
            Arrays.asList("O", "O", "O", "X", "X", " ", " ", " ", " "), "O");

        when(gameEngineClient.getGameState("game-123")).thenReturn(inProgressState);
        when(gameEngineClient.makeMove(eq("game-123"), any()))
            .thenReturn(
                createGameState("game-123", "IN_PROGRESS", "O", Arrays.asList("X", " ", " ", " ", " ", " ", " ", " ", " "), null),
                createGameState("game-123", "IN_PROGRESS", "X", Arrays.asList("X", "O", " ", " ", " ", " ", " ", " ", " "), null),
                createGameState("game-123", "IN_PROGRESS", "O", Arrays.asList("X", "O", " ", "X", " ", " ", " ", " ", " "), null),
                createGameState("game-123", "IN_PROGRESS", "X", Arrays.asList("X", "O", "O", "X", " ", " ", " ", " ", " "), null),
                createGameState("game-123", "IN_PROGRESS", "O", Arrays.asList("X", "O", "O", "X", "X", " ", " ", " ", " "), null),
                finalState
            );

        mockMvc.perform(post("/sessions/{sessionId}/simulate", sessionId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value("O_WINS"));
    }

    @Test
    @DisplayName("POST /sessions/{id}/simulate - Should simulate game until draw")
    void simulateGame_Draw() throws Exception {
        String sessionId = createTestSession();

        GameStateResponse inProgressState = createGameState("game-123", "IN_PROGRESS", "X",
            Arrays.asList(" ", " ", " ", " ", " ", " ", " ", " ", " "), null);

        GameStateResponse finalState = createGameState("game-123", "COMPLETED", null,
            Arrays.asList("X", "O", "X", "O", "X", "O", "O", "X", "O"), "DRAW");

        when(gameEngineClient.getGameState("game-123")).thenReturn(inProgressState);
        when(gameEngineClient.makeMove(eq("game-123"), any()))
            .thenReturn(inProgressState, inProgressState, inProgressState, inProgressState,
                       inProgressState, inProgressState, inProgressState, inProgressState, finalState);

        mockMvc.perform(post("/sessions/{sessionId}/simulate", sessionId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value("DRAW"))
            .andExpect(jsonPath("$.totalMoves").value(9));
    }

    @Test
    @DisplayName("POST /sessions/{id}/simulate - Should return 404 when session not found")
    void simulateGame_SessionNotFound() throws Exception {
        String validUuid = "550e8400-e29b-41d4-a716-446655440000";
        mockMvc.perform(post("/sessions/{sessionId}/simulate", validUuid))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value(containsString("Session not found")));
    }

    @Test
    @DisplayName("POST /sessions/{id}/simulate - Should return 400 when game already completed")
    void simulateGame_AlreadyCompleted() throws Exception {
        String sessionId = createTestSession();

        GameStateResponse inProgressState = createGameState("game-123", "IN_PROGRESS", "X",
            Arrays.asList(" ", " ", " ", " ", " ", " ", " ", " ", " "), null);
        GameStateResponse completedState = createGameState("game-123", "COMPLETED", null,
            Arrays.asList("X", "O", " ", " ", "X", "O", " ", " ", "X"), "X");

        when(gameEngineClient.getGameState("game-123")).thenReturn(inProgressState);
        when(gameEngineClient.makeMove(eq("game-123"), any())).thenReturn(completedState);

        mockMvc.perform(post("/sessions/{sessionId}/simulate", sessionId))
            .andExpect(status().isOk());

        mockMvc.perform(post("/sessions/{sessionId}/simulate", sessionId))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("already completed")));
    }

    @Test
    @DisplayName("GET /sessions/{id} - Should retrieve session details successfully")
    void getSessionDetails_Success() throws Exception {
        String sessionId = createAndSimulateSession();

        mockMvc.perform(get("/sessions/{sessionId}", sessionId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sessionId").value(sessionId))
            .andExpect(jsonPath("$.gameId").value("game-123"))
            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.result").exists())
            .andExpect(jsonPath("$.currentBoard").exists())
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.moves").isArray())
            .andExpect(jsonPath("$.moves[0].moveNumber").exists())
            .andExpect(jsonPath("$.moves[0].player").exists())
            .andExpect(jsonPath("$.moves[0].position").exists())
            .andExpect(jsonPath("$.moves[0].timestamp").exists());
    }

    @Test
    @DisplayName("GET /sessions/{id} - Should return 404 when session not found")
    void getSessionDetails_NotFound() throws Exception {
        String validUuid = "550e8400-e29b-41d4-a716-446655440000";
        mockMvc.perform(get("/sessions/{sessionId}", validUuid))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value(containsString("Session not found")));
    }

    @Test
    @DisplayName("GET /sessions/{id} - Should return empty moves for new session")
    void getSessionDetails_EmptyMoves() throws Exception {
        String sessionId = createTestSession();

        mockMvc.perform(get("/sessions/{sessionId}", sessionId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.moves").isEmpty())
            .andExpect(jsonPath("$.status").value("CREATED"))
            .andExpect(jsonPath("$.result").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("Integration Test - Full workflow from creation to completion")
    void fullWorkflow_CreateSimulateAndRetrieve() throws Exception {
        GameStateResponse mockGameState = createGameState("game-456", "IN_PROGRESS", "X",
            Arrays.asList(" ", " ", " ", " ", " ", " ", " ", " ", " "), null);
        GameStateResponse completedState = createGameState("game-456", "COMPLETED", null,
            Arrays.asList("X", "X", "X", "O", "O", " ", " ", " ", " "), "X");

        when(gameEngineClient.createGame()).thenReturn(mockGameState);
        when(gameEngineClient.getGameState("game-456")).thenReturn(mockGameState);
        when(gameEngineClient.makeMove(eq("game-456"), any()))
            .thenReturn(mockGameState, mockGameState, completedState);

        MvcResult createResult = mockMvc.perform(post("/sessions"))
            .andExpect(status().isCreated())
            .andReturn();

        CreateSessionResponse createResponse = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            CreateSessionResponse.class
        );
        String sessionId = createResponse.getSessionId();

        mockMvc.perform(post("/sessions/{sessionId}/simulate", sessionId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.result").value("X_WINS"))
            .andExpect(jsonPath("$.totalMoves").value(3));

        MvcResult detailsResult = mockMvc.perform(get("/sessions/{sessionId}", sessionId))
            .andExpect(status().isOk())
            .andReturn();

        SessionDetailsResponse detailsResponse = objectMapper.readValue(
            detailsResult.getResponse().getContentAsString(),
            SessionDetailsResponse.class
        );

        assertThat(detailsResponse.getSessionId()).isEqualTo(sessionId);
        assertThat(detailsResponse.getGameId()).isEqualTo("game-456");
        assertThat(detailsResponse.getStatus()).isEqualTo("COMPLETED");
        assertThat(detailsResponse.getResult()).isEqualTo("X_WINS");
        assertThat(detailsResponse.getMoves()).hasSize(3);
        assertThat(detailsResponse.getCreatedAt()).isNotNull();
        assertThat(detailsResponse.getCompletedAt()).isNotNull();
    }

    private String createTestSession() throws Exception {
        GameStateResponse mockGameState = createGameState("game-123", "IN_PROGRESS", "X",
            Arrays.asList(" ", " ", " ", " ", " ", " ", " ", " ", " "), null);

        when(gameEngineClient.createGame()).thenReturn(mockGameState);

        MvcResult result = mockMvc.perform(post("/sessions"))
            .andExpect(status().isCreated())
            .andReturn();

        CreateSessionResponse response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            CreateSessionResponse.class
        );

        return response.getSessionId();
    }

    private String createAndSimulateSession() throws Exception {
        String sessionId = createTestSession();

        GameStateResponse inProgressState = createGameState("game-123", "IN_PROGRESS", "X",
            Arrays.asList(" ", " ", " ", " ", " ", " ", " ", " ", " "), null);
        GameStateResponse completedState = createGameState("game-123", "COMPLETED", null,
            Arrays.asList("X", "O", " ", " ", "X", "O", " ", " ", "X"), "X");

        when(gameEngineClient.getGameState("game-123")).thenReturn(inProgressState);
        when(gameEngineClient.makeMove(eq("game-123"), any()))
            .thenReturn(inProgressState, inProgressState, completedState);

        mockMvc.perform(post("/sessions/{sessionId}/simulate", sessionId))
            .andExpect(status().isOk());

        return sessionId;
    }

    private GameStateResponse createGameState(String gameId, String status, String currentPlayer,
                                              List<String> board, String winner) {
        return GameStateResponse.builder()
            .gameId(gameId)
            .status(status)
            .currentPlayer(currentPlayer)
            .board(board)
            .winner(winner)
            .build();
    }
}