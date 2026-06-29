package com.fyre.tictactoe.engine.strategy;
import com.fyre.tictactoe.engine.model.GameStatus;
import com.fyre.tictactoe.engine.model.Player;

/**
 * Basic win condition strategy
 */
public class BasicStrategy implements WinConditionStrategy {

    private final int winLength;

    public BasicStrategy(int winLength) {
        this.winLength = winLength;
    }

    @Override
    public GameStatus evaluate(Player[][] board) {
        int size = board.length;

        // Check rows
        for (int i = 0; i < size; i++) {
            for (int j = 0; j <= size - winLength; j++) {
                if (checkLine(board, i, j, 0, 1, winLength)) {
                    return board[i][j] == Player.X ? GameStatus.X_WINS : GameStatus.O_WINS;
                }
            }
        }

        // Check columns
        for (int i = 0; i <= size - winLength; i++) {
            for (int j = 0; j < size; j++) {
                if (checkLine(board, i, j, 1, 0, winLength)) {
                    return board[i][j] == Player.X ? GameStatus.X_WINS : GameStatus.O_WINS;
                }
            }
        }

        // Check diagonals (top-left to bottom-right)
        for (int i = 0; i <= size - winLength; i++) {
            for (int j = 0; j <= size - winLength; j++) {
                if (checkLine(board, i, j, 1, 1, winLength)) {
                    return board[i][j] == Player.X ? GameStatus.X_WINS : GameStatus.O_WINS;
                }
            }
        }

        // Check diagonals (top-right to bottom-left)
        for (int i = 0; i <= size - winLength; i++) {
            for (int j = winLength - 1; j < size; j++) {
                if (checkLine(board, i, j, 1, -1, winLength)) {
                    return board[i][j] == Player.X ? GameStatus.X_WINS : GameStatus.O_WINS;
                }
            }
        }

        // Check for draw (no empty cells)
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == null) {
                    return GameStatus.IN_PROGRESS;
                }
            }
        }

        return GameStatus.DRAW;
    }

    private boolean checkLine(Player[][] board, int startRow, int startCol, int rowDelta, int colDelta, int length) {
        Player first = board[startRow][startCol];
        if (first == null) {
            return false;
        }

        for (int i = 1; i < length; i++) {
            int row = startRow + i * rowDelta;
            int col = startCol + i * colDelta;
            if (board[row][col] != first) {
                return false;
            }
        }

        return true;
    }
}
