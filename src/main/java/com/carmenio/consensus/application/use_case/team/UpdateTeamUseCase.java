package com.carmenio.consensus.application.use_case.team;

import com.carmenio.consensus.application.dto.team.TeamResponse;
import com.carmenio.consensus.domain.exception.TeamException;
import com.carmenio.consensus.domain.repository.TeamRepository;
import com.carmenio.consensus.infrastructure.mapper.TeamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Use case for updating an existing team.
 * <p>
 * Validates that the new name does not conflict with another team
 * in the same electoral process.
 */
@Component
@RequiredArgsConstructor
public class UpdateTeamUseCase {

    private final TeamRepository teamRepository;
    private final TeamMapper mapper;

    /**
     * Updates a team's name and/or avatar URL.
     *
     * @param id        the team UUID
     * @param name      the new name (null to keep unchanged)
     * @param avatarUrl the new avatar URL (null to keep unchanged)
     * @return the updated team response DTO
     * @throws TeamException if no team exists or the new name conflicts
     */
    public TeamResponse execute(UUID id, String name, String avatarUrl) {
        var entity = teamRepository.findById(id)
                .orElseThrow(() -> TeamException.notFound(id));

        if (name != null && !name.equals(entity.getName())) {
            var siblings = teamRepository.findByElectoralProcessId(entity.getElectoralProcessId());
            var nameExists = siblings.stream()
                    .anyMatch(t -> !t.getId().equals(id) && t.getName().equals(name));
            if (nameExists) {
                throw TeamException.alreadyExists(name);
            }
            entity.setName(name);
        }

        if (avatarUrl != null) {
            entity.setAvatarUrl(avatarUrl);
        }

        var saved = teamRepository.save(entity);
        return mapper.toResponse(saved);
    }
}
