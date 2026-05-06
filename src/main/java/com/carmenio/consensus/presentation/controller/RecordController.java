package com.carmenio.consensus.presentation.controller;

import com.carmenio.consensus.application.dto.record.CreateVoteRecordRequest;
import com.carmenio.consensus.application.dto.record.ProcessResultsResponse;
import com.carmenio.consensus.application.dto.record.VoteRecordResponse;
import com.carmenio.consensus.application.use_case.record.CreateVoteRecordUseCase;
import com.carmenio.consensus.application.use_case.record.GetProcessResultsUseCase;
import com.carmenio.consensus.presentation.middleware.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for vote record ingestion and process results.
 * <p>
 * Records are consumed by the Semaphore Relayer after on-chain proof validation.
 * Results are calculated in real-time and only available for CLOSED processes.
 */
@RestController
@RequiredArgsConstructor
public class RecordController {

    private final CreateVoteRecordUseCase createVoteRecordUseCase;
    private final GetProcessResultsUseCase getProcessResultsUseCase;

    /**
     * Ingests a validated vote record from the Semaphore Relayer.
     *
     * @param request the vote record payload
     * @return the created (or existing, if idempotent) record
     */
    @PostMapping("/api/private/records")
    public ResponseEntity<ApiResponse<VoteRecordResponse>> createRecord(
            @Valid @RequestBody CreateVoteRecordRequest request) {
        var response = createVoteRecordUseCase.execute(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Retrieves the results of an electoral process.
     * Results are only available when the process is in CLOSED state.
     *
     * @param id the UUID of the electoral process
     * @return the process results with per-team vote tallies
     */
    @GetMapping("/api/private/processes/{id}/results")
    public ResponseEntity<ApiResponse<ProcessResultsResponse>> getResults(
            @PathVariable UUID id) {
        var response = getProcessResultsUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
