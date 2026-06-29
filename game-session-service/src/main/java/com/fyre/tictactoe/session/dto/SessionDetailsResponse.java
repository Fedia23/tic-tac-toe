package com.fyre.tictactoe.session.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionDetailsResponse {
    private String sessionId;
    private String gameId;
    private String status;
    private String result;
    private String currentBoard;
    private String currentPlayer;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private List<MoveDto> moves;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MoveDto {
        private Integer moveNumber;
        private String player;
        private Integer position;
        private LocalDateTime timestamp;
    }
}