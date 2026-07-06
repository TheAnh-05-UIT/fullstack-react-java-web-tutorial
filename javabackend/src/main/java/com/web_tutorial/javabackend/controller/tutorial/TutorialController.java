package com.web_tutorial.javabackend.controller.tutorial;

import com.web_tutorial.javabackend.domain.dto.request.tutorial.CreateTutorialRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.tutorial.UpdateTutorialRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.tutorial.TutorialResponseDTO;
import com.web_tutorial.javabackend.domain.tutorial.Tutorial;
import com.web_tutorial.javabackend.exception.ResourceNotFoundException;
import com.web_tutorial.javabackend.mapper.MapperUtils;
import com.web_tutorial.javabackend.service.tutorial.TutorialService;
import com.web_tutorial.javabackend.util.annotation.ApiMessage;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import com.web_tutorial.javabackend.domain.dto.response.ResultPaginationDTO;

import java.util.Optional;

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
        Optional<Tutorial> tutorialById = this.tutorialService.getTutorialById(id);
        if (!tutorialById.isPresent()) {
            // ResourceNotFoundException → 404 NOT FOUND
            throw new ResourceNotFoundException("Tutorial with Id " + id + " does not exist");
        }
        Tutorial tutorial = tutorialById.get();
        this.tutorialService.incrementViewCount(tutorial.getId());
        tutorial.setViews((tutorial.getViews() == null ? 0L : tutorial.getViews()) + 1);
        TutorialResponseDTO tutorialResponseDTO = MapperUtils.toTutorialResponseDTO(tutorial);
        if (tutorialResponseDTO.getCreateBy() != null) {
            tutorialResponseDTO
                    .setAuthorName(this.tutorialService.getAuthorNameByEmail(tutorialResponseDTO.getCreateBy()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(tutorialResponseDTO);
    }

    @GetMapping("/slug/{slug}")
    @ApiMessage("Get Tutorial by Slug")
    public ResponseEntity<TutorialResponseDTO> getTutorialBySlug(@PathVariable String slug)
            throws ResourceNotFoundException {
        Optional<Tutorial> tutorialBySlug = this.tutorialService.getTutorialBySlug(slug);
        if (!tutorialBySlug.isPresent()) {
            throw new ResourceNotFoundException("Tutorial with slug " + slug + " does not exist");
        }
        Tutorial tutorial = tutorialBySlug.get();
        this.tutorialService.incrementViewCount(tutorial.getId());
        tutorial.setViews((tutorial.getViews() == null ? 0L : tutorial.getViews()) + 1);
        TutorialResponseDTO tutorialResponseDTO = MapperUtils.toTutorialResponseDTO(tutorial);
        if (tutorialResponseDTO.getCreateBy() != null) {
            tutorialResponseDTO
                    .setAuthorName(this.tutorialService.getAuthorNameByEmail(tutorialResponseDTO.getCreateBy()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(tutorialResponseDTO);
    }

    @PostMapping
    @ApiMessage("Create a Tutorial")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<TutorialResponseDTO> createTutorial(
            @RequestBody @Valid CreateTutorialRequestDTO requestDTO) {
        Tutorial tutorial = MapperUtils.toTutorial(requestDTO);
        Tutorial createdTutorial = this.tutorialService.createTutorial(tutorial);
        TutorialResponseDTO tutorialResponseDTO = MapperUtils.toTutorialResponseDTO(createdTutorial);
        if (tutorialResponseDTO.getCreateBy() != null) {
            tutorialResponseDTO
                    .setAuthorName(this.tutorialService.getAuthorNameByEmail(tutorialResponseDTO.getCreateBy()));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(tutorialResponseDTO);
    }

    @PutMapping("/{id}")
    @ApiMessage("Update a Tutorial")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<TutorialResponseDTO> updateTutorial(
            @PathVariable Long id,
            @RequestBody @Valid UpdateTutorialRequestDTO requestDTO) {
        // Bỏ double findById – service tự kiểm tra và throw exception nếu không tìm
        // thấy
        Tutorial tutorialDetails = new Tutorial();
        MapperUtils.updateTutorialFromDTO(requestDTO, tutorialDetails);
        Tutorial updatedTutorial = this.tutorialService.updateTutorial(id, tutorialDetails);
        TutorialResponseDTO tutorialResponseDTO = MapperUtils.toTutorialResponseDTO(updatedTutorial);
        if (tutorialResponseDTO.getCreateBy() != null) {
            tutorialResponseDTO
                    .setAuthorName(this.tutorialService.getAuthorNameByEmail(tutorialResponseDTO.getCreateBy()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(tutorialResponseDTO);
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete a Tutorial")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteTutorial(@PathVariable Long id) {
        // Kiểm tra tồn tại trước khi xóa
        if (this.tutorialService.getTutorialById(id).isEmpty()) {
            // ResourceNotFoundException → 404 NOT FOUND
            throw new ResourceNotFoundException("Tutorial with Id " + id + " does not exist");
        }
        // Service sẽ thực hiện soft delete
        this.tutorialService.deleteTutorial(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
