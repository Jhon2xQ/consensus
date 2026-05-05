package com.carmenio.consensus.presentation.controller;

import com.carmenio.consensus.application.dto.team.CreateTeamRequest;
import com.carmenio.consensus.application.dto.team.TeamResponse;
import com.carmenio.consensus.application.dto.team.UpdateTeamRequest;
import com.carmenio.consensus.application.use_case.team.*;
import com.carmenio.consensus.presentation.schema.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for team management within electoral processes.
 * <p>
 * All endpoints return a standardized {@link ApiResponse} wrapper.
 */
@RestController
@RequiredArgsConstructor
public class TeamController {

    private final CreateTeamUseCase createTeamUseCase;
    private final ListTeamsByProcessUseCase listTeamsUseCase;
    private final FindTeamByIdUseCase findTeamByIdUseCase;
    private final UpdateTeamUseCase updateTeamUseCase;
    private final DeleteTeamUseCase deleteTeamUseCase;

    /**
     * Creates a new team within an electoral process.
     */
    @PostMapping("/api/private/processes/{processId}/teams")
    public ResponseEntity<ApiResponse<TeamResponse>> create(
            @PathVariable UUID processId,
            @Valid @RequestBody CreateTeamRequest request) {
        request.setElectoralProcessId(processId);
        var response = createTeamUseCase.execute(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Lists all teams for an electoral process.
     */
    @GetMapping("/api/private/processes/{processId}/teams")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> list(
            @PathVariable UUID processId) {
        var response = listTeamsUseCase.execute(processId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Finds a team by its ID.
     */
    @GetMapping("/api/private/teams/{id}")
    public ResponseEntity<ApiResponse<TeamResponse>> findById(
            @PathVariable UUID id) {
        var response = findTeamByIdUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Updates a team's name and/or avatar URL.
     */
    @PutMapping("/api/private/teams/{id}")
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
    @DeleteMapping("/api/private/teams/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        deleteTeamUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success("Team deleted successfully", null));
    }
}
