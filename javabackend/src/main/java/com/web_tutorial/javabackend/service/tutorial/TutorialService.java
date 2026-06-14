package com.web_tutorial.javabackend.service.tutorial;

import com.web_tutorial.javabackend.model.tutorial.Tutorial;
import java.util.List;
import java.util.Optional;

public interface TutorialService {
    List<Tutorial> getAllTutorials();
    
    Optional<Tutorial> getTutorialById(Long id);
    
    Optional<Tutorial> getTutorialBySlug(String slug);
    
    Tutorial createTutorial(Tutorial tutorial);
    
    Tutorial updateTutorial(Long id, Tutorial tutorialDetails);
    
    void deleteTutorial(Long id);
}
