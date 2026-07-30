package com.web_tutorial.javabackend.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;

class RefreshTokenColumnMigrationTest {

    private static final String EXISTING_VALUE = "existing-refresh-token";
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("webtutorial_migration_test")
            .withUsername("migration_test")
            .withPassword("migration_test");

    @BeforeAll
    static void createPreAuthSchemaAndMigrate() throws Exception {
        MYSQL.start();
        try (Connection connection = connection(); Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE users (
                        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        refresh_token VARCHAR(255) NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE tutorials (
                        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        status VARCHAR(255),
                        is_deleted BIT(1) NOT NULL DEFAULT 0,
                        created_at DATETIME(6)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE projects (
                        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        status VARCHAR(255),
                        is_deleted BIT(1) NOT NULL DEFAULT 0,
                        created_at DATETIME(6)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE roadmaps (
                        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        slug VARCHAR(255),
                        is_deleted BIT(1) NOT NULL DEFAULT 0
                    )
                    """);
            statement.executeUpdate(
                    "INSERT INTO users (refresh_token) VALUES ('" + EXISTING_VALUE + "'), (NULL)");
            statement.executeUpdate("INSERT INTO tutorials (id) VALUES (1)");
            statement.executeUpdate("INSERT INTO projects (id) VALUES (1)");
            statement.executeUpdate("INSERT INTO roadmaps (id) VALUES (1)");
        }

        flyway().migrate();
    }

    @Test
    void migrationsExpandLegacyColumnCreateSessionsAndPreserveUsers() throws Exception {
        try (Connection connection = connection(); Statement statement = connection.createStatement()) {
            try (ResultSet column = statement.executeQuery("""
                    SELECT data_type, character_maximum_length, is_nullable
                    FROM information_schema.columns
                    WHERE table_schema = DATABASE()
                      AND table_name = 'users'
                      AND column_name = 'refresh_token'
                    """)) {
                assertThat(column.next()).isTrue();
                assertThat(column.getString("data_type")).isEqualTo("varchar");
                assertThat(column.getLong("character_maximum_length")).isEqualTo(2048);
                assertThat(column.getString("is_nullable")).isEqualTo("YES");
            }

            try (ResultSet rows = statement.executeQuery(
                    "SELECT COUNT(*) AS row_count, MAX(CHAR_LENGTH(refresh_token)) AS max_length FROM users")) {
                assertThat(rows.next()).isTrue();
                assertThat(rows.getLong("row_count")).isEqualTo(2);
                assertThat(rows.getLong("max_length")).isZero();
            }

            for (String table : new String[] {"tutorials", "projects", "roadmaps"}) {
                try (ResultSet rows = statement.executeQuery("SELECT COUNT(*) AS row_count FROM " + table)) {
                    assertThat(rows.next()).isTrue();
                    assertThat(rows.getLong("row_count")).isOne();
                }
            }

            try (ResultSet table = statement.executeQuery("""
                    SELECT COUNT(*) AS table_count
                    FROM information_schema.tables
                    WHERE table_schema = DATABASE()
                      AND table_name = 'refresh_token_sessions'
                    """)) {
                assertThat(table.next()).isTrue();
                assertThat(table.getLong("table_count")).isOne();
            }

            try (ResultSet constraints = statement.executeQuery("""
                    SELECT COUNT(*) AS constraint_count
                    FROM information_schema.table_constraints
                    WHERE table_schema = DATABASE()
                      AND table_name = 'refresh_token_sessions'
                      AND constraint_type IN ('FOREIGN KEY', 'UNIQUE')
                    """)) {
                assertThat(constraints.next()).isTrue();
                assertThat(constraints.getLong("constraint_count")).isEqualTo(3);
            }

            try (ResultSet indexes = statement.executeQuery("""
                    SELECT COUNT(DISTINCT index_name) AS index_count
                    FROM information_schema.statistics
                    WHERE table_schema = DATABASE()
                      AND table_name = 'refresh_token_sessions'
                      AND index_name IN (
                        'idx_refresh_token_sessions_user',
                        'idx_refresh_token_sessions_expires',
                        'idx_refresh_token_sessions_revoked'
                      )
                    """)) {
                assertThat(indexes.next()).isTrue();
                assertThat(indexes.getLong("index_count")).isEqualTo(3);
            }

            try (ResultSet historyColumns = statement.executeQuery("""
                    SELECT COUNT(*) AS column_count,
                           SUM(data_type = 'varchar') AS varchar_count
                    FROM information_schema.columns
                    WHERE table_schema = DATABASE()
                      AND table_name = 'refresh_token_sessions'
                      AND column_name IN (
                        'previous_token_hash',
                        'previous_jti',
                        'previous_consumed_at'
                      )
                    """)) {
                assertThat(historyColumns.next()).isTrue();
                assertThat(historyColumns.getLong("column_count")).isEqualTo(3);
                assertThat(historyColumns.getLong("varchar_count")).isEqualTo(2);
            }
        }
    }

    @Test
    void migrationCanBeRerunAndValidated() {
        assertThat(flyway().migrate().migrationsExecuted).isZero();
        flyway().validate();
    }

    private static Flyway flyway() {
        return Flyway.configure()
                .dataSource(migrationJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .load();
    }

    private static Connection connection() throws Exception {
        return DriverManager.getConnection(migrationJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
    }

    private static String migrationJdbcUrl() {
        return MYSQL.getJdbcUrl();
    }
}
