package com.carmenio.consensus.presentation.controller;

import com.carmenio.consensus.application.dto.team.CreateTeamRequest;
import com.carmenio.consensus.application.dto.team.TeamResponse;
import com.carmenio.consensus.application.dto.team.UpdateTeamRequest;
import com.carmenio.consensus.application.use_case.team.CreateTeamsBatchUseCase;
import com.carmenio.consensus.application.use_case.team.DeleteTeamUseCase;
import com.carmenio.consensus.application.use_case.team.UpdateTeamUseCase;
import com.carmenio.consensus.presentation.middleware.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Protected mutation endpoints for teams.
 * <p>
 * Requires {@code creator} role — authenticated via Logto JWT.
 * All endpoints return a standardized {@link ApiResponse} wrapper.
 */
@RestController
@RequestMapping("/private")
@RequiredArgsConstructor
public class TeamPrivateController {

    private final CreateTeamsBatchUseCase createTeamsBatchUseCase;
    private final UpdateTeamUseCase updateTeamUseCase;
    private final DeleteTeamUseCase deleteTeamUseCase;

    /**
     * Creates teams within an electoral process (batch operation).
     * Accepts a JSON array of {@link CreateTeamRequest} objects.
     * All teams are created atomically — any validation failure rolls back the entire batch.
     */
    @PostMapping("/processes/{processId}/teams")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> create(
            @PathVariable UUID processId,
            @Valid @RequestBody List<CreateTeamRequest> requests) {
        var responses = createTeamsBatchUseCase.execute(processId, requests);
        return ResponseEntity.ok(ApiResponse.success(responses.size() + " teams created", responses));
    }

    /**
     * Updates a team's name and/or avatar URL.
     */
    @PutMapping("/teams/{id}")
    public ResponseEntity<ApiResponse<TeamResponse>> update(
            @PathVariable UUID id,
            @RequestBody UpdateTeamRequest request) {
        var response = updateTeamUseCase.execute(
                id, request.getName(), request.getAvatarUrl());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Deletes a team by its ID.
     */
    @DeleteMapping("/teams/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        deleteTeamUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success("Team deleted successfully", null));
    }
}
