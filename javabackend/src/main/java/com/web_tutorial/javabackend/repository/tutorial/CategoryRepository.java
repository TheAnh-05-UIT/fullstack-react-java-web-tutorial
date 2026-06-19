package com.web_tutorial.javabackend.repository.tutorial;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.web_tutorial.javabackend.domain.tutorial.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);

    Optional<Category> findByName(String name);
}
