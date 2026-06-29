package com.fyre.tictactoe.engine.exception;

import com.fyre.tictactoe.engine.model.GameStatus;

public class GameAlreadyFinishedException extends RuntimeException {
    public GameAlreadyFinishedException(GameStatus status) {
        super("Game has already finished with status: " + status);
    }
}