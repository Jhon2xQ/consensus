package com.carmenio.consensus.application.use_case.team;

import com.carmenio.consensus.application.dto.team.CreateTeamRequest;
import com.carmenio.consensus.application.dto.team.TeamResponse;
import com.carmenio.consensus.domain.entity.ElectoralProcess;
import com.carmenio.consensus.domain.entity.Team;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.domain.exception.TeamException;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.domain.repository.TeamRepository;
import com.carmenio.consensus.infrastructure.mapper.TeamMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateTeamsBatchUseCaseTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private ElectoralProcessRepository electoralProcessRepository;

    @Mock
    private TeamMapper mapper;

    @InjectMocks
    private CreateTeamsBatchUseCase useCase;

    // --- Happy path ---

    @Test
    @DisplayName("Should create single team when process exists")
    void shouldCreateSingleTeam() {
        var processId = UUID.randomUUID();
        var request = CreateTeamRequest.builder().name("Alpha").build();
        var requests = List.of(request);

        var entity = new Team();
        var savedEntity = new Team();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setName("Alpha");
        savedEntity.setElectoralProcessId(processId);

        var response = TeamResponse.builder()
                .id(savedEntity.getId())
                .name("Alpha")
                .electoralProcessId(processId)
                .build();

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.of(new ElectoralProcess()));
        when(teamRepository.findNamesByProcessIdAndNamesIn(eq(processId), anyList()))
                .thenReturn(Collections.emptyList());
        when(mapper.toEntity(request, processId)).thenReturn(entity);
        when(teamRepository.saveAll(List.of(entity))).thenReturn(List.of(savedEntity));
        when(mapper.toResponse(savedEntity)).thenReturn(response);

        var result = useCase.execute(processId, requests);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Alpha", result.get(0).getName());
        assertEquals(processId, result.get(0).getElectoralProcessId());

        verify(electoralProcessRepository).findById(processId);
        verify(teamRepository).findNamesByProcessIdAndNamesIn(eq(processId), anyList());
        verify(teamRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should create multiple teams successfully")
    void shouldCreateMultipleTeams() {
        var processId = UUID.randomUUID();
        var req1 = CreateTeamRequest.builder().name("Alpha").build();
        var req2 = CreateTeamRequest.builder().name("Beta").build();
        var req3 = CreateTeamRequest.builder().name("Gamma").build();
        var requests = List.of(req1, req2, req3);

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.of(new ElectoralProcess()));
        when(teamRepository.findNamesByProcessIdAndNamesIn(eq(processId), anyList()))
                .thenReturn(Collections.emptyList());

        when(mapper.toEntity(req1, processId)).thenReturn(new Team());
        when(mapper.toEntity(req2, processId)).thenReturn(new Team());
        when(mapper.toEntity(req3, processId)).thenReturn(new Team());

        var saved1 = new Team(); saved1.setId(UUID.randomUUID()); saved1.setName("Alpha");
        var saved2 = new Team(); saved2.setId(UUID.randomUUID()); saved2.setName("Beta");
        var saved3 = new Team(); saved3.setId(UUID.randomUUID()); saved3.setName("Gamma");
        var saved = List.of(saved1, saved2, saved3);

        when(teamRepository.saveAll(anyList())).thenReturn(saved);

        when(mapper.toResponse(saved1)).thenReturn(
                TeamResponse.builder().id(saved1.getId()).name("Alpha").electoralProcessId(processId).build());
        when(mapper.toResponse(saved2)).thenReturn(
                TeamResponse.builder().id(saved2.getId()).name("Beta").electoralProcessId(processId).build());
        when(mapper.toResponse(saved3)).thenReturn(
                TeamResponse.builder().id(saved3.getId()).name("Gamma").electoralProcessId(processId).build());

        var result = useCase.execute(processId, requests);

        assertEquals(3, result.size());
        assertEquals("Alpha", result.get(0).getName());
        assertEquals("Beta", result.get(1).getName());
        assertEquals("Gamma", result.get(2).getName());
        verify(teamRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should reject empty batch with 400")
    void shouldRejectEmptyBatch() {
        var processId = UUID.randomUUID();

        var exception = assertThrows(TeamException.class,
                () -> useCase.execute(processId, Collections.emptyList()));

        assertEquals(400, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("At least one team"));
        verify(teamRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should reject null list with 400")
    void shouldRejectNullList() {
        var processId = UUID.randomUUID();

        var exception = assertThrows(TeamException.class,
                () -> useCase.execute(processId, null));

        assertEquals(400, exception.getStatusCode());
        verify(teamRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should detect within-batch duplicate name")
    void shouldRejectWithinBatchDuplicate() {
        var processId = UUID.randomUUID();
        var req1 = CreateTeamRequest.builder().name("Alpha").build();
        var req2 = CreateTeamRequest.builder().name("Alpha").build();
        var requests = List.of(req1, req2);

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.of(new ElectoralProcess()));

        var exception = assertThrows(TeamException.class,
                () -> useCase.execute(processId, requests));

        assertEquals(409, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Alpha"));
        verify(teamRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should reject when process not found")
    void shouldRejectWhenProcessNotFound() {
        var processId = UUID.randomUUID();
        var req = CreateTeamRequest.builder().name("Alpha").build();
        var requests = List.of(req);

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.empty());

        var exception = assertThrows(ElectoralProcessException.class,
                () -> useCase.execute(processId, requests));

        assertTrue(exception.getMessage().contains("not found"));
        verify(teamRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should reject when DB conflict exists")
    void shouldRejectWhenDbConflict() {
        var processId = UUID.randomUUID();
        var req1 = CreateTeamRequest.builder().name("Existing").build();
        var req2 = CreateTeamRequest.builder().name("New").build();
        var requests = List.of(req1, req2);

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.of(new ElectoralProcess()));
        when(teamRepository.findNamesByProcessIdAndNamesIn(eq(processId), anyList()))
                .thenReturn(List.of("Existing"));

        var exception = assertThrows(TeamException.class,
                () -> useCase.execute(processId, requests));

        assertEquals(409, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Existing"));
        verify(teamRepository, never()).saveAll(anyList());
    }
}
