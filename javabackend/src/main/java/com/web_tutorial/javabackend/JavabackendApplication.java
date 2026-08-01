package com.web_tutorial.javabackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class JavabackendApplication {

	public static void main(String[] args) {
		String runtimeMode = System.getenv().getOrDefault(
				"APP_RUNTIME_MODE",
				System.getProperty("app.runtime.mode", "application"));
		if ("migration".equalsIgnoreCase(runtimeMode)) {
			MigrationApplication.run(args);
			return;
		}
		SpringApplication.run(JavabackendApplication.class, args);
	}

}
