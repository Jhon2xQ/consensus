package com.carmenio.consensus.presentation.controller;

import com.carmenio.consensus.application.dto.record.CreateVoteRecordRequest;
import com.carmenio.consensus.application.dto.record.VoteRecordResponse;
import com.carmenio.consensus.application.use_case.record.CreateVoteRecordUseCase;
import com.carmenio.consensus.application.use_case.record.ListVoteRecordsUseCase;
import com.carmenio.consensus.presentation.middleware.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for vote record ingestion and listing.
 * <p>
 * POST records are consumed by the Semaphore Relayer after on-chain proof
 * validation (exempt from authentication).
 * GET listing requires any authenticated user.
 */
@RestController
@RequestMapping("/private")
@RequiredArgsConstructor
public class RecordController {

    private final CreateVoteRecordUseCase createVoteRecordUseCase;
    private final ListVoteRecordsUseCase listVoteRecordsUseCase;

    /**
     * Ingests a validated vote record from the Semaphore Relayer.
     *
     * @param request the vote record payload
     * @return the created (or existing, if idempotent) record
     */
    @PostMapping("/records")
    public ResponseEntity<ApiResponse<VoteRecordResponse>> createRecord(
            @Valid @RequestBody CreateVoteRecordRequest request) {
        var response = createVoteRecordUseCase.execute(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Lists vote records, optionally filtered by scope.
     *
     * @param scope    optional scope filter (triggers non-paginated full listing)
     * @param pageable pagination parameters (ignored when scope is present)
     * @return paginated or full list of vote records wrapped in ApiResponse
     */
    @GetMapping("/records")
    public ResponseEntity<ApiResponse<?>> listRecords(
            @RequestParam(required = false) String scope,
            Pageable pageable) {
        if (scope != null && !scope.isBlank()) {
            return ResponseEntity.ok(ApiResponse.success(
                    listVoteRecordsUseCase.executeByScope(scope)));
        }
        return ResponseEntity.ok(ApiResponse.success(
                listVoteRecordsUseCase.executePaginated(pageable)));
    }
}
