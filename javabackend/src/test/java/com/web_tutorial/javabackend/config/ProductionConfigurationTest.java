package com.web_tutorial.javabackend.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

class ProductionConfigurationTest {

    @Test
    void productionProfileRequiresExternalCredentialsAndUsesSafeJpaSettings() throws IOException {
        Properties properties = loadProperties("application-prod.properties");

        assertThat(properties.getProperty("spring.datasource.url")).isEqualTo("${DB_URL}");
        assertThat(properties.getProperty("spring.datasource.username")).isEqualTo("${DB_USERNAME}");
        assertThat(properties.getProperty("spring.datasource.password")).isEqualTo("${DB_PASSWORD}");
        assertThat(properties.getProperty("javabackend.jwt.base64-secret"))
                .isEqualTo("${JWT_SECRET_BASE64}");
        assertThat(properties.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
        assertThat(properties.getProperty("spring.jpa.show-sql")).isEqualTo("false");
        assertThat(properties.getProperty("app.bootstrap.admin.enabled")).isEqualTo("false");
    }

    @Test
    void developmentProfileImportsEnvFileAndMapsDockerDatabaseVariables() throws IOException {
        Properties properties = loadProperties("application-dev.properties");

        assertThat(properties.getProperty("spring.config.import"))
                .contains("optional:file:../.env[.properties]")
                .contains("optional:file:.env[.properties]");
        assertThat(properties.getProperty("spring.datasource.url"))
                .contains("localhost:${DB_PORT:3306}")
                .contains("${MYSQL_DATABASE:webtutorial}");
        assertThat(properties.getProperty("spring.datasource.username"))
                .isEqualTo("${DB_USERNAME:${MYSQL_USER:webtutorial_app}}");
        assertThat(properties.getProperty("spring.datasource.password"))
                .isEqualTo("${DB_PASSWORD:${MYSQL_PASSWORD:}}");
    }

    @Test
    void missingProductionJwtSecretFailsFastWithoutExposingAValue() {
        SecurityConfiguration configuration = new SecurityConfiguration();
        ReflectionTestUtils.setField(configuration, "jwtKey", "");

        assertThatThrownBy(configuration::jwtEncoder)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET_BASE64")
                .hasMessageNotContaining("password");
    }

    private Properties loadProperties(String resourceName) throws IOException {
        Properties properties = new Properties();
        try (var input = new ClassPathResource(resourceName).getInputStream()) {
            properties.load(input);
        }
        return properties;
    }
}
