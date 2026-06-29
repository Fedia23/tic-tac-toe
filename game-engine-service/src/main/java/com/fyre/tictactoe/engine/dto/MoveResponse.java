package com.fyre.tictactoe.engine.dto;

import com.fyre.tictactoe.engine.model.GameStatus;
import com.fyre.tictactoe.engine.model.Player;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveResponse {
    private String gameId;
    private Player[][] board;
    private GameStatus status;
    private Player winner;
    private Player nextTurn;
    private String message;
}