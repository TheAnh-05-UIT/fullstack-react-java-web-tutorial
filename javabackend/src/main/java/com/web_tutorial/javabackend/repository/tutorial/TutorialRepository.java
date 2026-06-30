package com.web_tutorial.javabackend.repository.tutorial;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.web_tutorial.javabackend.domain.tutorial.Tutorial;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
    Optional<Tutorial> findBySlug(String slug);

    List<Tutorial> findByIsDeletedFalseOrderByIdDesc();
    Page<Tutorial> findByIsDeletedFalseOrderByIdDesc(Pageable pageable);

    Optional<Tutorial> findBySlugAndIsDeletedFalse(String slug);

    Optional<Tutorial> findByIdAndIsDeletedFalse(Long id);

    @Modifying
    @Query("UPDATE Tutorial t SET t.views = COALESCE(t.views, 0) + 1 WHERE t.id = :id")
    void incrementViews(@Param("id") Long id);
}
