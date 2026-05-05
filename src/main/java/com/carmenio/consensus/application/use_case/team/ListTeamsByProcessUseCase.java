package com.carmenio.consensus.application.use_case.team;

import com.carmenio.consensus.application.dto.team.TeamResponse;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.domain.repository.TeamRepository;
import com.carmenio.consensus.infrastructure.mapper.TeamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Use case for listing all teams of an electoral process.
 * <p>
 * Validates that the process exists before returning teams.
 */
@Component
@RequiredArgsConstructor
public class ListTeamsByProcessUseCase {

    private final TeamRepository teamRepository;
    private final ElectoralProcessRepository electoralProcessRepository;
    private final TeamMapper mapper;

    /**
     * Lists all teams for the given electoral process.
     *
     * @param processId the electoral process UUID
     * @return a list of team response DTOs (possibly empty)
     * @throws ElectoralProcessException if the process does not exist
     */
    public List<TeamResponse> execute(UUID processId) {
        electoralProcessRepository.findById(processId)
                .orElseThrow(() -> ElectoralProcessException.notFound(processId));

        return teamRepository.findByElectoralProcessId(processId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
