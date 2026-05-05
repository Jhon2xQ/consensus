package com.carmenio.consensus.application.use_case.team;

import com.carmenio.consensus.domain.entity.ElectoralProcess;
import com.carmenio.consensus.domain.entity.Team;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.domain.repository.TeamRepository;
import com.carmenio.consensus.infrastructure.mapper.TeamMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListTeamsByProcessUseCaseTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private ElectoralProcessRepository electoralProcessRepository;

    @Mock
    private TeamMapper mapper;

    @InjectMocks
    private ListTeamsByProcessUseCase useCase;

    @Test
    @DisplayName("Should return list of teams for existing process")
    void shouldReturnTeamsForExistingProcess() {
        var processId = UUID.randomUUID();
        var team1 = new Team();
        team1.setName("Team A");
        var team2 = new Team();
        team2.setName("Team B");

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.of(new ElectoralProcess()));
        when(teamRepository.findByElectoralProcessId(processId))
                .thenReturn(List.of(team1, team2));
        when(mapper.toResponse(team1)).thenReturn(null);
        when(mapper.toResponse(team2)).thenReturn(null);

        var result = useCase.execute(processId);

        assertEquals(2, result.size());
        verify(electoralProcessRepository).findById(processId);
        verify(teamRepository).findByElectoralProcessId(processId);
        verify(mapper).toResponse(team1);
        verify(mapper).toResponse(team2);
    }

    @Test
    @DisplayName("Should throw 404 when process does not exist")
    void shouldThrow404WhenProcessNotFound() {
        var processId = UUID.randomUUID();

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.empty());

        assertThrows(ElectoralProcessException.class,
                () -> useCase.execute(processId));

        verify(teamRepository, never()).findByElectoralProcessId(any());
    }

    @Test
    @DisplayName("Should return empty list when process has no teams")
    void shouldReturnEmptyListWhenNoTeams() {
        var processId = UUID.randomUUID();

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.of(new ElectoralProcess()));
        when(teamRepository.findByElectoralProcessId(processId))
                .thenReturn(List.of());

        var result = useCase.execute(processId);

        assertTrue(result.isEmpty());
    }
}
