package com.fyre.tictactoe.session.simulator;

import com.fyre.tictactoe.session.dto.GameStateResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SimulationResult {
    private GameStateResponse finalGameState;
    private int totalMoves;
}