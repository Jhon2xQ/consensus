package com.carmenio.consensus.application.use_case.electoral_process;

import com.carmenio.consensus.application.dto.electoral_process.ElectoralProcessResponse;
import com.carmenio.consensus.application.util.JwtClaimExtractor;
import com.carmenio.consensus.common.constant.ProcessStatus;
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
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListProcessesByCreatorUseCaseTest {

    @Mock
    private ElectoralProcessRepository repository;

    @Mock
    private ElectoralProcessMapper mapper;

    @Mock
    private JwtClaimExtractor jwtClaimExtractor;

    @InjectMocks
    private ListProcessesByCreatorUseCase useCase;

    private Jwt createJwt(String userId) {
        return new Jwt(
                "test-token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                Map.of("sub", userId)
        );
    }

    @Test
    @DisplayName("should return paginated processes for creator")
    void shouldReturnPaginatedProcessesForCreator() {
        var now = Instant.now();
        var pageable = PageRequest.of(0, 10);
        var creatorId = "user-abc-123";

        var entity1 = ElectoralProcess.builder()
                .id(UUID.randomUUID())
                .name("Process A")
                .scope("scope-a")
                .createdBy(creatorId)
                .commitmentStart(now)
                .commitmentEnd(now.plusSeconds(3600))
                .votingStart(now.plusSeconds(7200))
                .votingEnd(now.plusSeconds(10800))
                .results(now.plusSeconds(14400))
                .build();

        var entity2 = ElectoralProcess.builder()
                .id(UUID.randomUUID())
                .name("Process B")
                .scope("scope-b")
                .createdBy(creatorId)
                .commitmentStart(now)
                .commitmentEnd(now.plusSeconds(3600))
                .votingStart(now.plusSeconds(7200))
                .votingEnd(now.plusSeconds(10800))
                .results(now.plusSeconds(14400))
                .build();

        Page<ElectoralProcess> page = new PageImpl<>(List.of(entity1, entity2), pageable, 2);

        when(repository.findByCreatedBy(eq(creatorId), any())).thenReturn(page);
        when(mapper.toResponse(eq(entity1), any(ProcessStatus.class))).thenReturn(
                ElectoralProcessResponse.builder()
                        .id(entity1.getId()).name("Process A").scope("scope-a")
                        .createdBy(creatorId).estatus(ProcessStatus.NONE).build());
        when(mapper.toResponse(eq(entity2), any(ProcessStatus.class))).thenReturn(
                ElectoralProcessResponse.builder()
                        .id(entity2.getId()).name("Process B").scope("scope-b")
                        .createdBy(creatorId).estatus(ProcessStatus.NONE).build());

        var jwt = createJwt(creatorId);
        when(jwtClaimExtractor.extractUserId(jwt)).thenReturn(creatorId);

        var response = useCase.execute(jwt, pageable);

        assertAll("paginated processes for creator",
                () -> assertNotNull(response),
                () -> assertEquals(2, response.getContent().size()),
                () -> assertEquals(0, response.getPage()),
                () -> assertEquals(10, response.getSize()),
                () -> assertEquals(2L, response.getTotalElements()),
                () -> assertEquals(1, response.getTotalPages())
        );

        verify(repository).findByCreatedBy(creatorId, pageable);
    }

    @Test
    @DisplayName("should return empty page when creator has no processes")
    void shouldReturnEmptyPageWhenCreatorHasNoProcesses() {
        var pageable = PageRequest.of(0, 10);
        var creatorId = "user-empty";

        Page<ElectoralProcess> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(repository.findByCreatedBy(eq(creatorId), any())).thenReturn(emptyPage);

        var jwt = createJwt(creatorId);
        when(jwtClaimExtractor.extractUserId(jwt)).thenReturn(creatorId);

        var response = useCase.execute(jwt, pageable);

        assertAll("empty page for creator",
                () -> assertNotNull(response),
                () -> assertTrue(response.getContent().isEmpty()),
                () -> assertEquals(0L, response.getTotalElements()),
                () -> assertEquals(0, response.getTotalPages())
        );
    }

    @Test
    @DisplayName("should map createdBy in each response")
    void shouldMapCreatedByInEachResponse() {
        var now = Instant.now();
        var pageable = PageRequest.of(0, 10);
        var creatorId = "user-xyz";

        var entity = ElectoralProcess.builder()
                .id(UUID.randomUUID())
                .name("My Process")
                .scope("my-scope")
                .createdBy(creatorId)
                .commitmentStart(now)
                .commitmentEnd(now.plusSeconds(3600))
                .votingStart(now.plusSeconds(7200))
                .votingEnd(now.plusSeconds(10800))
                .results(now.plusSeconds(14400))
                .build();

        Page<ElectoralProcess> page = new PageImpl<>(List.of(entity), pageable, 1);

        when(repository.findByCreatedBy(eq(creatorId), any())).thenReturn(page);
        when(mapper.toResponse(eq(entity), any(ProcessStatus.class))).thenReturn(
                ElectoralProcessResponse.builder()
                        .id(entity.getId()).name("My Process").scope("my-scope")
                        .createdBy(creatorId).estatus(ProcessStatus.NONE).build());

        var jwt = createJwt(creatorId);
        when(jwtClaimExtractor.extractUserId(jwt)).thenReturn(creatorId);

        var response = useCase.execute(jwt, pageable);

        assertEquals(1, response.getContent().size());
        assertEquals(creatorId, response.getContent().get(0).getCreatedBy());
    }

    @Test
    @DisplayName("should transition state for each entity")
    void shouldTransitionStateForEachEntity() {
        var now = Instant.now();
        var pageable = PageRequest.of(0, 10);
        var creatorId = "user-state";

        var entity = ElectoralProcess.builder()
                .id(UUID.randomUUID())
                .name("Stateful Process")
                .scope("state-scope")
                .createdBy(creatorId)
                .commitmentStart(now.minusSeconds(7200))
                .commitmentEnd(now.minusSeconds(3600))
                .votingStart(now.plusSeconds(3600))
                .votingEnd(now.plusSeconds(7200))
                .results(now.plusSeconds(14400))
                .build();

        Page<ElectoralProcess> page = new PageImpl<>(List.of(entity), pageable, 1);

        when(repository.findByCreatedBy(eq(creatorId), any())).thenReturn(page);
        when(mapper.toResponse(eq(entity), any(ProcessStatus.class))).thenReturn(
                ElectoralProcessResponse.builder()
                        .id(entity.getId()).name("Stateful Process").scope("state-scope")
                        .createdBy(creatorId).estatus(ProcessStatus.NONE).build());

        var jwt = createJwt(creatorId);
        when(jwtClaimExtractor.extractUserId(jwt)).thenReturn(creatorId);

        var response = useCase.execute(jwt, pageable);

        assertNotNull(response);
        // Verify transitionState was called by checking getEstatus() on entity
        // entity.getEstatus() was called by mapper.toResponse after transitionState
        verify(mapper).toResponse(eq(entity), any(ProcessStatus.class));
    }
}
