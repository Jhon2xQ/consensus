package com.carmenio.consensus.presentation.controller;

import com.carmenio.consensus.application.dto.team.TeamResponse;
import com.carmenio.consensus.application.use_case.team.FindTeamByIdUseCase;
import com.carmenio.consensus.application.use_case.team.ListTeamsByProcessUseCase;
import com.carmenio.consensus.presentation.middleware.ApiResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Public read-only endpoints for teams.
 * <p>
 * No authentication required — open access for all visitors.
 * All endpoints return a standardized {@link ApiResponse} wrapper.
 */
@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class TeamPublicController {

    private final ListTeamsByProcessUseCase listTeamsUseCase;
    private final FindTeamByIdUseCase findTeamByIdUseCase;

    /**
     * Lists all teams for an electoral process.
     */
    @GetMapping("/processes/{processId}/teams")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> listByProcess(
            @PathVariable UUID processId) {
        var response = listTeamsUseCase.execute(processId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Finds a team by its ID.
     */
    @GetMapping("/teams/{id}")
    public ResponseEntity<ApiResponse<TeamResponse>> findById(
            @PathVariable UUID id) {
        var response = findTeamByIdUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
