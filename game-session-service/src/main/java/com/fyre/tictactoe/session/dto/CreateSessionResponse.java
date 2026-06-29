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
@Schema(description = "Response after creating a new game session")
public class CreateSessionResponse {

    @Schema(description = "Unique session identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private String sessionId;

    @Schema(description = "Game Engine Service game identifier", example = "game-123")
    private String gameId;

    @Schema(description = "Session status", example = "CREATED", allowableValues = {"CREATED", "IN_PROGRESS", "COMPLETED"})
    private String status;

    @Schema(description = "Status message", example = "Game session created successfully")
    private String message;
}