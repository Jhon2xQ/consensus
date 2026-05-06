package com.carmenio.consensus.application.use_case.team;

import com.carmenio.consensus.application.dto.team.TeamResponse;
import com.carmenio.consensus.domain.entity.Team;
import com.carmenio.consensus.domain.exception.TeamException;
import com.carmenio.consensus.domain.repository.TeamRepository;
import com.carmenio.consensus.infrastructure.mapper.TeamMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateTeamUseCaseTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMapper mapper;

    @InjectMocks
    private UpdateTeamUseCase useCase;

    @Test
    @DisplayName("Should update team name successfully")
    void shouldUpdateTeamName() {
        var id = UUID.randomUUID();
        var processId = UUID.randomUUID();
        var existingTeam = Team.builder()
                .id(id)
                .name("Old Name")
                .electoralProcessId(processId)
                .build();

        var expectedResponse = TeamResponse.builder()
                .id(id)
                .name("New Name")
                .electoralProcessId(processId)
                .build();

        when(teamRepository.findById(id)).thenReturn(Optional.of(existingTeam));
        when(teamRepository.existsByElectoralProcessIdAndName(processId, "New Name"))
                .thenReturn(false);
        when(teamRepository.save(existingTeam)).thenReturn(existingTeam);
        when(mapper.toResponse(existingTeam)).thenReturn(expectedResponse);

        var result = useCase.execute(id, "New Name", null);

        assertNotNull(result);
        assertEquals("New Name", result.getName());
        verify(teamRepository).findById(id);
        verify(teamRepository).existsByElectoralProcessIdAndName(processId, "New Name");
        verify(teamRepository).save(existingTeam);
        verify(mapper).toResponse(existingTeam);
    }

    @Test
    @DisplayName("Should update team avatarUrl successfully")
    void shouldUpdateAvatarUrl() {
        var id = UUID.randomUUID();
        var processId = UUID.randomUUID();
        var existingTeam = Team.builder()
                .id(id)
                .name("Team Alpha")
                .electoralProcessId(processId)
                .build();

        var expectedResponse = TeamResponse.builder()
                .id(id)
                .name("Team Alpha")
                .avatarUrl("https://avatar.example.com/new.png")
                .electoralProcessId(processId)
                .build();

        when(teamRepository.findById(id)).thenReturn(Optional.of(existingTeam));
        when(teamRepository.save(existingTeam)).thenReturn(existingTeam);
        when(mapper.toResponse(existingTeam)).thenReturn(expectedResponse);

        var result = useCase.execute(id, null, "https://avatar.example.com/new.png");

        assertNotNull(result);
        assertEquals("https://avatar.example.com/new.png", result.getAvatarUrl());
        verify(teamRepository).save(existingTeam);
    }

    @Test
    @DisplayName("Should throw 404 when team not found")
    void shouldThrow404WhenNotFound() {
        var id = UUID.randomUUID();

        when(teamRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(TeamException.class,
                () -> useCase.execute(id, "New Name", null));
        verify(teamRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw 409 when new name conflicts with existing team")
    void shouldThrow409WhenNameConflict() {
        var id = UUID.randomUUID();
        var processId = UUID.randomUUID();
        var existingTeam = Team.builder()
                .id(id)
                .name("Old Name")
                .electoralProcessId(processId)
                .build();

        when(teamRepository.findById(id)).thenReturn(Optional.of(existingTeam));
        when(teamRepository.existsByElectoralProcessIdAndName(processId, "New Name"))
                .thenReturn(true);

        var exception = assertThrows(TeamException.class,
                () -> useCase.execute(id, "New Name", null));

        assertTrue(exception.getMessage().contains("already exists"));
        verify(teamRepository, never()).save(any());
    }
}
