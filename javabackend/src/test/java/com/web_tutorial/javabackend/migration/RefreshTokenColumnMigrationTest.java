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
            statement.executeUpdate(
                    "INSERT INTO users (refresh_token) VALUES ('" + EXISTING_VALUE + "'), (NULL)");
        }

        flyway().migrate();
    }

    @Test
    void migrationExpandsColumnAndPreservesExistingRows() throws Exception {
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
                assertThat(rows.getLong("max_length")).isEqualTo(EXISTING_VALUE.length());
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
