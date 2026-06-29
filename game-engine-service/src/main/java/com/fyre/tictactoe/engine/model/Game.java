package com.fyre.tictactoe.engine.model;

import com.fyre.tictactoe.engine.strategy.WinConditionStrategy;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Game {
    private String gameId;
    private Player[][] board;
    private GameStatus status;
    private Player currentTurn;
    private WinConditionStrategy strategy;

    public void switchTurn() {
        this.currentTurn = (this.currentTurn == Player.X) ? Player.O : Player.X;
    }

    public GameStatus calculateStatus() {
        this.status = strategy.evaluate(board);
        return this.status;
    }

    public Player getWinner() {
        return switch (status) {
            case X_WINS -> Player.X;
            case O_WINS -> Player.O;
            default -> null;
        };
    }
}