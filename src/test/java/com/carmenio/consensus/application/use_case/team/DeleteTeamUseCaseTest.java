package com.carmenio.consensus.application.use_case.team;

import com.carmenio.consensus.domain.entity.Team;
import com.carmenio.consensus.domain.exception.TeamException;
import com.carmenio.consensus.domain.repository.TeamRepository;
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
class DeleteTeamUseCaseTest {

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private DeleteTeamUseCase useCase;

    @Test
    @DisplayName("Should delete team when it exists")
    void shouldDeleteTeamWhenFound() {
        var id = UUID.randomUUID();
        var team = new Team();
        team.setId(id);

        when(teamRepository.findById(id)).thenReturn(Optional.of(team));

        useCase.execute(id);

        verify(teamRepository).findById(id);
        verify(teamRepository).delete(team);
    }

    @Test
    @DisplayName("Should throw 404 when team not found")
    void shouldThrow404WhenNotFound() {
        var id = UUID.randomUUID();

        when(teamRepository.findById(id)).thenReturn(Optional.empty());

        var exception = assertThrows(TeamException.class,
                () -> useCase.execute(id));

        assertTrue(exception.getMessage().contains("not found"));
        verify(teamRepository, never()).delete(any());
    }
}
