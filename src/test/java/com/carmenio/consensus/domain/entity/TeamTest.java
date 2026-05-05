package com.carmenio.consensus.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Team} entity.
 */
class TeamTest {

    @Test
    @DisplayName("Should create Team with builder and verify fields")
    void shouldCreateTeamWithBuilder() {
        var processId = UUID.randomUUID();
        var team = Team.builder()
                .name("Team Alpha")
                .avatarUrl("https://avatar.example.com/alpha.png")
                .electoralProcessId(processId)
                .build();

        assertNotNull(team);
        assertNull(team.getId(), "ID should be null until persisted");
        assertEquals("Team Alpha", team.getName());
        assertEquals("https://avatar.example.com/alpha.png", team.getAvatarUrl());
        assertEquals(processId, team.getElectoralProcessId());
    }

    @Test
    @DisplayName("Should create Team with null avatarUrl")
    void shouldCreateTeamWithNullAvatar() {
        var processId = UUID.randomUUID();
        var team = Team.builder()
                .name("Team Beta")
                .electoralProcessId(processId)
                .build();

        assertNull(team.getAvatarUrl());
        assertEquals("Team Beta", team.getName());
    }

    @Test
    @DisplayName("Should allow setting and getting ID")
    void shouldSetAndGetId() {
        var team = new Team();
        var id = UUID.randomUUID();
        team.setId(id);
        assertEquals(id, team.getId());
    }
}
