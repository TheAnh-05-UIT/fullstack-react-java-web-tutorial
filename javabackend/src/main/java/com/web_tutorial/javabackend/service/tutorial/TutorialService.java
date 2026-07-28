package com.web_tutorial.javabackend.service.tutorial;

import java.util.List;
import java.util.Optional;

import com.web_tutorial.javabackend.domain.dto.request.tutorial.CreateTutorialRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.tutorial.UpdateTutorialRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.ResultPaginationDTO;
import com.web_tutorial.javabackend.domain.dto.response.tutorial.TutorialResponseDTO;
import com.web_tutorial.javabackend.domain.tutorial.Tutorial;

import org.springframework.data.domain.Pageable;

public interface TutorialService {
    // Chỉ trả về tutorial chưa bị soft-deleted
    List<Tutorial> getAllTutorials();
    ResultPaginationDTO getAllTutorials(Pageable pageable);
    ResultPaginationDTO getAllTutorialsForAdmin(Pageable pageable);

    Optional<Tutorial> getTutorialById(Long id);

    Optional<Tutorial> getTutorialBySlug(String slug);

    Tutorial createTutorial(Tutorial tutorial);

    Tutorial updateTutorial(Long id, Tutorial tutorialDetails);

    // Soft delete thay vì hard delete
    void deleteTutorial(Long id);

    // Lookup author name – tránh controller truy cập repository trực tiếp
    String getAuthorNameByEmail(String email);
    void incrementViewCount(Long id);

    // DTO response methods cho Phase 3 (di chuyển business logic khỏi Controller)
    TutorialResponseDTO getTutorialResponseById(Long id);

    TutorialResponseDTO getTutorialResponseBySlug(String slug);
    TutorialResponseDTO getTutorialResponseByIdForAdmin(Long id);

    TutorialResponseDTO createTutorialFromDTO(CreateTutorialRequestDTO requestDTO);

    TutorialResponseDTO updateTutorialFromDTO(Long id, UpdateTutorialRequestDTO requestDTO);
}
