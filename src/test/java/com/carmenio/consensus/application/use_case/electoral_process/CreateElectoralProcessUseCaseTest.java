package com.carmenio.consensus.application.use_case.electoral_process;

import com.carmenio.consensus.application.dto.electoral_process.CreateElectoralProcessRequest;
import com.carmenio.consensus.application.dto.electoral_process.ElectoralProcessResponse;
import com.carmenio.consensus.application.util.JwtClaimExtractor;
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
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateElectoralProcessUseCaseTest {

    @Mock
    private ElectoralProcessRepository repository;

    @Mock
    private ElectoralProcessMapper mapper;

    @Mock
    private JwtClaimExtractor jwtClaimExtractor;

    @InjectMocks
    private CreateElectoralProcessUseCase useCase;

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
    @DisplayName("should create process when request is valid")
    void shouldCreateProcessWhenRequestIsValid() {
        var now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        var request = CreateElectoralProcessRequest.builder()
                .name("Presidential Election")
                .scope("presidential-2026")
                .commitmentStart(now)
                .commitmentEnd(now.plusSeconds(3600))
                .votingStart(now.plusSeconds(7200))
                .votingEnd(now.plusSeconds(10800))
                .results(now.plusSeconds(14400))
                .build();

        var entity = ElectoralProcess.builder()
                .id(UUID.randomUUID())
                .name("Presidential Election")
                .scope("presidential-2026")
                .description("Presidential election 2026")
                .commitmentStart(now)
                .commitmentEnd(now.plusSeconds(3600))
                .votingStart(now.plusSeconds(7200))
                .votingEnd(now.plusSeconds(10800))
                .results(now.plusSeconds(14400))
                .build();

        when(repository.existsByName("Presidential Election")).thenReturn(false);
        when(repository.existsByScope("presidential-2026")).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(any(), any())).thenReturn(ElectoralProcessResponse.builder()
                .id(entity.getId())
                .name("Presidential Election")
                .scope("presidential-2026")
                .commitmentStart(now)
                .commitmentEnd(now.plusSeconds(3600))
                .votingStart(now.plusSeconds(7200))
                .votingEnd(now.plusSeconds(10800))
                .results(now.plusSeconds(14400))
                .build());

        var jwt = createJwt("user-1");
        when(jwtClaimExtractor.extractUserId(jwt)).thenReturn("user-1");

        var response = useCase.execute(request, jwt);

        assertAll("create process",
                () -> assertNotNull(response),
                () -> assertEquals("Presidential Election", response.getName()),
                () -> assertEquals("presidential-2026", response.getScope())
        );
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("should set createdBy on entity from parameter")
    void shouldSetCreatedByOnEntity() {
        var now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        var request = CreateElectoralProcessRequest.builder()
                .name("Test Process")
                .scope("test-scope")
                .commitmentStart(now)
                .commitmentEnd(now.plusSeconds(3600))
                .votingStart(now.plusSeconds(7200))
                .votingEnd(now.plusSeconds(10800))
                .results(now.plusSeconds(14400))
                .build();

        var entity = ElectoralProcess.builder()
                .name("Test Process")
                .scope("test-scope")
                .commitmentStart(now)
                .commitmentEnd(now.plusSeconds(3600))
                .votingStart(now.plusSeconds(7200))
                .votingEnd(now.plusSeconds(10800))
                .results(now.plusSeconds(14400))
                .build();

        when(repository.existsByName("Test Process")).thenReturn(false);
        when(repository.existsByScope("test-scope")).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(any(), any())).thenReturn(ElectoralProcessResponse.builder()
                .id(UUID.randomUUID())
                .name("Test Process")
                .scope("test-scope")
                .createdBy("creator-abc")
                .build());

        var jwt = createJwt("creator-abc");
        when(jwtClaimExtractor.extractUserId(jwt)).thenReturn("creator-abc");

        var response = useCase.execute(request, jwt);

        assertAll("set createdBy",
                () -> assertNotNull(response),
                () -> assertEquals("creator-abc", entity.getCreatedBy(),
                        "Entity createdBy should be set from parameter"),
                () -> assertEquals("creator-abc", response.getCreatedBy(),
                        "Response createdBy should match")
        );
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("should throw when name already exists")
    void shouldThrowWhenNameAlreadyExists() {
        var now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        var request = CreateElectoralProcessRequest.builder()
                .name("Existing Process")
                .scope("unique-scope")
                .commitmentStart(now)
                .commitmentEnd(now.plusSeconds(3600))
                .votingStart(now.plusSeconds(7200))
                .votingEnd(now.plusSeconds(10800))
                .results(now.plusSeconds(14400))
                .build();

        when(repository.existsByName("Existing Process")).thenReturn(true);

        var jwt = createJwt("user-1");
        var exception = assertThrows(ElectoralProcessException.class, () -> useCase.execute(request, jwt));
        assertTrue(exception.getMessage().contains("already exists"));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("should throw when scope already exists")
    void shouldThrowWhenScopeAlreadyExists() {
        var now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        var request = CreateElectoralProcessRequest.builder()
                .name("Unique Name")
                .scope("existing-scope")
                .commitmentStart(now)
                .commitmentEnd(now.plusSeconds(3600))
                .votingStart(now.plusSeconds(7200))
                .votingEnd(now.plusSeconds(10800))
                .results(now.plusSeconds(14400))
                .build();

        when(repository.existsByName("Unique Name")).thenReturn(false);
        when(repository.existsByScope("existing-scope")).thenReturn(true);

        var jwt = createJwt("user-1");
        var exception = assertThrows(ElectoralProcessException.class, () -> useCase.execute(request, jwt));
        assertTrue(exception.getMessage().contains("already exists"));
        verify(repository, never()).save(any());
    }
}
