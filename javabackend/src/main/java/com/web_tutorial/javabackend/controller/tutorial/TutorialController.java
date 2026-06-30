package com.web_tutorial.javabackend.controller.tutorial;

import com.web_tutorial.javabackend.domain.dto.request.tutorial.CreateTutorialRequestDTO;
import com.web_tutorial.javabackend.domain.dto.request.tutorial.UpdateTutorialRequestDTO;
import com.web_tutorial.javabackend.domain.dto.response.tutorial.TutorialResponseDTO;
import com.web_tutorial.javabackend.domain.tutorial.Tutorial;
import com.web_tutorial.javabackend.exception.IdInvalidException;
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

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/tutorials")
public class TutorialController {

    private final TutorialService tutorialService;

    // Bỏ UserRepository, controller chỉ giao tiếp với Service
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
    public ResponseEntity<TutorialResponseDTO> getTutorialById(@PathVariable Long id) throws IdInvalidException {
        Optional<Tutorial> tutorialById = this.tutorialService.getTutorialById(id);
        if (!tutorialById.isPresent()) {
            throw new IdInvalidException("Tutorial with Id " + id + " does not exist");
        }
        this.tutorialService.incrementViewCount(tutorialById.get().getId());
        TutorialResponseDTO tutorialResponseDTO = MapperUtils.toTutorialResponseDTO(tutorialById.get());
        tutorialResponseDTO.setViewCount((tutorialResponseDTO.getViewCount() == null ? 0L : tutorialResponseDTO.getViewCount()) + 1);
        if (tutorialResponseDTO.getCreateBy() != null) {
            tutorialResponseDTO.setAuthorName(this.tutorialService.getAuthorNameByEmail(tutorialResponseDTO.getCreateBy()));
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
        this.tutorialService.incrementViewCount(tutorialBySlug.get().getId());
        TutorialResponseDTO tutorialResponseDTO = MapperUtils.toTutorialResponseDTO(tutorialBySlug.get());
        tutorialResponseDTO.setViewCount((tutorialResponseDTO.getViewCount() == null ? 0L : tutorialResponseDTO.getViewCount()) + 1);
        if (tutorialResponseDTO.getCreateBy() != null) {
            tutorialResponseDTO.setAuthorName(this.tutorialService.getAuthorNameByEmail(tutorialResponseDTO.getCreateBy()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(tutorialResponseDTO);
    }

    @PostMapping
    @ApiMessage("Create a Tutorial")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TutorialResponseDTO> createTutorial(
            @RequestBody @Valid CreateTutorialRequestDTO requestDTO) {
        Tutorial tutorial = MapperUtils.toTutorial(requestDTO);
        Tutorial createdTutorial = this.tutorialService.createTutorial(tutorial);
        TutorialResponseDTO tutorialResponseDTO = MapperUtils.toTutorialResponseDTO(createdTutorial);
        if (tutorialResponseDTO.getCreateBy() != null) {
            tutorialResponseDTO.setAuthorName(this.tutorialService.getAuthorNameByEmail(tutorialResponseDTO.getCreateBy()));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(tutorialResponseDTO);
    }

    @PutMapping("/{id}")
    @ApiMessage("Update a Tutorial")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TutorialResponseDTO> updateTutorial(
            @PathVariable Long id,
            @RequestBody @Valid UpdateTutorialRequestDTO requestDTO) throws IdInvalidException {
        // Bỏ double findById – service tự kiểm tra và throw exception nếu không tìm thấy
        Tutorial tutorialDetails = new Tutorial();
        MapperUtils.updateTutorialFromDTO(requestDTO, tutorialDetails);
        Tutorial updatedTutorial = this.tutorialService.updateTutorial(id, tutorialDetails);
        TutorialResponseDTO tutorialResponseDTO = MapperUtils.toTutorialResponseDTO(updatedTutorial);
        if (tutorialResponseDTO.getCreateBy() != null) {
            tutorialResponseDTO.setAuthorName(this.tutorialService.getAuthorNameByEmail(tutorialResponseDTO.getCreateBy()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(tutorialResponseDTO);
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete a Tutorial")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTutorial(@PathVariable Long id) throws IdInvalidException {
        // Kiểm tra tồn tại trước khi xóa
        if (this.tutorialService.getTutorialById(id).isEmpty()) {
            throw new IdInvalidException("Tutorial with Id " + id + " does not exist");
        }
        // Service sẽ thực hiện soft delete
        this.tutorialService.deleteTutorial(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
