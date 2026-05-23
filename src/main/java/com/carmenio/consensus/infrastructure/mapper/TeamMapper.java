package com.carmenio.consensus.infrastructure.mapper;

import com.carmenio.consensus.application.dto.team.CreateTeamRequest;
import com.carmenio.consensus.application.dto.team.TeamResponse;
import com.carmenio.consensus.domain.entity.Team;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper for {@link Team} entity ↔ DTO conversions.
 * <p>
 * Lives in the infrastructure layer because it knows about JPA entity details.
 * Application and domain layers remain JPA-free.
 */
@Component
public class TeamMapper {

    /**
     * Converts a create request to a new entity (with null ID for JPA generation).
     */
    public Team toEntity(CreateTeamRequest request, UUID processId) {
        return Team.builder()
                .name(request.getName())
                .avatarUrl(request.getAvatarUrl())
                .electoralProcessId(processId)
                .build();
    }

    /**
     * Converts an entity to a response DTO.
     */
    public TeamResponse toResponse(Team entity) {
        return TeamResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .avatarUrl(entity.getAvatarUrl())
                .electoralProcessId(entity.getElectoralProcessId())
                .build();
    }
}
