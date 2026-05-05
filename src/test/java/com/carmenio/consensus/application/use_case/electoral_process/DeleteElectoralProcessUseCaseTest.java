package com.carmenio.consensus.application.use_case.electoral_process;

import com.carmenio.consensus.domain.entity.ElectoralProcess;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.domain.repository.TeamRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteElectoralProcessUseCaseTest {

    @Mock
    private ElectoralProcessRepository electoralProcessRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private DeleteElectoralProcessUseCase useCase;

    @Test
    @DisplayName("should delete process when no dependencies exist")
    void shouldDeleteProcessWhenNoDependenciesExist() {
        var id = UUID.randomUUID();
        var now = Instant.now();
        var entity = ElectoralProcess.builder()
                .id(id)
                .name("Test Process")
                .scope("test-scope")
                .commitmentStart(now)
                .commitmentEnd(now.plusSeconds(3600))
                .votingStart(now.plusSeconds(7200))
                .votingEnd(now.plusSeconds(10800))
                .results(now.plusSeconds(14400))
                .build();

        when(electoralProcessRepository.findById(id)).thenReturn(Optional.of(entity));
        when(teamRepository.existsByProcessId(id)).thenReturn(false);

        useCase.execute(id);

        verify(electoralProcessRepository).delete(entity);
    }

    @Test
    @DisplayName("should throw when process not found")
    void shouldThrowWhenProcessNotFound() {
        var id = UUID.randomUUID();
        when(electoralProcessRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ElectoralProcessException.class, () -> useCase.execute(id));
        verify(electoralProcessRepository, never()).delete(any());
    }

    @Test
    @DisplayName("should throw when process has teams")
    void shouldThrowWhenProcessHasTeams() {
        var id = UUID.randomUUID();
        var now = Instant.now();
        var entity = ElectoralProcess.builder()
                .id(id)
                .name("Test Process")
                .scope("test-scope")
                .commitmentStart(now)
                .commitmentEnd(now.plusSeconds(3600))
                .votingStart(now.plusSeconds(7200))
                .votingEnd(now.plusSeconds(10800))
                .results(now.plusSeconds(14400))
                .build();

        when(electoralProcessRepository.findById(id)).thenReturn(Optional.of(entity));
        when(teamRepository.existsByProcessId(id)).thenReturn(true);

        var exception = assertThrows(ElectoralProcessException.class, () -> useCase.execute(id));
        assertTrue(exception.getMessage().contains("dependencies"));
        verify(electoralProcessRepository, never()).delete(any());
    }
}
