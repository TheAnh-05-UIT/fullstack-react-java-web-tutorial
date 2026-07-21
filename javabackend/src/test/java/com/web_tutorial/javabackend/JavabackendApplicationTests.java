package com.web_tutorial.javabackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.web_tutorial.javabackend.support.AbstractMySqlIntegrationTest;

@SpringBootTest
@ActiveProfiles("test")
class JavabackendApplicationTests extends AbstractMySqlIntegrationTest {

	@Test
	void contextLoads() {
	}

}
