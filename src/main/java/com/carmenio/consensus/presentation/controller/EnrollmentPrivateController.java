package com.carmenio.consensus.presentation.controller;

import com.carmenio.consensus.application.dto.enrollment.ClaimEnrollmentRequest;
import com.carmenio.consensus.application.dto.enrollment.CreateEnrollmentRequest;
import com.carmenio.consensus.application.dto.enrollment.EnrollmentResponse;
import com.carmenio.consensus.application.use_case.enrollment.ClaimEnrollmentUseCase;
import com.carmenio.consensus.application.use_case.enrollment.CreateEnrollmentsBatchUseCase;
import com.carmenio.consensus.application.use_case.enrollment.DeleteEnrollmentUseCase;
import com.carmenio.consensus.application.use_case.enrollment.FindEnrollmentByIdUseCase;
import com.carmenio.consensus.application.use_case.enrollment.ListEnrollmentsByProcessUseCase;
import com.carmenio.consensus.presentation.middleware.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for private enrollment management within electoral processes.
 * <p>
 * Two-phase enrollment flow:
 * <ol>
 *   <li><b>Creator phase</b> — POST registers voter emails (requires {@code consensus-creator} role)</li>
 *   <li><b>User phase</b> — PUT claims an enrollment with JWT email match
 *       and Semaphore commitment (requires {@code consensus-user} role)</li>
 * </ol>
 * GET endpoints require authentication (any role).
 * DELETE requires {@code consensus-creator} role.
 * All endpoints return a standardized {@link ApiResponse} wrapper.
 */
@RestController
@RequestMapping("/private")
@RequiredArgsConstructor
public class EnrollmentPrivateController {

    private final CreateEnrollmentsBatchUseCase createEnrollmentsBatchUseCase;
    private final ClaimEnrollmentUseCase claimEnrollmentUseCase;
    private final ListEnrollmentsByProcessUseCase listEnrollmentsUseCase;
    private final FindEnrollmentByIdUseCase findEnrollmentByIdUseCase;
    private final DeleteEnrollmentUseCase deleteEnrollmentUseCase;

    /**
     * Creates enrollments with email only (creator phase, batch operation).
     * Accepts a JSON array of {@link CreateEnrollmentRequest} objects.
     * All enrollments are created atomically — any validation failure rolls back the entire batch.
     * <p>
     * Requires {@code consensus-creator} role (enforced by SecurityConfig).
     */
    @PostMapping("/processes/{processId}/enrollments")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> create(
            @PathVariable UUID processId,
            @Valid @RequestBody List<CreateEnrollmentRequest> requests) {
        var responses = createEnrollmentsBatchUseCase.execute(processId, requests);
        return ResponseEntity.ok(ApiResponse.success(responses.size() + " enrollments created", responses));
    }

    /**
     * Claims an existing enrollment with JWT email match and Semaphore commitment (user phase).
     * <p>
     * The authenticated user's JWT {@code email} claim must match the enrollment's
     * pre-registered email. The JWT {@code sub} claim becomes the userId, and the
     * provided commitment is validated for uniqueness in the process.
     * <p>
     * Requires {@code consensus-user} role (enforced by SecurityConfig).
     */
    @PutMapping("/enrollments/{id}/commitment")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> claim(
            @PathVariable UUID id,
            @Valid @RequestBody ClaimEnrollmentRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        var response = claimEnrollmentUseCase.execute(id, request, jwt);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Lists all enrollments for an electoral process.
     * <p>
     * Requires authentication (enforced by SecurityConfig).
     */
    @GetMapping("/processes/{processId}/enrollments")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> list(
            @PathVariable UUID processId) {
        var response = listEnrollmentsUseCase.execute(processId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Finds an enrollment by its ID.
     * <p>
     * Requires authentication (enforced by SecurityConfig).
     */
    @GetMapping("/enrollments/{id}")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> findById(
            @PathVariable UUID id) {
        var response = findEnrollmentByIdUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Deletes an enrollment by its ID.
     * <p>
     * Requires {@code consensus-creator} role (enforced by SecurityConfig).
     */
    @DeleteMapping("/enrollments/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        deleteEnrollmentUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
