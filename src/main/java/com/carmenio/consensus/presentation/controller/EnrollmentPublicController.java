package com.carmenio.consensus.presentation.controller;

import com.carmenio.consensus.application.dto.enrollment.EnrollmentStatsResponse;
import com.carmenio.consensus.application.use_case.enrollment.GetEnrollmentStatsUseCase;
import com.carmenio.consensus.presentation.middleware.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Public read-only endpoints for enrollment statistics.
 * <p>
 * No authentication required — open access for all visitors.
 * All endpoints return a standardized {@link ApiResponse} wrapper.
 */
@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class EnrollmentPublicController {

    private final GetEnrollmentStatsUseCase getEnrollmentStatsUseCase;

    /**
     * Returns aggregate enrollment statistics for an electoral process.
     * <p>
     * Includes total enrollments, participants (claimed), commitments submitted,
     * and votes cast. Does not expose individual voter data.
     */
    @GetMapping("/processes/{processId}/enrollments")
    public ResponseEntity<ApiResponse<EnrollmentStatsResponse>> getStats(
            @PathVariable UUID processId) {
        var response = getEnrollmentStatsUseCase.execute(processId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
