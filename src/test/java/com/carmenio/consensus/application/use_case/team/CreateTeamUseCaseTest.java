package com.carmenio.consensus.application.use_case.team;

import com.carmenio.consensus.application.dto.team.CreateTeamRequest;
import com.carmenio.consensus.domain.entity.ElectoralProcess;
import com.carmenio.consensus.domain.entity.Team;
import com.carmenio.consensus.application.dto.team.TeamResponse;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.domain.exception.TeamException;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.domain.repository.TeamRepository;
import com.carmenio.consensus.infrastructure.mapper.TeamMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateTeamUseCaseTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private ElectoralProcessRepository electoralProcessRepository;

    @Mock
    private TeamMapper mapper;

    @InjectMocks
    private CreateTeamUseCase useCase;

    @Captor
    private ArgumentCaptor<Team> teamCaptor;

    @Test
    @DisplayName("Should create team when process exists and name is unique")
    void shouldCreateTeamWhenProcessExists() {
        var processId = UUID.randomUUID();
        var request = CreateTeamRequest.builder()
                .name("Team Alpha")
                .avatarUrl("https://avatar.example.com/alpha.png")
                .build();

        var entity = new Team();
        entity.setName("Team Alpha");
        entity.setElectoralProcessId(processId);

        var savedEntity = new Team();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setName("Team Alpha");
        savedEntity.setElectoralProcessId(processId);

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.of(new ElectoralProcess()));
        when(teamRepository.existsByElectoralProcessIdAndName(processId, "Team Alpha"))
                .thenReturn(false);
        var expectedResponse = TeamResponse.builder()
                .id(savedEntity.getId())
                .name("Team Alpha")
                .electoralProcessId(processId)
                .build();

        when(mapper.toEntity(request, processId)).thenReturn(entity);
        when(teamRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toResponse(savedEntity)).thenReturn(expectedResponse);

        var result = useCase.execute(processId, request);

        assertNotNull(result);
        assertEquals("Team Alpha", result.getName());
        assertEquals(processId, result.getElectoralProcessId());
        verify(electoralProcessRepository).findById(processId);
        verify(teamRepository).existsByElectoralProcessIdAndName(processId, "Team Alpha");
        verify(mapper).toEntity(request, processId);
        verify(teamRepository).save(entity);
        verify(mapper).toResponse(savedEntity);
    }

    @Test
    @DisplayName("Should throw 404 when process does not exist")
    void shouldThrow404WhenProcessNotFound() {
        var processId = UUID.randomUUID();
        var request = CreateTeamRequest.builder()
                .name("Team Alpha")
                .build();

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.empty());

        var exception = assertThrows(ElectoralProcessException.class,
                () -> useCase.execute(processId, request));

        assertTrue(exception.getMessage().contains("not found"));
        verify(teamRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw 409 when team name already exists in process")
    void shouldThrow409WhenDuplicateName() {
        var processId = UUID.randomUUID();
        var request = CreateTeamRequest.builder()
                .name("Team Alpha")
                .build();

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.of(new ElectoralProcess()));
        when(teamRepository.existsByElectoralProcessIdAndName(processId, "Team Alpha"))
                .thenReturn(true);

        var exception = assertThrows(TeamException.class,
                () -> useCase.execute(processId, request));

        assertTrue(exception.getMessage().contains("already exists"));
        verify(teamRepository, never()).save(any());
    }
}
