package com.fyre.tictactoe.engine.config;

import com.fyre.tictactoe.engine.strategy.BasicStrategy;
import com.fyre.tictactoe.engine.strategy.WinConditionStrategy;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "game.board")
public class BoardConfig {

    private int size = 3;
    private int winLength = 3;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getWinLength() {
        return winLength;
    }

    public void setWinLength(int winLength) {
        this.winLength = winLength;
    }

    public int getTotalCells() {
        return size * size;
    }

    @Bean
    public WinConditionStrategy winConditionStrategy() {
        return new BasicStrategy(winLength);
    }
}