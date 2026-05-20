package com.carmenio.consensus.presentation.controller;

import com.carmenio.consensus.application.dto.electoral_process.CreateElectoralProcessRequest;
import com.carmenio.consensus.application.dto.electoral_process.ElectoralProcessResponse;
import com.carmenio.consensus.application.dto.electoral_process.UpdateElectoralProcessRequest;
import com.carmenio.consensus.application.use_case.electoral_process.CreateElectoralProcessUseCase;
import com.carmenio.consensus.application.use_case.electoral_process.DeleteElectoralProcessUseCase;
import com.carmenio.consensus.application.use_case.electoral_process.UpdateElectoralProcessUseCase;
import com.carmenio.consensus.presentation.middleware.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Protected mutation endpoints for electoral processes.
 * <p>
 * Requires {@code creator} role — authenticated via Logto JWT.
 * All endpoints return a standardized {@link ApiResponse} wrapper.
 */
@RestController
@RequestMapping("/private/processes")
@RequiredArgsConstructor
public class ElectoralProcessPrivateController {

    private final CreateElectoralProcessUseCase createUseCase;
    private final UpdateElectoralProcessUseCase updateUseCase;
    private final DeleteElectoralProcessUseCase deleteUseCase;

    /**
     * Creates a new electoral process.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ElectoralProcessResponse>> create(
            @Valid @RequestBody CreateElectoralProcessRequest request) {
        var response = createUseCase.execute(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Updates an existing electoral process.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ElectoralProcessResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateElectoralProcessRequest request) {
        var response = updateUseCase.execute(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Deletes an electoral process.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        deleteUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success("Process deleted successfully", null));
    }
}
