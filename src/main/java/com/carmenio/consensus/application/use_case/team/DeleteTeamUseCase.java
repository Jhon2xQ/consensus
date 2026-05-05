package com.carmenio.consensus.application.use_case.team;

import com.carmenio.consensus.domain.exception.TeamException;
import com.carmenio.consensus.domain.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Use case for deleting a team by its unique identifier.
 */
@Component
@RequiredArgsConstructor
public class DeleteTeamUseCase {

    private final TeamRepository teamRepository;

    /**
     * Deletes a team by ID.
     *
     * @param id the team UUID
     * @throws TeamException if no team exists with the given ID
     */
    public void execute(UUID id) {
        var entity = teamRepository.findById(id)
                .orElseThrow(() -> TeamException.notFound(id));
        teamRepository.delete(entity);
    }
}
