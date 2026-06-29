package com.fyre.tictactoe.session.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameStateResponse {
    private String gameId;
    private String status;
    private String winner;
    private String currentPlayer;
    private List<String> board;
    private String message;
}