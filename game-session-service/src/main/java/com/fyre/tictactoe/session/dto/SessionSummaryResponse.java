package com.fyre.tictactoe.session.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionSummaryResponse {
    private String sessionId;
    private String gameId;
    private String status;
    private String result;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private Integer totalMoves;
}