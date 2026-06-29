package com.fyre.tictactoe.engine.rules;

import com.fyre.tictactoe.engine.model.GameStatus;
import com.fyre.tictactoe.engine.model.Player;
import org.springframework.stereotype.Component;

@Component
public class TicTacToeRulesEngine {

    public String generateGameMessage(GameStatus status, Player lastPlayer) {
        return switch (status) {
            case X_WINS -> "Player X wins!";
            case O_WINS -> "Player O wins!";
            case DRAW -> "Game ended in a draw!";
            case IN_PROGRESS -> String.format("Move successful. Player %s's turn next.",
                    lastPlayer == Player.X ? Player.O : Player.X);
        };
    }
}