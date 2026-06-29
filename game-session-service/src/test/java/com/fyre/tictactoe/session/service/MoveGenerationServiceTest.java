package com.fyre.tictactoe.session.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Move Generation Service Tests")
class MoveGenerationServiceTest {

    private MoveGenerationService moveGenerationService;

    @BeforeEach
    void setUp() {
        moveGenerationService = new MoveGenerationService();
    }

    @Test
    @DisplayName("generateRandomMove - Should return valid position for empty board")
    void generateRandomMove_EmptyBoard() {
        List<String> emptyBoard = Arrays.asList(" ", " ", " ", " ", " ", " ", " ", " ", " ");

        Integer position = moveGenerationService.generateRandomMove(emptyBoard);

        assertThat(position).isBetween(0, 8);
    }

    @Test
    @DisplayName("generateRandomMove - Should return valid position for partially filled board")
    void generateRandomMove_PartiallyFilledBoard() {
        List<String> board = Arrays.asList("X", "O", " ", " ", "X", " ", " ", "O", " ");

        Integer position = moveGenerationService.generateRandomMove(board);

        assertThat(position).isIn(2, 3, 5, 6, 8);
        assertThat(board.get(position)).isIn(" ", "", null);
    }

    @Test
    @DisplayName("generateRandomMove - Should return the only available position")
    void generateRandomMove_OnePositionLeft() {
        List<String> board = Arrays.asList("X", "O", "X", "O", "X", "O", "O", "X", " ");

        Integer position = moveGenerationService.generateRandomMove(board);

        assertThat(position).isEqualTo(8);
    }

    @Test
    @DisplayName("generateRandomMove - Should throw exception for full board")
    void generateRandomMove_FullBoard() {
        List<String> fullBoard = Arrays.asList("X", "O", "X", "O", "X", "O", "O", "X", "O");

        assertThatThrownBy(() -> moveGenerationService.generateRandomMove(fullBoard))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("No available positions");
    }

    @Test
    @DisplayName("generateRandomMove - Should handle boards with null values")
    void generateRandomMove_BoardWithNulls() {
        List<String> board = Arrays.asList("X", null, " ", "O", null, " ", "X", " ", "O");

        Integer position = moveGenerationService.generateRandomMove(board);

        assertThat(position).isIn(1, 2, 4, 5, 7);
    }

    @Test
    @DisplayName("generateRandomMove - Should handle boards with empty strings")
    void generateRandomMove_BoardWithEmptyStrings() {
        List<String> board = Arrays.asList("X", "", " ", "O", "", " ", "X", " ", "O");

        Integer position = moveGenerationService.generateRandomMove(board);

        assertThat(position).isIn(1, 2, 4, 5, 7);
    }

    @Test
    @DisplayName("generateRandomMove - Should generate different positions over multiple calls")
    void generateRandomMove_GeneratesDifferentPositions() {
        List<String> emptyBoard = Arrays.asList(" ", " ", " ", " ", " ", " ", " ", " ", " ");
        Set<Integer> generatedPositions = new HashSet<>();

        for (int i = 0; i < 50; i++) {
            Integer position = moveGenerationService.generateRandomMove(emptyBoard);
            generatedPositions.add(position);
        }

        assertThat(generatedPositions.size()).isGreaterThan(1);
    }

    @Test
    @DisplayName("generateRandomMove - Should only return available positions")
    void generateRandomMove_OnlyAvailablePositions() {
        List<String> board = Arrays.asList("X", " ", "O", " ", "X", " ", "O", " ", "X");
        Set<Integer> expectedPositions = Set.of(1, 3, 5, 7);

        for (int i = 0; i < 20; i++) {
            Integer position = moveGenerationService.generateRandomMove(board);
            assertThat(expectedPositions).contains(position);
        }
    }

    @Test
    @DisplayName("generateRandomMove - Should work with board size 9")
    void generateRandomMove_CorrectBoardSize() {
        List<String> board = Arrays.asList(" ", " ", " ", " ", " ", " ", " ", " ", " ");

        Integer position = moveGenerationService.generateRandomMove(board);

        assertThat(position).isBetween(0, board.size() - 1);
    }
}