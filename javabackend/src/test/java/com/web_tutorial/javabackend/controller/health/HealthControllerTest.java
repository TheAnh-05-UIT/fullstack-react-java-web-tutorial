package com.web_tutorial.javabackend.controller.health;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.web_tutorial.javabackend.config.SecurityConfiguration;
import com.web_tutorial.javabackend.observability.AuditAccessDeniedHandler;
import com.web_tutorial.javabackend.observability.RequestIdFilter;
import com.web_tutorial.javabackend.observability.SecurityAuditLogger;

@WebMvcTest(HealthController.class)
@Import({SecurityConfiguration.class, RequestIdFilter.class})
class HealthControllerTest {

    @DynamicPropertySource
    static void securityProperties(DynamicPropertyRegistry registry) {
        registry.add("javabackend.jwt.base64-secret", () -> Base64.getEncoder().encodeToString(
                "health-controller-test-key-material-".repeat(2).getBytes(StandardCharsets.UTF_8)));
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private SecurityAuditLogger securityAuditLogger;

    @MockitoBean
    private AuditAccessDeniedHandler auditAccessDeniedHandler;

    @Test
    void health_shouldBePublicAndExposeOnlyUpStatus() throws Exception {
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);

        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists(RequestIdFilter.HEADER_NAME))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.error").isEmpty());
    }

    @Test
    void health_shouldReturnDownWithoutDatabaseDetailsWhenDatabaseFails() throws Exception {
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class))
                .thenThrow(new IllegalStateException("sensitive database detail"));

        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().exists(RequestIdFilter.HEADER_NAME))
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(content().json("{\"status\":\"DOWN\"}"));
    }
}
