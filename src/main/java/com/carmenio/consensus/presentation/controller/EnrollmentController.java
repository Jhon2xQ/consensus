package com.carmenio.consensus.presentation.controller;

import com.carmenio.consensus.application.dto.enrollment.CreateEnrollmentRequest;
import com.carmenio.consensus.application.dto.enrollment.EnrollmentResponse;
import com.carmenio.consensus.application.use_case.enrollment.CreateEnrollmentUseCase;
import com.carmenio.consensus.application.use_case.enrollment.FindEnrollmentByIdUseCase;
import com.carmenio.consensus.application.use_case.enrollment.ListEnrollmentsByProcessUseCase;
import com.carmenio.consensus.presentation.middleware.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for enrollment management within electoral processes.
 * <p>
 * Requires {@code user} role — authenticated via Logto JWT.
 * All endpoints return a standardized {@link ApiResponse} wrapper.
 */
@RestController
@RequestMapping("/private")
@RequiredArgsConstructor
public class EnrollmentController {

    private final CreateEnrollmentUseCase createEnrollmentUseCase;
    private final ListEnrollmentsByProcessUseCase listEnrollmentsUseCase;
    private final FindEnrollmentByIdUseCase findEnrollmentByIdUseCase;

    /**
     * Creates a new enrollment within an electoral process.
     */
    @PostMapping("/processes/{processId}/enrollments")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> create(
            @PathVariable UUID processId,
            @Valid @RequestBody CreateEnrollmentRequest request) {
        request.setElectoralProcessId(processId);
        var response = createEnrollmentUseCase.execute(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Lists all enrollments for an electoral process.
     */
    @GetMapping("/processes/{processId}/enrollments")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> list(
            @PathVariable UUID processId) {
        var response = listEnrollmentsUseCase.execute(processId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Finds an enrollment by its ID.
     */
    @GetMapping("/enrollments/{id}")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> findById(
            @PathVariable UUID id) {
        var response = findEnrollmentByIdUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
