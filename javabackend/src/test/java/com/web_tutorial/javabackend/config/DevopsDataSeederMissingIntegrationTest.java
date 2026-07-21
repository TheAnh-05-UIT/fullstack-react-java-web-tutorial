package com.web_tutorial.javabackend.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import com.web_tutorial.javabackend.support.AbstractMySqlIntegrationTest;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class DevopsDataSeederMissingIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired(required = false)
    private DevopsDataSeeder devopsDataSeeder;

    @Test
    void testSeederIsDisabledWhenPropertyIsMissing() {
        // By default, matchIfMissing = false means if app.devops.seeding.enabled is not present, it won't load
        assertThat(devopsDataSeeder).isNull();
    }
}
