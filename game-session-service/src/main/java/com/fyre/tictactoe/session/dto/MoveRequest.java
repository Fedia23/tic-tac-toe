package com.fyre.tictactoe.session.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for making a move in the game")
public class MoveRequest {

    @NotNull(message = "Position is required")
    @Min(value = 0, message = "Position must be between 0 and 8")
    @Max(value = 8, message = "Position must be between 0 and 8")
    @Schema(description = "Board position (0-8)", example = "4", required = true)
    private Integer position;

    @NotNull(message = "Player is required")
    @Pattern(regexp = "^[XO]$", message = "Player must be either 'X' or 'O'")
    @Schema(description = "Player making the move", example = "X", allowableValues = {"X", "O"}, required = true)
    private String player;
}