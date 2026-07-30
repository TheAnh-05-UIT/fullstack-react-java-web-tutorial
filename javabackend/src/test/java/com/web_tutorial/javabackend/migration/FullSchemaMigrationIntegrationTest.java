package com.web_tutorial.javabackend.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.repository.Repository;
import org.springframework.jdbc.core.JdbcTemplate;

import com.web_tutorial.javabackend.support.AbstractMySqlIntegrationTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class FullSchemaMigrationIntegrationTest extends AbstractMySqlIntegrationTest {

    private static final List<String> APPLICATION_TABLES = List.of(
            "authors",
            "categores",
            "devops_phases",
            "project_tags",
            "projects",
            "refresh_token_sessions",
            "roadmaps",
            "roadmapsteps",
            "roles",
            "tutorials",
            "user_learning_progress",
            "userprojectprogress",
            "users");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Flyway flyway;

    @Autowired
    private ListableBeanFactory beanFactory;

    @Test
    void emptyDatabaseIsFullyMigratedValidatedAndMapped() {
        flyway.validate();

        List<String> tables = jdbcTemplate.queryForList("""
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                  AND table_name <> 'flyway_schema_history'
                ORDER BY table_name
                """, String.class);
        assertThat(tables).containsExactlyElementsOf(APPLICATION_TABLES);

        List<Map<String, Object>> history = jdbcTemplate.queryForList("""
                SELECT version, success
                FROM flyway_schema_history
                WHERE version IS NOT NULL
                ORDER BY installed_rank
                """);
        assertThat(history).extracting(row -> row.get("version").toString())
                .containsExactly("1", "2", "3", "4", "5", "6");
        assertThat(history).allSatisfy(row -> assertThat(row.get("success")).isEqualTo(true));

        assertThat(beanFactory.getBeansOfType(Repository.class)).hasSize(12);
    }

    @Test
    void generatedTablesUseExplicitEngineCharsetKeysAndExpectedColumnTypes() {
        APPLICATION_TABLES.forEach(table -> {
            Map<String, Object> row = jdbcTemplate.queryForMap("SHOW CREATE TABLE `" + table + "`");
            String ddl = row.values().stream()
                    .map(Object::toString)
                    .filter(value -> value.startsWith("CREATE TABLE"))
                    .findFirst()
                    .orElseThrow();
            assertThat(ddl).contains("ENGINE=InnoDB", "CHARSET=utf8mb4");
        });

        Map<String, Object> tutorial = jdbcTemplate.queryForMap("SHOW CREATE TABLE tutorials");
        assertThat(tutorial.values().toString()).contains("`content` longtext", "`description` longtext");

        Map<String, Object> project = jdbcTemplate.queryForMap("SHOW CREATE TABLE projects");
        assertThat(project.values().toString()).contains("`content` mediumtext", "`description` mediumtext");

        Map<String, Object> session = jdbcTemplate.queryForMap("SHOW CREATE TABLE refresh_token_sessions");
        assertThat(session.values().toString())
                .contains("`family_id` varchar(36)")
                .contains("`token_hash` varchar(64)")
                .contains("`previous_token_hash` varchar(64)")
                .contains("FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)");
    }
}
