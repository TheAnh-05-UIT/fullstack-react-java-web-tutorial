package com.web_tutorial.javabackend.service.tutorial.impl;

import com.web_tutorial.javabackend.domain.tutorial.Tutorial;
import com.web_tutorial.javabackend.repository.tutorial.TutorialRepository;
import com.web_tutorial.javabackend.service.tutorial.TutorialService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TutorialServiceImpl implements TutorialService {

    private final TutorialRepository tutorialRepository;

    public TutorialServiceImpl(TutorialRepository tutorialRepository) {
        this.tutorialRepository = tutorialRepository;
    }

    @Override
    public List<Tutorial> getAllTutorials() {
        return this.tutorialRepository.findAll();
    }

    @Override
    public Optional<Tutorial> getTutorialById(Long id) {
        return this.tutorialRepository.findById(id);
    }

    @Override
    public Optional<Tutorial> getTutorialBySlug(String slug) {
        return this.tutorialRepository.findBySlug(slug);
    }

    @Override
    public Tutorial createTutorial(Tutorial tutorial) {
        return this.tutorialRepository.save(tutorial);
    }

    @Override
    public Tutorial updateTutorial(Long id, Tutorial tutorialDetails) {
        return this.tutorialRepository.findById(id).map(tutorial -> {
            tutorial.setTitle(tutorialDetails.getTitle());
            tutorial.setSlug(tutorialDetails.getSlug());
            tutorial.setContent(tutorialDetails.getContent());
            return this.tutorialRepository.save(tutorial);
        }).orElseThrow(() -> new RuntimeException("Tutorial not found with id " + id));
    }

    @Override
    public void deleteTutorial(Long id) {
        this.tutorialRepository.deleteById(id);
    }
}
