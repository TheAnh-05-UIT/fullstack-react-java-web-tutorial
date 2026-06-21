package com.web_tutorial.javabackend.repository.tutorial;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.web_tutorial.javabackend.domain.tutorial.Tutorial;

@Repository
public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
    Optional<Tutorial> findBySlug(String slug);

    // Chỉ lấy các tutorial chưa bị soft-deleted
    List<Tutorial> findByIsDeletedFalse();

    // Tìm theo slug nhưng chưa bị xóa
    Optional<Tutorial> findBySlugAndIsDeletedFalse(String slug);

    // Tìm theo id nhưng chưa bị xóa
    Optional<Tutorial> findByIdAndIsDeletedFalse(Long id);
}
