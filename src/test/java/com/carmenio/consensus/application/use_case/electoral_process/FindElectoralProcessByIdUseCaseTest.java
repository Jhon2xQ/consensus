package com.carmenio.consensus.application.use_case.electoral_process;

import com.carmenio.consensus.application.dto.electoral_process.ElectoralProcessResponse;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindElectoralProcessByIdUseCaseTest {

    @Mock
    private ElectoralProcessRepository repository;

    @Mock
    private ElectoralProcessMapper mapper;

    @InjectMocks
    private FindElectoralProcessByIdUseCase useCase;

    @Test
    @DisplayName("should return process when found")
    void shouldReturnProcessWhenFound() {
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

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(any(), any())).thenReturn(ElectoralProcessResponse.builder()
                .id(id).name("Test Process").scope("test-scope").build());

        var result = useCase.execute(id);

        assertAll("found process",
                () -> assertNotNull(result),
                () -> assertEquals(id, result.getId()),
                () -> assertEquals("Test Process", result.getName())
        );
    }

    @Test
    @DisplayName("should throw when process not found")
    void shouldThrowWhenProcessNotFound() {
        var id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ElectoralProcessException.class, () -> useCase.execute(id));
    }
}
