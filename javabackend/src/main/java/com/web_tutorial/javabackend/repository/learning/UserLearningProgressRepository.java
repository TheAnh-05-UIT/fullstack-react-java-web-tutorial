package com.web_tutorial.javabackend.repository.learning;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.web_tutorial.javabackend.domain.learning.LearningContentType;
import com.web_tutorial.javabackend.domain.learning.LearningProgressStatus;
import com.web_tutorial.javabackend.domain.learning.UserLearningProgress;

@Repository
public interface UserLearningProgressRepository extends JpaRepository<UserLearningProgress, Long> {

    Optional<UserLearningProgress> findByUserIdAndContentTypeAndContentKey(Long userId, LearningContentType contentType, String contentKey);

    Page<UserLearningProgress> findByUserIdOrderByLastAccessedAtDesc(Long userId, Pageable pageable);

    Optional<UserLearningProgress> findTopByUserIdAndStatusOrderByLastAccessedAtDesc(Long userId, LearningProgressStatus status);

    long countByUserIdAndStatus(Long userId, LearningProgressStatus status);

    void deleteByUserIdAndContentTypeAndContentKey(Long userId, LearningContentType contentType, String contentKey);
}
