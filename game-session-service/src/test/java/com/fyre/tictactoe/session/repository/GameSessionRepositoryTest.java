package com.fyre.tictactoe.session.repository;

import com.fyre.tictactoe.session.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Game Session Repository Tests")
class GameSessionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GameSessionRepository sessionRepository;

    @Autowired
    private MoveRepository moveRepository;

    @BeforeEach
    void setUp() {
        sessionRepository.deleteAll();
    }

    @Test
    @DisplayName("save - Should persist game session successfully")
    void save_Success() {
        GameSession session = createGameSession("game-123");

        GameSession saved = sessionRepository.save(session);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getGameId()).isEqualTo("game-123");
        assertThat(saved.getStatus()).isEqualTo(GameStatus.CREATED);
        assertThat(saved.getResult()).isEqualTo(GameResult.IN_PROGRESS);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("findById - Should retrieve session by id")
    void findById_Success() {
        GameSession session = createGameSession("game-456");
        GameSession saved = entityManager.persistAndFlush(session);

        Optional<GameSession> found = sessionRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getGameId()).isEqualTo("game-456");
    }

    @Test
    @DisplayName("findById - Should return empty for non-existent id")
    void findById_NotFound() {
        Optional<GameSession> found = sessionRepository.findById("non-existent");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("save - Should persist session with moves")
    void save_WithMoves() {
        GameSession session = createGameSession("game-789");
        session = entityManager.persistAndFlush(session);

        Move move1 = createMove(session, 1, Player.X, 0);
        Move move2 = createMove(session, 2, Player.O, 4);

        moveRepository.save(move1);
        moveRepository.save(move2);
        entityManager.flush();
        entityManager.clear();

        GameSession retrieved = sessionRepository.findById(session.getId()).orElseThrow();

        assertThat(retrieved.getMoves()).hasSize(2);
        assertThat(retrieved.getMoves().get(0).getPlayer()).isEqualTo(Player.X);
        assertThat(retrieved.getMoves().get(1).getPlayer()).isEqualTo(Player.O);
    }

    @Test
    @DisplayName("save - Should update existing session")
    void save_UpdateExisting() {
        GameSession session = createGameSession("game-update");
        GameSession saved = entityManager.persistAndFlush(session);

        saved.setStatus(GameStatus.COMPLETED);
        saved.setResult(GameResult.X_WINS);
        saved.setCompletedAt(LocalDateTime.now());

        GameSession updated = sessionRepository.save(saved);

        assertThat(updated.getStatus()).isEqualTo(GameStatus.COMPLETED);
        assertThat(updated.getResult()).isEqualTo(GameResult.X_WINS);
        assertThat(updated.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("delete - Should delete session cascade to moves")
    void delete_CascadeToMoves() {
        GameSession session = createGameSession("game-delete");
        session = entityManager.persistAndFlush(session);

        Move move = createMove(session, 1, Player.X, 0);
        moveRepository.save(move);
        entityManager.flush();

        String sessionId = session.getId();
        Long moveId = move.getId();

        entityManager.refresh(session);
        sessionRepository.delete(session);
        entityManager.flush();

        assertThat(sessionRepository.findById(sessionId)).isEmpty();
        assertThat(moveRepository.findById(moveId)).isEmpty();
    }

    @Test
    @DisplayName("findAll - Should return all sessions")
    void findAll_Success() {
        GameSession session1 = createGameSession("game-1");
        GameSession session2 = createGameSession("game-2");

        entityManager.persist(session1);
        entityManager.persist(session2);
        entityManager.flush();

        List<GameSession> sessions = sessionRepository.findAll();

        assertThat(sessions).hasSize(2);
    }

    @Test
    @DisplayName("save - Should handle board state correctly")
    void save_BoardState() {
        GameSession session = createGameSession("game-board");
        session.setCurrentBoard("X,O,X,O,X,O, , , ");

        GameSession saved = sessionRepository.save(session);
        entityManager.flush();

        GameSession retrieved = sessionRepository.findById(saved.getId()).orElseThrow();

        assertThat(retrieved.getCurrentBoard()).isEqualTo("X,O,X,O,X,O, , , ");
    }

    @Test
    @DisplayName("save - Should handle null completed date for in-progress games")
    void save_NullCompletedAt() {
        GameSession session = createGameSession("game-inprogress");
        session.setStatus(GameStatus.IN_PROGRESS);

        GameSession saved = sessionRepository.save(session);

        assertThat(saved.getCompletedAt()).isNull();
    }

    private GameSession createGameSession(String gameId) {
        return GameSession.builder()
            .gameId(gameId)
            .status(GameStatus.CREATED)
            .result(GameResult.IN_PROGRESS)
            .createdAt(LocalDateTime.now())
            .currentBoard(" , , , , , , , , ")
            .currentPlayer(Player.X)
            .build();
    }

    private Move createMove(GameSession session, int moveNumber, Player player, int position) {
        return Move.builder()
            .session(session)
            .moveNumber(moveNumber)
            .player(player)
            .position(position)
            .timestamp(LocalDateTime.now())
            .build();
    }
}