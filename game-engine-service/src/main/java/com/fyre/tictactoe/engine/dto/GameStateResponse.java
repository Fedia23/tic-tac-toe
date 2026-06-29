package com.fyre.tictactoe.engine.dto;

import com.fyre.tictactoe.engine.model.GameStatus;
import com.fyre.tictactoe.engine.model.Player;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameStateResponse {
    private String gameId;
    private Player[][] board;
    private GameStatus status;
    private Player currentTurn;
    private Player winner;
}