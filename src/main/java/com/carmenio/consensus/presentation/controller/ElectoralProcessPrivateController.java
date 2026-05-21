package com.carmenio.consensus.presentation.controller;

import com.carmenio.consensus.application.dto.PaginatedResponse;
import com.carmenio.consensus.application.dto.electoral_process.CreateElectoralProcessRequest;
import com.carmenio.consensus.application.dto.electoral_process.ElectoralProcessResponse;
import com.carmenio.consensus.application.dto.electoral_process.UpdateElectoralProcessRequest;
import com.carmenio.consensus.application.use_case.electoral_process.CreateElectoralProcessUseCase;
import com.carmenio.consensus.application.use_case.electoral_process.DeleteElectoralProcessUseCase;
import com.carmenio.consensus.application.use_case.electoral_process.ListProcessesByCreatorUseCase;
import com.carmenio.consensus.application.use_case.electoral_process.UpdateElectoralProcessUseCase;
import com.carmenio.consensus.application.util.JwtClaimExtractor;
import com.carmenio.consensus.presentation.middleware.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
    private final ListProcessesByCreatorUseCase listUseCase;
    private final JwtClaimExtractor jwtClaimExtractor;

    /**
     * Creates a new electoral process.
     * <p>
     * Extracts the creator's user ID from the JWT {@code sub} claim
     * and sets it as {@code createdBy} on the new process entity.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ElectoralProcessResponse>> create(
            @Valid @RequestBody CreateElectoralProcessRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        var createdBy = jwtClaimExtractor.extractUserId(jwt);
        var response = createUseCase.execute(request, createdBy);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Lists electoral processes created by the authenticated user.
     * <p>
     * Filtered by {@code createdBy} matching the JWT {@code sub} claim.
     * Returns a paginated response with fresh computed estatus values.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<ElectoralProcessResponse>>> listMyProcesses(
            @AuthenticationPrincipal Jwt jwt,
            Pageable pageable) {
        var createdBy = jwtClaimExtractor.extractUserId(jwt);
        var response = listUseCase.execute(createdBy, pageable);
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
