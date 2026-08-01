package com.web_tutorial.javabackend.migration;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.web.context.WebServerApplicationContext;

import com.web_tutorial.javabackend.MigrationApplication;
import com.web_tutorial.javabackend.support.AbstractMySqlIntegrationTest;

class MigrationApplicationIntegrationTest extends AbstractMySqlIntegrationTest {

    @Test
    void migrationModeMigratesValidatesAndExitsWithoutStartingHttp() {
        try (ConfigurableApplicationContext context = MigrationApplication.start(new String[] {
                "--spring.profiles.active=migration",
                "--spring.datasource.url=" + MYSQL.getJdbcUrl(),
                "--spring.datasource.username=" + MYSQL.getUsername(),
                "--spring.datasource.password=" + MYSQL.getPassword(),
                "--javabackend.jwt.base64-secret="
                        + "dGVzdC1qd3Qtc2VjcmV0LWlzLWF0LWxlYXN0LTMyLWJ5dGVzLWxvbmc="
        })) {
            assertThat(context).isNotInstanceOf(WebServerApplicationContext.class);
            context.getBean(Flyway.class).validate();
        }

        Flyway flyway = Flyway.configure()
                .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
                .load();
        assertThat(flyway.validateWithResult().validationSuccessful).isTrue();
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("7");
    }
}
