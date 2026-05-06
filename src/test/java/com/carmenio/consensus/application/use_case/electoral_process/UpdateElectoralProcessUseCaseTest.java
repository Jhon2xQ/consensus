package com.carmenio.consensus.application.use_case.electoral_process;

import com.carmenio.consensus.application.dto.electoral_process.ElectoralProcessResponse;
import com.carmenio.consensus.application.dto.electoral_process.UpdateElectoralProcessRequest;
import com.carmenio.consensus.domain.entity.ElectoralProcess;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.infrastructure.mapper.ElectoralProcessMapper;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateElectoralProcessUseCaseTest {

    @Mock
    private ElectoralProcessRepository repository;

    @Mock
    private ElectoralProcessMapper mapper;

    @InjectMocks
    private UpdateElectoralProcessUseCase useCase;

    @Test
    @DisplayName("should update process when request is valid")
    void shouldUpdateProcessWhenRequestIsValid() {
        var id = UUID.randomUUID();
        var now = Instant.now();
        var existing = ElectoralProcess.builder()
                .id(id)
                .name("Old Name")
                .scope("original-scope")
                .commitmentStart(now)
                .commitmentEnd(now.plusSeconds(3600))
                .votingStart(now.plusSeconds(7200))
                .votingEnd(now.plusSeconds(10800))
                .results(now.plusSeconds(14400))
                .build();

        var request = UpdateElectoralProcessRequest.builder()
                .name("New Name")
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.existsByName("New Name")).thenReturn(false);
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toResponse(any(), any())).thenReturn(ElectoralProcessResponse.builder()
                .id(id).name("New Name").scope("original-scope").build());

        var result = useCase.execute(id, request);

        assertNotNull(result);
        assertEquals("New Name", result.getName());
        verify(mapper).updateEntity(existing, request);
        verify(repository).save(existing);
    }

    @Test
    @DisplayName("should throw when process not found")
    void shouldThrowWhenProcessNotFound() {
        var id = UUID.randomUUID();
        var request = UpdateElectoralProcessRequest.builder().name("New Name").build();

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ElectoralProcessException.class, () -> useCase.execute(id, request));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("should throw when new name conflicts with existing process")
    void shouldThrowWhenNewNameConflictsWithExistingProcess() {
        var id = UUID.randomUUID();
        var now = Instant.now();
        var existing = ElectoralProcess.builder()
                .id(id)
                .name("Old Name")
                .scope("original-scope")
                .commitmentStart(now)
                .commitmentEnd(now.plusSeconds(3600))
                .votingStart(now.plusSeconds(7200))
                .votingEnd(now.plusSeconds(10800))
                .results(now.plusSeconds(14400))
                .build();

        var request = UpdateElectoralProcessRequest.builder()
                .name("Conflicting Name")
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.existsByName("Conflicting Name")).thenReturn(true);

        var exception = assertThrows(ElectoralProcessException.class, () -> useCase.execute(id, request));
        assertTrue(exception.getMessage().contains("already exists"));
        verify(repository, never()).save(any());
    }
}
