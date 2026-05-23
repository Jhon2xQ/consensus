package com.carmenio.consensus.application.use_case.team;

import com.carmenio.consensus.application.dto.team.CreateTeamRequest;
import com.carmenio.consensus.application.dto.team.TeamResponse;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.domain.exception.TeamException;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.domain.repository.TeamRepository;
import com.carmenio.consensus.infrastructure.mapper.TeamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for creating a new team within an electoral process.
 * <p>
 * Validates that the process exists and that the team name is unique
 * within that process.
 */
@Component
@RequiredArgsConstructor
@Transactional
public class CreateTeamUseCase {

    private final TeamRepository teamRepository;
    private final ElectoralProcessRepository electoralProcessRepository;
    private final TeamMapper mapper;

    /**
     * Creates a new team.
     *
     * @param processId the electoral process to create the team in
     * @param request   the creation payload with name and avatarUrl
     * @return the created team as a response DTO
     * @throws ElectoralProcessException if the process does not exist
     * @throws TeamException             if a team with the same name already exists in the process
     */
    public TeamResponse execute(UUID processId, CreateTeamRequest request) {

        electoralProcessRepository.findById(processId)
                .orElseThrow(() -> ElectoralProcessException.notFound(processId));

        if (teamRepository.existsByElectoralProcessIdAndName(processId, request.getName())) {
            throw TeamException.alreadyExists(request.getName());
        }

        var entity = mapper.toEntity(request, processId);
        var saved = teamRepository.save(entity);
        return mapper.toResponse(saved);
    }
}
