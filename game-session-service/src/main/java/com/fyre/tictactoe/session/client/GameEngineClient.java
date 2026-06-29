package com.fyre.tictactoe.session.client;

import com.fyre.tictactoe.session.dto.GameStateResponse;
import com.fyre.tictactoe.session.dto.MoveRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "game-engine-service",
    url = "${game.engine.url}"
)
public interface GameEngineClient {

    @PostMapping("/games")
    GameStateResponse createGame();

    @PostMapping("/games/{gameId}/moves")
    GameStateResponse makeMove(
        @PathVariable("gameId") String gameId,
        @RequestBody MoveRequest moveRequest
    );

    @GetMapping("/games/{gameId}")
    GameStateResponse getGameState(@PathVariable("gameId") String gameId);
}