package com.fyre.tictactoe.engine.service;

import com.fyre.tictactoe.engine.config.BoardConfig;
import com.fyre.tictactoe.engine.dto.GameStateResponse;
import com.fyre.tictactoe.engine.dto.MoveRequest;
import com.fyre.tictactoe.engine.dto.MoveResponse;
import com.fyre.tictactoe.engine.exception.GameAlreadyFinishedException;
import com.fyre.tictactoe.engine.exception.GameNotFoundException;
import com.fyre.tictactoe.engine.exception.InvalidMoveException;
import com.fyre.tictactoe.engine.factory.GameFactory;
import com.fyre.tictactoe.engine.model.Game;
import com.fyre.tictactoe.engine.model.GameStatus;
import com.fyre.tictactoe.engine.model.Player;
import com.fyre.tictactoe.engine.rules.TicTacToeRulesEngine;
import com.fyre.tictactoe.engine.strategy.BasicStrategy;
import com.fyre.tictactoe.engine.strategy.WinConditionStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {

    private GameService gameService;
    private TicTacToeRulesEngine rulesEngine;
    private GameFactory gameFactory;

    @BeforeEach
    void setUp() {
        WinConditionStrategy strategy = new BasicStrategy(3);

        BoardConfig boardConfig = new BoardConfig();
        boardConfig.setSize(3);
        boardConfig.setWinLength(3);

        gameFactory = new GameFactory(boardConfig, strategy);

        rulesEngine = new TicTacToeRulesEngine();

        gameService = new GameService(rulesEngine, gameFactory);
    }

    @Test
    void testCreateGame() {
        String gameId = "test-game";
        Game game = gameService.createGame(gameId);

        assertNotNull(game);
        assertEquals(gameId, game.getGameId());
        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
        assertEquals(Player.X, game.getCurrentTurn());
        assertNotNull(game.getBoard());
    }

    @Test
    void testGetGame() {
        String gameId = "test-game";
        gameService.createGame(gameId);

        Game game = gameService.getGame(gameId);
        assertEquals(gameId, game.getGameId());
    }

    @Test
    void testGetGame_NotFound() {
        assertThrows(GameNotFoundException.class, () -> gameService.getGame("non-existent"));
    }

    @Test
    void testGetGameState() {
        String gameId = "test-game";
        gameService.createGame(gameId);

        GameStateResponse response = gameService.getGameState(gameId);
        assertEquals(gameId, response.getGameId());
        assertEquals(GameStatus.IN_PROGRESS, response.getStatus());
        assertEquals(Player.X, response.getCurrentTurn());
        assertNull(response.getWinner());
    }

    @Test
    void testMakeMove() {
        String gameId = "test-game";
        MoveRequest moveRequest = new MoveRequest(Player.X, 0, 0);

        MoveResponse response = gameService.makeMove(gameId, moveRequest);

        assertEquals(gameId, response.getGameId());
        assertEquals(Player.X, response.getBoard()[0][0]);
        assertEquals(GameStatus.IN_PROGRESS, response.getStatus());
        assertEquals(Player.O, response.getNextTurn());
        assertNull(response.getWinner());
    }

    @Test
    void testMakeMove_OccupiedCell() {
        String gameId = "test-game";
        gameService.makeMove(gameId, new MoveRequest(Player.X, 0, 0));

        assertThrows(InvalidMoveException.class, () ->
            gameService.makeMove(gameId, new MoveRequest(Player.O, 0, 0)));
    }

    @Test
    void testMakeMove_WrongTurn() {
        String gameId = "test-game";
        gameService.makeMove(gameId, new MoveRequest(Player.X, 0, 0));

        assertThrows(InvalidMoveException.class, () ->
            gameService.makeMove(gameId, new MoveRequest(Player.X, 0, 1)));
    }

    @Test
    void testMakeMove_GameFinished() {
        String gameId = "test-game";
        gameService.makeMove(gameId, new MoveRequest(Player.X, 0, 0));
        gameService.makeMove(gameId, new MoveRequest(Player.O, 1, 0));
        gameService.makeMove(gameId, new MoveRequest(Player.X, 0, 1));
        gameService.makeMove(gameId, new MoveRequest(Player.O, 1, 1));
        gameService.makeMove(gameId, new MoveRequest(Player.X, 0, 2));

        assertThrows(GameAlreadyFinishedException.class, () ->
            gameService.makeMove(gameId, new MoveRequest(Player.O, 2, 2)));
    }

    @Test
    void testWinCondition_HorizontalTop() {
        String gameId = "win-horizontal-top";
        gameService.makeMove(gameId, new MoveRequest(Player.X, 0, 0));
        gameService.makeMove(gameId, new MoveRequest(Player.O, 1, 0));
        gameService.makeMove(gameId, new MoveRequest(Player.X, 0, 1));
        gameService.makeMove(gameId, new MoveRequest(Player.O, 1, 1));
        MoveResponse response = gameService.makeMove(gameId, new MoveRequest(Player.X, 0, 2));

        assertEquals(GameStatus.X_WINS, response.getStatus());
        assertEquals(Player.X, response.getWinner());
        assertEquals("Player X wins!", response.getMessage());
    }

    @Test
    void testWinCondition_HorizontalMiddle() {
        String gameId = "win-horizontal-middle";
        gameService.makeMove(gameId, new MoveRequest(Player.X, 0, 0));
        gameService.makeMove(gameId, new MoveRequest(Player.O, 1, 0));
        gameService.makeMove(gameId, new MoveRequest(Player.X, 0, 1));
        gameService.makeMove(gameId, new MoveRequest(Player.O, 1, 1));
        gameService.makeMove(gameId, new MoveRequest(Player.X, 2, 0));
        MoveResponse response = gameService.makeMove(gameId, new MoveRequest(Player.O, 1, 2));

        assertEquals(GameStatus.O_WINS, response.getStatus());
        assertEquals(Player.O, response.getWinner());
    }

    @Test
    void testWinCondition_VerticalLeft() {
        String gameId = "win-vertical-left";
        gameService.makeMove(gameId, new MoveRequest(Player.X, 0, 0));
        gameService.makeMove(gameId, new MoveRequest(Player.O, 0, 1));
        gameService.makeMove(gameId, new MoveRequest(Player.X, 1, 0));
        gameService.makeMove(gameId, new MoveRequest(Player.O, 0, 2));
        MoveResponse response = gameService.makeMove(gameId, new MoveRequest(Player.X, 2, 0));

        assertEquals(GameStatus.X_WINS, response.getStatus());
        assertEquals(Player.X, response.getWinner());
    }

    @Test
    void testWinCondition_DiagonalTopLeftToBottomRight() {
        String gameId = "win-diagonal-1";
        gameService.makeMove(gameId, new MoveRequest(Player.X, 0, 0));
        gameService.makeMove(gameId, new MoveRequest(Player.O, 0, 1));
        gameService.makeMove(gameId, new MoveRequest(Player.X, 1, 1));
        gameService.makeMove(gameId, new MoveRequest(Player.O, 0, 2));
        MoveResponse response = gameService.makeMove(gameId, new MoveRequest(Player.X, 2, 2));

        assertEquals(GameStatus.X_WINS, response.getStatus());
        assertEquals(Player.X, response.getWinner());
    }

    @Test
    void testWinCondition_DiagonalTopRightToBottomLeft() {
        String gameId = "win-diagonal-2";
        gameService.makeMove(gameId, new MoveRequest(Player.X, 0, 0));
        gameService.makeMove(gameId, new MoveRequest(Player.O, 0, 2));
        gameService.makeMove(gameId, new MoveRequest(Player.X, 0, 1));
        gameService.makeMove(gameId, new MoveRequest(Player.O, 1, 1));
        gameService.makeMove(gameId, new MoveRequest(Player.X, 1, 0));
        MoveResponse response = gameService.makeMove(gameId, new MoveRequest(Player.O, 2, 0));

        assertEquals(GameStatus.O_WINS, response.getStatus());
        assertEquals(Player.O, response.getWinner());
    }

    @Test
    void testDrawCondition() {
        String gameId = "draw-game";
        gameService.makeMove(gameId, new MoveRequest(Player.X, 0, 0));
        gameService.makeMove(gameId, new MoveRequest(Player.O, 0, 1));
        gameService.makeMove(gameId, new MoveRequest(Player.X, 0, 2));
        gameService.makeMove(gameId, new MoveRequest(Player.O, 1, 0));
        gameService.makeMove(gameId, new MoveRequest(Player.X, 1, 2));
        gameService.makeMove(gameId, new MoveRequest(Player.O, 1, 1));
        gameService.makeMove(gameId, new MoveRequest(Player.X, 2, 1));
        gameService.makeMove(gameId, new MoveRequest(Player.O, 2, 2));
        MoveResponse response = gameService.makeMove(gameId, new MoveRequest(Player.X, 2, 0));

        assertEquals(GameStatus.DRAW, response.getStatus());
        assertNull(response.getWinner());
        assertEquals("Game ended in a draw!", response.getMessage());
    }

    @Test
    void testTurnSwitching() {
        String gameId = "turn-test";
        Game game = gameService.createGame(gameId);
        assertEquals(Player.X, game.getCurrentTurn());

        gameService.makeMove(gameId, new MoveRequest(Player.X, 0, 0));
        game = gameService.getGame(gameId);
        assertEquals(Player.O, game.getCurrentTurn());

        gameService.makeMove(gameId, new MoveRequest(Player.O, 1, 1));
        game = gameService.getGame(gameId);
        assertEquals(Player.X, game.getCurrentTurn());
    }

    @Test
    void testDeleteGame() {
        String gameId = "delete-test";
        gameService.createGame(gameId);
        assertNotNull(gameService.getGame(gameId));

        gameService.deleteGame(gameId);
        assertThrows(GameNotFoundException.class, () -> gameService.getGame(gameId));
    }

    @Test
    void testGetAllGames() {
        gameService.createGame("game1");
        gameService.createGame("game2");
        gameService.createGame("game3");

        assertEquals(3, gameService.getAllGames().size());
    }

    @Test
    void testBoardStateAfterMultipleMoves() {
        String gameId = "board-test";
        gameService.makeMove(gameId, new MoveRequest(Player.X, 0, 0));
        gameService.makeMove(gameId, new MoveRequest(Player.O, 1, 1));
        gameService.makeMove(gameId, new MoveRequest(Player.X, 2, 2));

        Game game = gameService.getGame(gameId);
        assertEquals(Player.X, game.getBoard()[0][0]);
        assertEquals(Player.O, game.getBoard()[1][1]);
        assertEquals(Player.X, game.getBoard()[2][2]);
        assertNull(game.getBoard()[0][1]);
        assertNull(game.getBoard()[0][2]);
    }
}