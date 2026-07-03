package com.web_tutorial.javabackend.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Expose the 'uploads' directory statically
        Path uploadDir = Paths.get("uploads");
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadDir.toUri().toString(), "file:./uploads/");
    }
}
