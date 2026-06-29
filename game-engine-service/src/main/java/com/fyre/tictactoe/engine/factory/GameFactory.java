package com.fyre.tictactoe.engine.factory;

import com.fyre.tictactoe.engine.config.BoardConfig;
import com.fyre.tictactoe.engine.model.Game;
import com.fyre.tictactoe.engine.model.GameStatus;
import com.fyre.tictactoe.engine.model.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.fyre.tictactoe.engine.strategy.WinConditionStrategy;

@Component
@RequiredArgsConstructor
public class GameFactory {

    private final BoardConfig boardConfig;
    private final WinConditionStrategy winConditionStrategy;

    public Game createGame(String gameId) {
        int size = boardConfig.getSize();
        Player[][] board = new Player[size][size];
        return new Game(gameId, board, GameStatus.IN_PROGRESS, Player.X, winConditionStrategy);
    }
}