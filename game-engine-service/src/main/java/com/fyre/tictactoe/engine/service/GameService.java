package com.fyre.tictactoe.engine.service;

import com.fyre.tictactoe.engine.dto.MoveRequest;
import com.fyre.tictactoe.engine.dto.MoveResponse;
import com.fyre.tictactoe.engine.dto.GameStateResponse;
import com.fyre.tictactoe.engine.exception.GameAlreadyFinishedException;
import com.fyre.tictactoe.engine.exception.GameNotFoundException;
import com.fyre.tictactoe.engine.exception.InvalidMoveException;
import com.fyre.tictactoe.engine.factory.GameFactory;
import com.fyre.tictactoe.engine.model.Game;
import com.fyre.tictactoe.engine.model.GameStatus;
import com.fyre.tictactoe.engine.model.Player;
import com.fyre.tictactoe.engine.rules.TicTacToeRulesEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameService {

    private final Map<String, Game> games = new ConcurrentHashMap<>();
    private final TicTacToeRulesEngine rulesEngine;
    private final GameFactory gameFactory;

    public Game createGame(String gameId) {
        Game game = gameFactory.createGame(gameId);
        games.put(gameId, game);
        log.info("Created new game with ID: {}", gameId);
        return game;
    }

    public Game getGame(String gameId) {
        Game game = games.get(gameId);
        if (game == null) {
            throw new GameNotFoundException(gameId);
        }
        return game;
    }

    public GameStateResponse getGameState(String gameId) {
        Game game = getGame(gameId);
        return new GameStateResponse(
                game.getGameId(),
                game.getBoard(),
                game.getStatus(),
                game.getCurrentTurn(),
                game.getWinner()
        );
    }

    public MoveResponse makeMove(String gameId, MoveRequest moveRequest) {
        Game game = games.computeIfAbsent(gameId, gameFactory::createGame);

        validateMove(game, moveRequest);

        game.getBoard()[moveRequest.getRow()][moveRequest.getCol()] = moveRequest.getPlayer();
        log.info("Player {} made move at ({}, {}) in game {}",
                moveRequest.getPlayer(), moveRequest.getRow(), moveRequest.getCol(), gameId);

        GameStatus newStatus = game.calculateStatus();
        Player winner = game.getWinner();
        String message = rulesEngine.generateGameMessage(newStatus, moveRequest.getPlayer());

        if (newStatus == GameStatus.IN_PROGRESS) {
            game.switchTurn();
        }

        return new MoveResponse(
                game.getGameId(),
                game.getBoard(),
                game.getStatus(),
                winner,
                game.getCurrentTurn(),
                message
        );
    }

    private void validateMove(Game game, MoveRequest moveRequest) {
        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new GameAlreadyFinishedException(game.getStatus());
        }

        if (game.getBoard()[moveRequest.getRow()][moveRequest.getCol()] != null) {
            throw new InvalidMoveException(
                    String.format("Cell (%d, %d) is already occupied",
                            moveRequest.getRow(), moveRequest.getCol())
            );
        }

        if (game.getCurrentTurn() != moveRequest.getPlayer()) {
            throw new InvalidMoveException(
                    String.format("It's not player %s's turn. Current turn: %s",
                            moveRequest.getPlayer(), game.getCurrentTurn())
            );
        }
    }

    public Map<String, Game> getAllGames() {
        return games;
    }

    public void deleteGame(String gameId) {
        games.remove(gameId);
        log.info("Deleted game with ID: {}", gameId);
    }
}