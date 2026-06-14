package com.web_tutorial.javabackend.service.tutorial;

import java.util.List;
import java.util.Optional;

import com.web_tutorial.javabackend.domain.tutorial.Tutorial;

public interface TutorialService {
    List<Tutorial> getAllTutorials();

    Optional<Tutorial> getTutorialById(Long id);

    Optional<Tutorial> getTutorialBySlug(String slug);

    Tutorial createTutorial(Tutorial tutorial);

    Tutorial updateTutorial(Long id, Tutorial tutorialDetails);

    void deleteTutorial(Long id);
}
