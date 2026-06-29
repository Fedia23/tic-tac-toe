package com.fyre.tictactoe.session.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response after simulating a complete game")
public class SimulateGameResponse {

    @Schema(description = "Session identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private String sessionId;

    @Schema(description = "Final session status", example = "COMPLETED")
    private String status;

    @Schema(description = "Game result", example = "X_WINS", allowableValues = {"X_WINS", "O_WINS", "DRAW", "IN_PROGRESS"})
    private String result;

    @Schema(description = "Total number of moves played", example = "7")
    private Integer totalMoves;

    @Schema(description = "Status message", example = "Game simulation completed")
    private String message;
}