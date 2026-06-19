package com.web_tutorial.javabackend.service.tutorial.impl;

import com.web_tutorial.javabackend.domain.tutorial.Category;
import com.web_tutorial.javabackend.repository.tutorial.CategoryRepository;
import com.web_tutorial.javabackend.domain.tutorial.Tutorial;
import com.web_tutorial.javabackend.repository.tutorial.TutorialRepository;
import com.web_tutorial.javabackend.service.tutorial.TutorialService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.time.Instant;
import com.web_tutorial.javabackend.service.security.SecurityService;

@Service
public class TutorialServiceImpl implements TutorialService {

    private final TutorialRepository tutorialRepository;
    private final CategoryRepository categoryRepository;

    public TutorialServiceImpl(TutorialRepository tutorialRepository, CategoryRepository categoryRepository) {
        this.tutorialRepository = tutorialRepository;
        this.categoryRepository = categoryRepository;
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
        String currentUser = SecurityService.getCurrentUserLogin().orElse("System");
        tutorial.setCreateBy(currentUser);
        tutorial.setCreatedAt(Instant.now());

        if (tutorial.getCategory() != null && tutorial.getCategory().getName() != null) {
            String catName = tutorial.getCategory().getName();
            Category category = categoryRepository.findByName(catName).orElseGet(() -> {
                Category newCat = new Category();
                newCat.setName(catName);
                newCat.setSlug(catName.toLowerCase().replace(" ", "-"));
                return categoryRepository.save(newCat);
            });
            tutorial.setCategory(category);
        }

        return this.tutorialRepository.save(tutorial);
    }

    @Override
    public Tutorial updateTutorial(Long id, Tutorial tutorialDetails) {
        return this.tutorialRepository.findById(id).map(tutorial -> {
            if (tutorialDetails.getTitle() != null)
                tutorial.setTitle(tutorialDetails.getTitle());
            if (tutorialDetails.getSlug() != null)
                tutorial.setSlug(tutorialDetails.getSlug());
            if (tutorialDetails.getDescription() != null)
                tutorial.setDescription(tutorialDetails.getDescription());
            if (tutorialDetails.getContent() != null)
                tutorial.setContent(tutorialDetails.getContent());
            if (tutorialDetails.getCoverImage() != null)
                tutorial.setCoverImage(tutorialDetails.getCoverImage());
            if (tutorialDetails.getStatus() != null)
                tutorial.setStatus(tutorialDetails.getStatus());

            if (tutorialDetails.getCategory() != null && tutorialDetails.getCategory().getName() != null) {
                String catName = tutorialDetails.getCategory().getName();
                Category category = categoryRepository.findByName(catName).orElseGet(() -> {
                    Category newCat = new Category();
                    newCat.setName(catName);
                    newCat.setSlug(catName.toLowerCase().replace(" ", "-"));
                    return categoryRepository.save(newCat);
                });
                tutorial.setCategory(category);
            }

            return this.tutorialRepository.save(tutorial);
        }).orElseThrow(() -> new RuntimeException("Tutorial not found with id " + id));
    }

    @Override
    public void deleteTutorial(Long id) {
        this.tutorialRepository.deleteById(id);
    }
}
