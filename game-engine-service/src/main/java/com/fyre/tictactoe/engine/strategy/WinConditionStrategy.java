package com.fyre.tictactoe.engine.strategy;

import com.fyre.tictactoe.engine.model.GameStatus;
import com.fyre.tictactoe.engine.model.Player;

public interface WinConditionStrategy {
    /**
     * Evaluates the game board and determines the current game status
     *
     * @param board The game board to evaluate
     * @return The current game status (IN_PROGRESS, X_WINS, O_WINS, or DRAW)
     */
    GameStatus evaluate(Player[][] board);
}
