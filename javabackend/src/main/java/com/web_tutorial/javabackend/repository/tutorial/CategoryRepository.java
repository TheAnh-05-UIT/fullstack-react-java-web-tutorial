package com.web_tutorial.javabackend.repository.tutorial;

import com.web_tutorial.javabackend.model.tutorial.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
