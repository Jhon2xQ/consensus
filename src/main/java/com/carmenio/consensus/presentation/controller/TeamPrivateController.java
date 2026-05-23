package com.carmenio.consensus.presentation.controller;

import com.carmenio.consensus.application.dto.team.CreateTeamRequest;
import com.carmenio.consensus.application.dto.team.TeamResponse;
import com.carmenio.consensus.application.dto.team.UpdateTeamRequest;
import com.carmenio.consensus.application.use_case.team.CreateTeamUseCase;
import com.carmenio.consensus.application.use_case.team.DeleteTeamUseCase;
import com.carmenio.consensus.application.use_case.team.UpdateTeamUseCase;
import com.carmenio.consensus.presentation.middleware.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    private final CreateTeamUseCase createTeamUseCase;
    private final UpdateTeamUseCase updateTeamUseCase;
    private final DeleteTeamUseCase deleteTeamUseCase;

    /**
     * Creates a new team within an electoral process.
     */
    @PostMapping("/processes/{processId}/teams")
    public ResponseEntity<ApiResponse<TeamResponse>> create(
            @PathVariable UUID processId,
            @Valid @RequestBody CreateTeamRequest request) {
        var response = createTeamUseCase.execute(processId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
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
