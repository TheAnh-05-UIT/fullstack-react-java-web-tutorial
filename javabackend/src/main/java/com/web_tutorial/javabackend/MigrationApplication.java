package com.web_tutorial.javabackend;

import org.flywaydb.core.Flyway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

public final class MigrationApplication {

    private MigrationApplication() {
    }

    public static void run(String[] args) {
        try (ConfigurableApplicationContext context = start(args)) {
            context.getBean(Flyway.class).validate();
        }
    }

    public static ConfigurableApplicationContext start(String[] args) {
        SpringApplication application = new SpringApplication(MigrationConfiguration.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        return application.run(args);
    }

    @Configuration(proxyBeanMethods = false)
    @Profile("migration")
    @EnableAutoConfiguration(exclude = {
            HibernateJpaAutoConfiguration.class,
            JpaRepositoriesAutoConfiguration.class
    })
    static class MigrationConfiguration {
    }
}
