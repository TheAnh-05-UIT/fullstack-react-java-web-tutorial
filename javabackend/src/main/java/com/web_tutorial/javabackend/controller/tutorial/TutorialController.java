package com.web_tutorial.javabackend.controller.tutorial;

import com.web_tutorial.javabackend.domain.dto.request.tutorial.CreateTutorialRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.tutorial.UpdateTutorialRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.ResultPaginationDTO;
import com.web_tutorial.javabackend.domain.dto.response.tutorial.TutorialResponseDTO;
import com.web_tutorial.javabackend.service.tutorial.TutorialService;
import com.web_tutorial.javabackend.util.annotation.ApiMessage;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tutorials")
public class TutorialController {

    private final TutorialService tutorialService;

    public TutorialController(TutorialService tutorialService) {
        this.tutorialService = tutorialService;
    }

    @GetMapping
    @ApiMessage("Get All Tutorials")
    public ResponseEntity<ResultPaginationDTO> getAllTutorials(Pageable pageable) {
        ResultPaginationDTO response = this.tutorialService.getAllTutorials(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    @ApiMessage("Get Tutorial by Id")
    public ResponseEntity<TutorialResponseDTO> getTutorialById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(this.tutorialService.getTutorialResponseById(id));
    }

    @GetMapping("/slug/{slug}")
    @ApiMessage("Get Tutorial by Slug")
    public ResponseEntity<TutorialResponseDTO> getTutorialBySlug(@PathVariable String slug) {
        return ResponseEntity.status(HttpStatus.OK).body(this.tutorialService.getTutorialResponseBySlug(slug));
    }

    @PostMapping
    @ApiMessage("Create a Tutorial")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<TutorialResponseDTO> createTutorial(
            @RequestBody @Valid CreateTutorialRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.tutorialService.createTutorialFromDTO(requestDTO));
    }

    @PutMapping("/{id}")
    @ApiMessage("Update a Tutorial")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<TutorialResponseDTO> updateTutorial(
            @PathVariable Long id,
            @RequestBody @Valid UpdateTutorialRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(this.tutorialService.updateTutorialFromDTO(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete a Tutorial")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteTutorial(@PathVariable Long id) {
        this.tutorialService.deleteTutorial(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
