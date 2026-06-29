package com.fyre.tictactoe.session.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class MoveGenerationService {

    private final Random random = new Random();

    public Integer generateRandomMove(List<String> board) {
        List<Integer> availablePositions = new ArrayList<>();

        for (int i = 0; i < board.size(); i++) {
            if (board.get(i) == null || board.get(i).isEmpty() || board.get(i).equals(" ")) {
                availablePositions.add(i);
            }
        }

        if (availablePositions.isEmpty()) {
            throw new IllegalStateException("No available positions on the board");
        }

        return availablePositions.get(random.nextInt(availablePositions.size()));
    }
}