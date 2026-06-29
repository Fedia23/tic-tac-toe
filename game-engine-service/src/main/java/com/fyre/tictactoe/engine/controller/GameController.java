package com.fyre.tictactoe.engine.controller;

import com.fyre.tictactoe.engine.dto.GameStateResponse;
import com.fyre.tictactoe.engine.dto.MoveRequest;
import com.fyre.tictactoe.engine.dto.MoveResponse;
import com.fyre.tictactoe.engine.exception.ErrorResponse;
import com.fyre.tictactoe.engine.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Game Management", description = "APIs for managing Tic Tac Toe games")
public class GameController {

    private final GameService gameService;

    @Operation(
            summary = "Make a move in a game",
            description = "Submit a move for a player in the specified game. The game will be created if it doesn't exist.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Move successfully made",
                    content = @Content(schema = @Schema(implementation = MoveResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid move (cell occupied, wrong turn, or invalid coordinates)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "409",
                    description = "Game has already finished",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{gameId}/move")
    public ResponseEntity<MoveResponse> makeMove(
            @Parameter(description = "Unique identifier for the game", required = true)
            @PathVariable String gameId,
            @Valid @RequestBody MoveRequest moveRequest) {
        log.info("Received move request for game {}: {}", gameId, moveRequest);
        MoveResponse response = gameService.makeMove(gameId, moveRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get game state",
            description = "Retrieve the current state of a game including board, status, and current turn")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Game state retrieved successfully",
                    content = @Content(schema = @Schema(implementation = GameStateResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Game not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{gameId}")
    public ResponseEntity<GameStateResponse> getGameState(
            @Parameter(description = "Unique identifier for the game", required = true)
            @PathVariable String gameId) {
        log.info("Fetching game state for game {}", gameId);
        GameStateResponse response = gameService.getGameState(gameId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Create a new game",
            description = "Create a new Tic Tac Toe game with the specified ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Game created successfully",
                    content = @Content(schema = @Schema(implementation = GameStateResponse.class)))
    })
    @PostMapping("/{gameId}")
    public ResponseEntity<GameStateResponse> createGame(
            @Parameter(description = "Unique identifier for the new game", required = true)
            @PathVariable String gameId) {
        log.info("Creating new game with ID: {}", gameId);
        gameService.createGame(gameId);
        GameStateResponse response = gameService.getGameState(gameId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Delete a game",
            description = "Remove a game from the system")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Game deleted successfully")
    })
    @DeleteMapping("/{gameId}")
    public ResponseEntity<Void> deleteGame(
            @Parameter(description = "Unique identifier for the game to delete", required = true)
            @PathVariable String gameId) {
        log.info("Deleting game with ID: {}", gameId);
        gameService.deleteGame(gameId);
        return ResponseEntity.noContent().build();
    }
}