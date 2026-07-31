package com.web_tutorial.javabackend.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.web_tutorial.javabackend.support.AbstractMySqlIntegrationTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class DatabaseIntegrityIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void businessKeysAreUniqueWithinTheirResourceAndEmailIsCaseInsensitive() {
        long suffix = System.nanoTime();
        Long roleId = insertRole("ROLE_DB2_" + suffix);
        insertUser("db2-" + suffix, "Db2-" + suffix + "@example.test", roleId);

        assertThatThrownBy(() ->
                insertUser("db2-duplicate-" + suffix, "db2-" + suffix + "@EXAMPLE.TEST", roleId))
                .isInstanceOf(DataIntegrityViolationException.class);

        insertTutorial("shared-" + suffix);
        assertThatThrownBy(() -> insertTutorial("shared-" + suffix))
                .isInstanceOf(DataIntegrityViolationException.class);

        insertProject("shared-" + suffix);
        insertRoadmap("shared-" + suffix);
        assertThatThrownBy(() -> insertRoadmap("shared-" + suffix))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM projects WHERE slug = ?",
                Integer.class, "shared-" + suffix)).isOne();
    }

    @Test
    void checksRejectInvalidProgressViewsAndTokenHashes() {
        long suffix = System.nanoTime();
        Long roleId = insertRole("ROLE_CHECK_" + suffix);
        Long userId = insertUser("check-" + suffix, "check-" + suffix + "@example.test", roleId);

        assertThatThrownBy(() -> insertProgress(userId, "too-low-" + suffix, -1))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> insertProgress(userId, "too-high-" + suffix, 101))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO refresh_token_sessions (
                    user_id, family_id, token_hash, current_jti, expires_at, created_at, updated_at, version
                ) VALUES (?, ?, ?, ?, ?, ?, ?, 0)
                """, userId, "family-" + suffix, "short", "jti-" + suffix,
                Timestamp.from(Instant.now().plusSeconds(3600)),
                Timestamp.from(Instant.now()), Timestamp.from(Instant.now())))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO tutorials (title, slug, content, views, is_deleted)
                VALUES ('invalid', ?, 'content', -1, b'0')
                """, "invalid-view-" + suffix))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> insertProgress(Long.MAX_VALUE, "invalid-fk-" + suffix, 50))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void learningProgressIsUniqueAndCascadesWhenUserIsDeleted() {
        long suffix = System.nanoTime();
        Long roleId = insertRole("ROLE_CASCADE_" + suffix);
        Long userId = insertUser("cascade-" + suffix, "cascade-" + suffix + "@example.test", roleId);

        insertProgress(userId, "content-" + suffix, 50);
        jdbcTemplate.update("""
                INSERT INTO refresh_token_sessions (
                    user_id, family_id, token_hash, current_jti, expires_at, created_at, updated_at, version
                ) VALUES (?, ?, ?, ?, ?, ?, ?, 0)
                """, userId, "cascade-family-" + suffix, "a".repeat(64), "cascade-jti-" + suffix,
                Timestamp.from(Instant.now().plusSeconds(3600)),
                Timestamp.from(Instant.now()), Timestamp.from(Instant.now()));
        assertThatThrownBy(() -> insertProgress(userId, "content-" + suffix, 75))
                .isInstanceOf(DataIntegrityViolationException.class);

        jdbcTemplate.update("DELETE FROM users WHERE id = ?", userId);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_learning_progress WHERE user_id = ?",
                Integer.class, userId)).isZero();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM refresh_token_sessions WHERE user_id = ?",
                Integer.class, userId)).isZero();
    }

    @Test
    void projectTagsAreUniqueOnlyInsideOneProject() {
        long suffix = System.nanoTime();
        insertProject("tag-project-a-" + suffix);
        insertProject("tag-project-b-" + suffix);
        Long firstProject = jdbcTemplate.queryForObject(
                "SELECT id FROM projects WHERE slug = ?", Long.class, "tag-project-a-" + suffix);
        Long secondProject = jdbcTemplate.queryForObject(
                "SELECT id FROM projects WHERE slug = ?", Long.class, "tag-project-b-" + suffix);

        jdbcTemplate.update("INSERT INTO project_tags (project_id, tags) VALUES (?, 'java')", firstProject);
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO project_tags (project_id, tags) VALUES (?, 'java')", firstProject))
                .isInstanceOf(DataIntegrityViolationException.class);
        jdbcTemplate.update("INSERT INTO project_tags (project_id, tags) VALUES (?, 'java')", secondProject);
    }

    @Test
    void roadmapDifficultyUsesStringMappingAndNamedConstraintsExist() {
        long suffix = System.nanoTime();
        jdbcTemplate.update("""
                INSERT INTO roadmaps (title, slug, difficulty, is_deleted)
                VALUES ('DB2 roadmap', ?, 'ADVANCED', b'0')
                """, "db2-roadmap-" + suffix);

        assertThat(jdbcTemplate.queryForObject(
                "SELECT difficulty FROM roadmaps WHERE slug = ?",
                String.class, "db2-roadmap-" + suffix)).isEqualTo("ADVANCED");

        Map<String, Integer> constraints = jdbcTemplate.query("""
                SELECT constraint_name
                FROM information_schema.table_constraints
                WHERE constraint_schema = DATABASE()
                  AND constraint_name IN (
                    'uk_users_email',
                    'uk_tutorials_slug',
                    'uk_projects_slug',
                    'uk_roadmaps_slug',
                    'ck_learning_progress_percent'
                  )
                """, rs -> {
                    java.util.HashMap<String, Integer> names = new java.util.HashMap<>();
                    while (rs.next()) {
                        names.put(rs.getString(1), 1);
                    }
                    return names;
                });
        assertThat(constraints).hasSize(5);
    }

    @Test
    void concurrentEmailAndLearningProgressInsertsHaveOneWinner() throws Exception {
        long suffix = System.nanoTime();
        String roleName = "ROLE_RACE_" + suffix;
        Long roleId = insertCommittedRole(roleName);
        String email = "race-" + suffix + "@example.test";
        try {
            List<Boolean> emailResults = race(
                    "INSERT INTO users (username, email, password, role_id) VALUES (?, ?, ?, ?)",
                    new Object[] {"race-a-" + suffix, email, "encoded", roleId},
                    new Object[] {"race-b-" + suffix, email.toUpperCase(), "encoded", roleId});
            assertThat(emailResults).containsExactlyInAnyOrder(true, false);
            Long userId = queryLongCommitted("SELECT id FROM users WHERE email = ?", email);

            String progressSql = """
                    INSERT INTO user_learning_progress (
                        user_id, content_type, content_key, status, progress_percent, created_at, updated_at
                    ) VALUES (?, 'TUTORIAL', ?, 'IN_PROGRESS', ?, ?, ?)
                    """;
            Timestamp now = Timestamp.from(Instant.now());
            List<Boolean> progressResults = race(
                    progressSql,
                    new Object[] {userId, "race-content-" + suffix, 25, now, now},
                    new Object[] {userId, "race-content-" + suffix, 75, now, now});
            assertThat(progressResults).containsExactlyInAnyOrder(true, false);
            assertThat(queryLongCommitted("""
                    SELECT COUNT(*) FROM user_learning_progress
                    WHERE user_id = ? AND content_type = 'TUTORIAL' AND content_key = ?
                    """, userId, "race-content-" + suffix)).isOne();
        } finally {
            executeCommitted("DELETE FROM users WHERE email = ?", email);
            executeCommitted("DELETE FROM roles WHERE name = ?", roleName);
        }
    }

    private Long insertRole(String name) {
        jdbcTemplate.update("INSERT INTO roles (name) VALUES (?)", name);
        return jdbcTemplate.queryForObject("SELECT id FROM roles WHERE name = ?", Long.class, name);
    }

    private Long insertUser(String username, String email, Long roleId) {
        jdbcTemplate.update("""
                INSERT INTO users (username, email, password, role_id)
                VALUES (?, ?, 'encoded-password', ?)
                """, username, email, roleId);
        return jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, email);
    }

    private void insertTutorial(String slug) {
        jdbcTemplate.update("""
                INSERT INTO tutorials (title, slug, content, views, is_deleted)
                VALUES ('DB2 tutorial', ?, 'content', 0, b'0')
                """, slug);
    }

    private void insertProject(String slug) {
        jdbcTemplate.update("""
                INSERT INTO projects (title, slug, content, views, is_deleted)
                VALUES ('DB2 project', ?, 'content', 0, b'0')
                """, slug);
    }

    private void insertRoadmap(String slug) {
        jdbcTemplate.update("""
                INSERT INTO roadmaps (title, slug, is_deleted)
                VALUES ('DB2 roadmap', ?, b'0')
                """, slug);
    }

    private void insertProgress(Long userId, String contentKey, int percent) {
        Instant now = Instant.now();
        jdbcTemplate.update("""
                INSERT INTO user_learning_progress (
                    user_id, content_type, content_key, status, progress_percent, created_at, updated_at
                ) VALUES (?, 'TUTORIAL', ?, 'IN_PROGRESS', ?, ?, ?)
                """, userId, contentKey, percent, Timestamp.from(now), Timestamp.from(now));
    }

    private List<Boolean> race(String sql, Object[] firstArgs, Object[] secondArgs) throws Exception {
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<Boolean> first = executor.submit(() -> executeRacingInsert(sql, firstArgs, ready, start));
            Future<Boolean> second = executor.submit(() -> executeRacingInsert(sql, secondArgs, ready, start));
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            return List.of(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS));
        }
    }

    private boolean executeRacingInsert(
            String sql,
            Object[] args,
            CountDownLatch ready,
            CountDownLatch start) throws Exception {
        try (Connection connection = MYSQL.createConnection("");
                PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < args.length; index++) {
                statement.setObject(index + 1, args[index]);
            }
            ready.countDown();
            start.await();
            statement.executeUpdate();
            return true;
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1062) {
                return false;
            }
            throw ex;
        }
    }

    private Long insertCommittedRole(String name) throws SQLException {
        try (Connection connection = MYSQL.createConnection("");
                PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO roles (name) VALUES (?)")) {
            insert.setString(1, name);
            insert.executeUpdate();
        }
        return jdbcTemplate.queryForObject("SELECT id FROM roles WHERE name = ?", Long.class, name);
    }

    private Long queryLongCommitted(String sql, Object... args) throws SQLException {
        try (Connection connection = MYSQL.createConnection("");
                PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < args.length; index++) {
                statement.setObject(index + 1, args[index]);
            }
            try (java.sql.ResultSet result = statement.executeQuery()) {
                assertThat(result.next()).isTrue();
                return result.getLong(1);
            }
        }
    }

    private void executeCommitted(String sql, Object... args) throws SQLException {
        try (Connection connection = MYSQL.createConnection("");
                PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < args.length; index++) {
                statement.setObject(index + 1, args[index]);
            }
            statement.executeUpdate();
        }
    }
}
