package com.fyre.tictactoe.session.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "moves")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Move {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private GameSession session;

    @Column(nullable = false)
    private Integer moveNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Player player;

    @Column(nullable = false)
    private Integer position;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}