package com.fyre.tictactoe.session.controller;

import com.fyre.tictactoe.session.dto.CreateSessionResponse;
import com.fyre.tictactoe.session.dto.SessionDetailsResponse;
import com.fyre.tictactoe.session.dto.SessionSummaryResponse;
import com.fyre.tictactoe.session.dto.SimulateGameResponse;
import com.fyre.tictactoe.session.model.GameStatus;
import com.fyre.tictactoe.session.service.GameSessionService;
import com.fyre.tictactoe.session.validation.UUIDConstraint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Game Session Management", description = "APIs for managing tic-tac-toe game sessions")
public class GameSessionController {

    private final GameSessionService gameSessionService;

    @Operation(
        summary = "Create a new game session",
        description = "Creates a new tic-tac-toe game session and initializes the game state in the Game Engine Service"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Game session created successfully",
            content = @Content(schema = @Schema(implementation = CreateSessionResponse.class))
        ),
        @ApiResponse(
            responseCode = "503",
            description = "Game Engine Service unavailable"
        )
    })
    @PostMapping
    public ResponseEntity<CreateSessionResponse> createSession() {
        log.info("POST /sessions - Creating new game session");
        CreateSessionResponse response = gameSessionService.createSession();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Simulate an automated game",
        description = "Automatically generates and executes random moves for both players until the game concludes (win or draw)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Game simulation completed successfully",
            content = @Content(schema = @Schema(implementation = SimulateGameResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Session not found"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Game session is already completed"
        )
    })
    @PostMapping("/{sessionId}/simulate")
    public ResponseEntity<SimulateGameResponse> simulateGame(
            @Parameter(description = "Unique session identifier (UUID)", required = true)
            @UUIDConstraint
            @PathVariable String sessionId) {
        log.info("POST /sessions/{}/simulate - Starting game simulation", sessionId);
        SimulateGameResponse response = gameSessionService.simulateGame(sessionId);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get session details",
        description = "Retrieves complete session information including game state, result, and full move history"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Session details retrieved successfully",
            content = @Content(schema = @Schema(implementation = SessionDetailsResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Session not found"
        )
    })
    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionDetailsResponse> getSessionDetails(
            @Parameter(description = "Unique session identifier (UUID)", required = true)
            @UUIDConstraint
            @PathVariable String sessionId) {
        log.info("GET /sessions/{} - Retrieving session details", sessionId);
        SessionDetailsResponse response = gameSessionService.getSessionDetails(sessionId);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get all game sessions",
        description = "Retrieves a paginated list of all game sessions, optionally filtered by status"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Sessions retrieved successfully",
            content = @Content(schema = @Schema(implementation = Page.class))
        )
    })
    @GetMapping
    public ResponseEntity<Page<SessionSummaryResponse>> getAllSessions(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            @PageableDefault(size = 20, sort = "createdAt")
            @Valid Pageable pageable,
            @Parameter(description = "Filter by session status (CREATED, IN_PROGRESS, COMPLETED)")
            @RequestParam(required = false) GameStatus status) {
        log.info("GET /sessions - Retrieving all sessions with pagination");

        Page<SessionSummaryResponse> response = status != null
            ? gameSessionService.getSessionsByStatus(status, pageable)
            : gameSessionService.getAllSessions(pageable);

        return ResponseEntity.ok(response);
    }
}