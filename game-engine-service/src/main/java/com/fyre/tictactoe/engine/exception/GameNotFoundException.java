package com.fyre.tictactoe.engine.exception;

public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(String gameId) {
        super("Game not found with ID: " + gameId);
    }
}