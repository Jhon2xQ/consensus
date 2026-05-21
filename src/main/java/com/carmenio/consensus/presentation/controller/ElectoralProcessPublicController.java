package com.carmenio.consensus.presentation.controller;

import com.carmenio.consensus.application.dto.electoral_process.ElectoralProcessResponse;
import com.carmenio.consensus.application.dto.PaginatedResponse;
import com.carmenio.consensus.application.dto.electoral_process.ProcessStateResponse;
import com.carmenio.consensus.application.use_case.electoral_process.FindElectoralProcessByIdUseCase;
import com.carmenio.consensus.application.use_case.electoral_process.GetProcessStateUseCase;
import com.carmenio.consensus.application.use_case.electoral_process.ListElectoralProcessesUseCase;
import com.carmenio.consensus.presentation.middleware.ApiResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Public read-only endpoints for electoral processes.
 * <p>
 * No authentication required — open access for all visitors.
 * All endpoints return a standardized {@link ApiResponse} wrapper.
 */
@RestController
@RequestMapping("/public/processes")
@RequiredArgsConstructor
public class ElectoralProcessPublicController {

    private final ListElectoralProcessesUseCase listUseCase;
    private final FindElectoralProcessByIdUseCase findByIdUseCase;
    private final GetProcessStateUseCase getStateUseCase;

    /**
     * Lists all electoral processes with pagination.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<ElectoralProcessResponse>>> list(
            Pageable pageable) {
        var response = listUseCase.execute(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Finds an electoral process by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ElectoralProcessResponse>> findById(
            @PathVariable UUID id) {
        var response = findByIdUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Returns the real-time state of an electoral process.
     */
    @GetMapping("/{id}/state")
    public ResponseEntity<ApiResponse<ProcessStateResponse>> getState(
            @PathVariable UUID id) {
        var response = getStateUseCase.execute(id, Instant.now());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
