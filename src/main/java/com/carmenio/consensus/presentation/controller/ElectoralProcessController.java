package com.carmenio.consensus.presentation.controller;

import com.carmenio.consensus.application.dto.electoral_process.CreateElectoralProcessRequest;
import com.carmenio.consensus.application.dto.electoral_process.ElectoralProcessResponse;
import com.carmenio.consensus.application.dto.electoral_process.PaginatedResponse;
import com.carmenio.consensus.application.dto.electoral_process.ProcessStateResponse;
import com.carmenio.consensus.application.dto.electoral_process.UpdateElectoralProcessRequest;
import com.carmenio.consensus.application.use_case.electoral_process.*;
import com.carmenio.consensus.presentation.middleware.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

/**
 * REST controller for electoral process management.
 * <p>
 * All endpoints return a standardized {@link ApiResponse} wrapper.
 */
@RestController
@RequiredArgsConstructor
public class ElectoralProcessController {

    private final CreateElectoralProcessUseCase createUseCase;
    private final ListElectoralProcessesUseCase listUseCase;
    private final FindElectoralProcessByIdUseCase findByIdUseCase;
    private final UpdateElectoralProcessUseCase updateUseCase;
    private final DeleteElectoralProcessUseCase deleteUseCase;
    private final GetProcessStateUseCase getStateUseCase;

    /**
     * Creates a new electoral process.
     */
    @PostMapping("/api/private/processes")
    public ResponseEntity<ApiResponse<ElectoralProcessResponse>> create(
            @Valid @RequestBody CreateElectoralProcessRequest request) {
        var response = createUseCase.execute(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Lists all electoral processes with pagination.
     */
    @GetMapping("/api/private/processes")
    public ResponseEntity<ApiResponse<PaginatedResponse<ElectoralProcessResponse>>> list(
            Pageable pageable) {
        var response = listUseCase.execute(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Finds an electoral process by its ID.
     */
    @GetMapping("/api/private/processes/{id}")
    public ResponseEntity<ApiResponse<ElectoralProcessResponse>> findById(
            @PathVariable UUID id) {
        var response = findByIdUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Updates an existing electoral process.
     */
    @PutMapping("/api/private/processes/{id}")
    public ResponseEntity<ApiResponse<ElectoralProcessResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateElectoralProcessRequest request) {
        var response = updateUseCase.execute(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Deletes an electoral process.
     */
    @DeleteMapping("/api/private/processes/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        deleteUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success("Process deleted successfully", null));
    }

    /**
     * Returns the real-time state of an electoral process.
     */
    @GetMapping("/api/private/processes/{id}/state")
    public ResponseEntity<ApiResponse<ProcessStateResponse>> getState(
            @PathVariable UUID id) {
        var response = getStateUseCase.execute(id, Instant.now());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
