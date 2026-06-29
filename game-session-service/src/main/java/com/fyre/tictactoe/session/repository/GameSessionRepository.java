package com.fyre.tictactoe.session.repository;

import com.fyre.tictactoe.session.model.GameSession;
import com.fyre.tictactoe.session.model.GameStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, String> {

    @Query("SELECT s FROM GameSession s LEFT JOIN FETCH s.moves WHERE s.id = :id")
    Optional<GameSession> findByIdWithMoves(@Param("id") String id);

    Page<GameSession> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<GameSession> findByStatus(GameStatus status, Pageable pageable);
}