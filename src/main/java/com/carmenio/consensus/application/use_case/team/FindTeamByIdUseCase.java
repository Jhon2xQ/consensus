package com.carmenio.consensus.application.use_case.team;

import com.carmenio.consensus.application.dto.team.TeamResponse;
import com.carmenio.consensus.domain.exception.TeamException;
import com.carmenio.consensus.domain.repository.TeamRepository;
import com.carmenio.consensus.infrastructure.mapper.TeamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Use case for finding a team by its unique identifier.
 */
@Component
@RequiredArgsConstructor
public class FindTeamByIdUseCase {

    private final TeamRepository teamRepository;
    private final TeamMapper mapper;

    /**
     * Finds a team by ID.
     *
     * @param id the team UUID
     * @return the team response DTO
     * @throws TeamException if no team exists with the given ID
     */
    public TeamResponse execute(UUID id) {
        var entity = teamRepository.findById(id)
                .orElseThrow(() -> TeamException.notFound(id));
        return mapper.toResponse(entity);
    }
}
