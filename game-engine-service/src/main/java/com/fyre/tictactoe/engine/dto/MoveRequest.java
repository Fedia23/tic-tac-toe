package com.fyre.tictactoe.engine.dto;

import com.fyre.tictactoe.engine.model.Player;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to make a move in the game")
public class MoveRequest {
    @Schema(description = "Player making the move", example = "X", required = true)
    @NotNull(message = "Player cannot be null")
    private Player player;

    @Schema(description = "Row index (0-2)", example = "1", required = true, minimum = "0", maximum = "2")
    @NotNull(message = "Row cannot be null")
    @Min(value = 0, message = "Row must be between 0 and 2")
    @Max(value = 2, message = "Row must be between 0 and 2")
    private Integer row;

    @Schema(description = "Column index (0-2)", example = "1", required = true, minimum = "0", maximum = "2")
    @NotNull(message = "Column cannot be null")
    @Min(value = 0, message = "Column must be between 0 and 2")
    @Max(value = 2, message = "Column must be between 0 and 2")
    private Integer col;
}