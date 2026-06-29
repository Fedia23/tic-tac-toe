package com.fyre.tictactoe.session.util;

import com.fyre.tictactoe.session.model.GameResult;
import org.springframework.stereotype.Component;

@Component
public class GameResultMapper {

    public GameResult mapWinnerToResult(String winner) {
        if (winner == null) {
            return GameResult.IN_PROGRESS;
        }

        return switch (winner) {
            case "X" -> GameResult.X_WINS;
            case "O" -> GameResult.O_WINS;
            case "DRAW" -> GameResult.DRAW;
            default -> GameResult.IN_PROGRESS;
        };
    }
}