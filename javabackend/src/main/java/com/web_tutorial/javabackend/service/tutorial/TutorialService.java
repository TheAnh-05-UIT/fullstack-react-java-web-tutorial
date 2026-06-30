package com.web_tutorial.javabackend.service.tutorial;

import java.util.List;
import java.util.Optional;

import com.web_tutorial.javabackend.domain.tutorial.Tutorial;

import org.springframework.data.domain.Pageable;
import com.web_tutorial.javabackend.domain.dto.response.ResultPaginationDTO;

public interface TutorialService {
    // Chỉ trả về tutorial chưa bị soft-deleted
    List<Tutorial> getAllTutorials();
    ResultPaginationDTO getAllTutorials(Pageable pageable);

    Optional<Tutorial> getTutorialById(Long id);

    Optional<Tutorial> getTutorialBySlug(String slug);

    Tutorial createTutorial(Tutorial tutorial);

    Tutorial updateTutorial(Long id, Tutorial tutorialDetails);

    // Soft delete thay vì hard delete
    void deleteTutorial(Long id);

    // Lookup author name – tránh controller truy cập repository trực tiếp
    String getAuthorNameByEmail(String email);
    void incrementViewCount(Long id);
}

