package com.web_tutorial.javabackend.controller.tutorial;

import com.web_tutorial.javabackend.model.tutorial.Tutorial;
import com.web_tutorial.javabackend.service.tutorial.TutorialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tutorials")
public class TutorialController {

    private final TutorialService tutorialService;

    public TutorialController(TutorialService tutorialService) {
        this.tutorialService = tutorialService;
    }

    @GetMapping
    public ResponseEntity<List<Tutorial>> getAllTutorials() {
        return ResponseEntity.status(HttpStatus.OK).body(tutorialService.getAllTutorials());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tutorial> getTutorialById(@PathVariable Long id) {
        return tutorialService.getTutorialById(id)
                .map(tutorial -> ResponseEntity.status(HttpStatus.OK).body(tutorial))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Tutorial> getTutorialBySlug(@PathVariable String slug) {
        return tutorialService.getTutorialBySlug(slug)
                .map(tutorial -> ResponseEntity.status(HttpStatus.OK).body(tutorial))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @PostMapping
    public ResponseEntity<Tutorial> createTutorial(@RequestBody Tutorial tutorial) {
        Tutorial createdTutorial = tutorialService.createTutorial(tutorial);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTutorial);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tutorial> updateTutorial(@PathVariable Long id, @RequestBody Tutorial tutorialDetails) {
        try {
            Tutorial updatedTutorial = tutorialService.updateTutorial(id, tutorialDetails);
            return ResponseEntity.status(HttpStatus.OK).body(updatedTutorial);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTutorial(@PathVariable Long id) {
        tutorialService.deleteTutorial(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
