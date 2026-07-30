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
import org.springframework.validation.annotation.Validated;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Set;

import com.web_tutorial.javabackend.validation.ValidatedPageRequest;

import static com.web_tutorial.javabackend.validation.ApiInputConstraints.SLUG_PATTERN;
import static com.web_tutorial.javabackend.validation.ApiInputConstraints.VARCHAR_MAX;

@RestController
@RequestMapping("/api/v1/tutorials")
@Validated
public class TutorialController {

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("id", "title", "createdAt", "updatedAt", "views", "status");

    private final TutorialService tutorialService;

    public TutorialController(TutorialService tutorialService) {
        this.tutorialService = tutorialService;
    }

    @GetMapping
    @ApiMessage("Get All Tutorials")
    public ResponseEntity<ResultPaginationDTO> getAllTutorials(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            HttpServletRequest request) {
        Pageable pageable = ValidatedPageRequest.of(page, size, request.getParameterValues("sort"),
                ALLOWED_SORT_FIELDS);
        ResultPaginationDTO response = this.tutorialService.getAllTutorials(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Get All Tutorials for Admin")
    public ResponseEntity<ResultPaginationDTO> getAllTutorialsForAdmin(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            HttpServletRequest request) {
        Pageable pageable = ValidatedPageRequest.of(page, size, request.getParameterValues("sort"),
                ALLOWED_SORT_FIELDS);
        return ResponseEntity.ok(this.tutorialService.getAllTutorialsForAdmin(pageable));
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Get Tutorial by Id for Admin")
    public ResponseEntity<TutorialResponseDTO> getTutorialByIdForAdmin(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(this.tutorialService.getTutorialResponseByIdForAdmin(id));
    }

    @GetMapping("/{id}")
    @ApiMessage("Get Tutorial by Id")
    public ResponseEntity<TutorialResponseDTO> getTutorialById(@PathVariable @Positive Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(this.tutorialService.getTutorialResponseById(id));
    }

    @GetMapping("/slug/{slug}")
    @ApiMessage("Get Tutorial by Slug")
    public ResponseEntity<TutorialResponseDTO> getTutorialBySlug(
            @PathVariable
            @Size(max = VARCHAR_MAX)
            @Pattern(regexp = SLUG_PATTERN)
            String slug) {
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
            @PathVariable @Positive Long id,
            @RequestBody @Valid UpdateTutorialRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(this.tutorialService.updateTutorialFromDTO(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete a Tutorial")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteTutorial(@PathVariable @Positive Long id) {
        this.tutorialService.deleteTutorial(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
