package com.web_tutorial.javabackend;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.web_tutorial.javabackend.config.DataInitializer;
import com.web_tutorial.javabackend.support.AbstractMySqlIntegrationTest;

@SpringBootTest
class AdminSeederTest extends AbstractMySqlIntegrationTest {

    @Autowired(required = false)
    private DataInitializer dataInitializer;

    @Test
    void bootstrapIsDisabledByDefaultInTestProfile() {
        assertThat(dataInitializer).isNull();
    }
}
