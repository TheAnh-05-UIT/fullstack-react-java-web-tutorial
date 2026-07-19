package com.web_tutorial.javabackend.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.web_tutorial.javabackend.repository.devops.DevopsPhaseRepository;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.devops.seeding.enabled=false"
})
public class DevopsDataSeederDisabledIntegrationTest {

    @Autowired(required = false)
    private DevopsDataSeeder devopsDataSeeder;

    @Autowired
    private DevopsPhaseRepository phaseRepository;

    @Test
    void testSeederIsDisabledByProperty() {
        // Seeder bean should not be loaded into the context
        assertThat(devopsDataSeeder).isNull();
    }
}
