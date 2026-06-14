package com.web_tutorial.javabackend.repository.tutorial;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.web_tutorial.javabackend.domain.tutorial.Tutorial;

@Repository
public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
    Optional<Tutorial> findBySlug(String slug);
}
