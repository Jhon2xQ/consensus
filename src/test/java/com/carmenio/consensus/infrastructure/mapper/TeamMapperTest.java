package com.carmenio.consensus.infrastructure.mapper;

import com.carmenio.consensus.application.dto.team.CreateTeamRequest;
import com.carmenio.consensus.domain.entity.Team;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TeamMapper}.
 */
class TeamMapperTest {

    private final TeamMapper mapper = new TeamMapper();

    @Test
    @DisplayName("Should map from CreateTeamRequest to entity")
    void shouldMapCreateRequestToEntity() {
        var processId = UUID.randomUUID();
        var request = CreateTeamRequest.builder()
                .name("Team Alpha")
                .avatarUrl("https://avatar.example.com/alpha.png")
                .build();

        var entity = mapper.toEntity(request, processId);

        assertNull(entity.getId(), "Entity ID should be null until persisted");
        assertEquals("Team Alpha", entity.getName());
        assertEquals("https://avatar.example.com/alpha.png", entity.getAvatarUrl());
        assertEquals(processId, entity.getElectoralProcessId());
    }

    @Test
    @DisplayName("Should map from entity to TeamResponse")
    void shouldMapEntityToResponse() {
        var id = UUID.randomUUID();
        var processId = UUID.randomUUID();
        var entity = Team.builder()
                .id(id)
                .name("Team Beta")
                .avatarUrl("https://avatar.example.com/beta.png")
                .electoralProcessId(processId)
                .build();

        var response = mapper.toResponse(entity);

        assertEquals(id, response.getId());
        assertEquals("Team Beta", response.getName());
        assertEquals("https://avatar.example.com/beta.png", response.getAvatarUrl());
        assertEquals(processId, response.getElectoralProcessId());
    }

    @Test
    @DisplayName("Should map entity with null avatarUrl to response")
    void shouldMapEntityWithNullAvatar() {
        var id = UUID.randomUUID();
        var processId = UUID.randomUUID();
        var entity = Team.builder()
                .id(id)
                .name("Team Gamma")
                .avatarUrl(null)
                .electoralProcessId(processId)
                .build();

        var response = mapper.toResponse(entity);

        assertNull(response.getAvatarUrl());
    }
}
