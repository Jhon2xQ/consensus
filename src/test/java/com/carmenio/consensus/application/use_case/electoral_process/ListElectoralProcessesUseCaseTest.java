package com.carmenio.consensus.application.use_case.electoral_process;

import com.carmenio.consensus.application.dto.electoral_process.ElectoralProcessResponse;
import com.carmenio.consensus.domain.entity.ElectoralProcess;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.infrastructure.mapper.ElectoralProcessMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListElectoralProcessesUseCaseTest {

    @Mock
    private ElectoralProcessRepository repository;

    @Mock
    private ElectoralProcessMapper mapper;

    @InjectMocks
    private ListElectoralProcessesUseCase useCase;

    @Test
    @DisplayName("should return paginated processes when processes exist")
    void shouldReturnPaginatedProcessesWhenProcessesExist() {
        var pageable = PageRequest.of(0, 10);
        var now = Instant.now();
        var process1 = ElectoralProcess.builder()
                .id(UUID.randomUUID())
                .name("Process A")
                .scope("scope-a")
                .commitmentStart(now)
                .commitmentEnd(now.plusSeconds(3600))
                .votingStart(now.plusSeconds(7200))
                .votingEnd(now.plusSeconds(10800))
                .results(now.plusSeconds(14400))
                .build();
        var process2 = ElectoralProcess.builder()
                .id(UUID.randomUUID())
                .name("Process B")
                .scope("scope-b")
                .commitmentStart(now)
                .commitmentEnd(now.plusSeconds(3600))
                .votingStart(now.plusSeconds(7200))
                .votingEnd(now.plusSeconds(10800))
                .results(now.plusSeconds(14400))
                .build();

        var page = new PageImpl<>(List.of(process1, process2), pageable, 2);
        when(repository.findAll(pageable)).thenReturn(page);
        when(mapper.toResponse(process1)).thenReturn(ElectoralProcessResponse.builder()
                .id(process1.getId()).name("Process A").scope("scope-a").build());
        when(mapper.toResponse(process2)).thenReturn(ElectoralProcessResponse.builder()
                .id(process2.getId()).name("Process B").scope("scope-b").build());

        var result = useCase.execute(pageable);

        assertAll("paginated result",
                () -> assertNotNull(result),
                () -> assertEquals(2, result.getContent().size()),
                () -> assertEquals("Process A", result.getContent().get(0).getName()),
                () -> assertEquals("Process B", result.getContent().get(1).getName()),
                () -> assertEquals(0, result.getPage()),
                () -> assertEquals(10, result.getSize()),
                () -> assertEquals(2, result.getTotalElements()),
                () -> assertEquals(1, result.getTotalPages())
        );
    }

    @Test
    @DisplayName("should return empty page when no processes exist")
    void shouldReturnEmptyPageWhenNoProcessesExist() {
        var pageable = PageRequest.of(0, 10);
        var emptyPage = Page.<ElectoralProcess>empty(pageable);
        when(repository.findAll(pageable)).thenReturn(emptyPage);

        var result = useCase.execute(pageable);

        assertAll("empty result",
                () -> assertNotNull(result),
                () -> assertTrue(result.getContent().isEmpty()),
                () -> assertEquals(0, result.getTotalElements()),
                () -> assertEquals(0, result.getTotalPages())
        );
    }
}
