package com.web_tutorial.javabackend.controller.devops;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.web_tutorial.javabackend.domain.devops.dto.DevopsDTOs;
import com.web_tutorial.javabackend.exception.ResourceNotFoundException;
import com.web_tutorial.javabackend.service.devops.DevopsService;
import com.web_tutorial.javabackend.util.annotation.ApiMessage;
import com.web_tutorial.javabackend.observability.SecurityAuditEvent;
import com.web_tutorial.javabackend.observability.SecurityAuditLogger;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import static com.web_tutorial.javabackend.validation.ApiInputConstraints.SLUG_PATTERN;
import static com.web_tutorial.javabackend.validation.ApiInputConstraints.VARCHAR_MAX;

/**
 * REST API Controller cho module DevOps Lifecycle Content.
 *
 * Base URL: /api/v1/devops
 *
 * Phân quyền:
 * - GET endpoints: Permit All - học viên truy cập để học
 * - POST/PUT/DELETE endpoints: Yêu cầu ROLE_ADMIN mới được thao tác
 * Thêm/Sửa/Xóa
 */
@RestController
@RequestMapping("/api/v1/devops")
@Validated
public class DevopsController {

    private final DevopsService devopsService;
    private final SecurityAuditLogger auditLogger;

    public DevopsController(DevopsService devopsService, SecurityAuditLogger auditLogger) {
        this.devopsService = devopsService;
        this.auditLogger = auditLogger;
    }

    /**
     * GET /api/v1/devops/phases
     */
    @GetMapping("/phases")
    @ApiMessage("Get All Active DevOps Phases")
    public ResponseEntity<List<DevopsDTOs.PhaseResponse>> getActivePhases() {
        List<DevopsDTOs.PhaseResponse> phases = devopsService.getActivePhases();
        return ResponseEntity.status(HttpStatus.OK).body(phases);
    }

    /**
     * GET /api/v1/devops/phases/{phaseKey}
     * GET /api/v1/devops/phases/code
     */
    @GetMapping("/phases/{phaseKey}")
    @ApiMessage("Get Phase Detail with Content by Key")
    public ResponseEntity<DevopsDTOs.PhaseDetailResponse> getPhaseDetailByKey(
            @PathVariable @Size(max = VARCHAR_MAX) @Pattern(regexp = SLUG_PATTERN) String phaseKey) {
        DevopsDTOs.PhaseDetailResponse phaseDetail = devopsService.getPhaseDetailByKey(phaseKey)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy Giai đoạn DevOps với khóa: " + phaseKey));
        return ResponseEntity.status(HttpStatus.OK).body(phaseDetail);
    }

    /**
     * GET /api/v1/devops/admin/phases
     */
    @GetMapping("/admin/phases")
    @ApiMessage("Admin: Get All DevOps Phases Details")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<DevopsDTOs.PhaseDetailResponse>> getAllPhasesForAdmin() {
        List<DevopsDTOs.PhaseDetailResponse> phases = devopsService.getAllPhasesForAdmin();
        return ResponseEntity.status(HttpStatus.OK).body(phases);
    }

    /**
     * POST /api/v1/devops/phases
     */
    @PostMapping("/phases")
    @ApiMessage("Admin: Create DevOps Phase Content")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<DevopsDTOs.PhaseDetailResponse> createPhase(
            @RequestBody @Valid DevopsDTOs.PhaseRequest request) {
        DevopsDTOs.PhaseDetailResponse createdPhase = devopsService.createPhase(request);
        auditLogger.admin(SecurityAuditEvent.ADMIN_CONTENT_CREATED, auditLogger.currentActor(),
                "DEVOPS_PHASE", createdPhase.id(), "CONTENT_CREATED");
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPhase);
    }

    /**
     * PUT /api/v1/devops/phases/{id}
     */
    @PutMapping("/phases/{id}")
    @ApiMessage("Admin: Update DevOps Phase Content")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<DevopsDTOs.PhaseDetailResponse> updatePhase(
            @PathVariable @Positive Long id,
            @RequestBody @Valid DevopsDTOs.PhaseRequest request) {
        DevopsDTOs.PhaseDetailResponse updatedPhase = devopsService.updatePhase(id, request);
        auditLogger.admin(SecurityAuditEvent.ADMIN_CONTENT_UPDATED, auditLogger.currentActor(),
                "DEVOPS_PHASE", id, "CONTENT_UPDATED");
        return ResponseEntity.status(HttpStatus.OK).body(updatedPhase);
    }

    /**
     * Xóa mềm (Soft Delete) một Giai đoạn DevOps khỏi giao diện học viên.
     * DELETE /api/v1/devops/phases/{id}
     */
    @DeleteMapping("/phases/{id}")
    @ApiMessage("Admin: Delete (Soft) DevOps Phase")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deletePhase(@PathVariable @Positive Long id) {
        devopsService.deletePhase(id);
        auditLogger.admin(SecurityAuditEvent.ADMIN_CONTENT_DELETED, auditLogger.currentActor(),
                "DEVOPS_PHASE", id, "CONTENT_DELETED");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
