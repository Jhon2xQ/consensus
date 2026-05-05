package com.carmenio.consensus.infrastructure.mapper;

import com.carmenio.consensus.application.dto.electoral_process.CreateElectoralProcessRequest;
import com.carmenio.consensus.application.dto.electoral_process.ElectoralProcessResponse;
import com.carmenio.consensus.application.dto.electoral_process.UpdateElectoralProcessRequest;
import com.carmenio.consensus.domain.entity.ElectoralProcess;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ElectoralProcessMapper}.
 * <p>
 * Verifies entity ↔ DTO conversions.
 */
class ElectoralProcessMapperTest {

    private final ElectoralProcessMapper mapper = new ElectoralProcessMapper();

    @Test
    @DisplayName("should map CreateElectoralProcessRequest to entity")
    void shouldMapCreateRequestToEntity() {
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

        var entity = mapper.toEntity(request);

        assertAll("create request → entity",
                () -> assertNull(entity.getId(), "ID should be null before persist"),
                () -> assertEquals("Presidential Election", entity.getName()),
                () -> assertEquals("presidential-2026", entity.getScope()),
                () -> assertEquals(now, entity.getCommitmentStart()),
                () -> assertEquals(now.plusSeconds(3600), entity.getCommitmentEnd()),
                () -> assertEquals(now.plusSeconds(7200), entity.getVotingStart()),
                () -> assertEquals(now.plusSeconds(10800), entity.getVotingEnd()),
                () -> assertEquals(now.plusSeconds(14400), entity.getResults())
        );
    }

    @Test
    @DisplayName("should update entity from UpdateElectoralProcessRequest")
    void shouldUpdateEntityFromUpdateRequest() {
        var original = ElectoralProcess.builder()
                .id(UUID.randomUUID())
                .name("Original Name")
                .scope("original-scope")
                .commitmentStart(Instant.parse("2026-01-01T00:00:00Z"))
                .commitmentEnd(Instant.parse("2026-01-10T00:00:00Z"))
                .votingStart(Instant.parse("2026-02-01T00:00:00Z"))
                .votingEnd(Instant.parse("2026-02-10T00:00:00Z"))
                .results(Instant.parse("2026-03-01T00:00:00Z"))
                .build();

        var update = UpdateElectoralProcessRequest.builder()
                .name("Updated Name")
                .votingStart(Instant.parse("2026-02-15T00:00:00Z"))
                .build();

        mapper.updateEntity(original, update);

        assertAll("partial update",
                () -> assertEquals("Updated Name", original.getName()),
                () -> assertEquals("original-scope", original.getScope(), "scope should not change"),
                () -> assertEquals(Instant.parse("2026-01-01T00:00:00Z"), original.getCommitmentStart(),
                        "commitmentStart should not change"),
                () -> assertEquals(Instant.parse("2026-02-15T00:00:00Z"), original.getVotingStart(),
                        "votingStart should be updated"),
                () -> assertEquals(Instant.parse("2026-02-10T00:00:00Z"), original.getVotingEnd(),
                        "votingEnd should not change")
        );
    }

    @Test
    @DisplayName("should not change entity when update request has all null fields")
    void shouldNotChangeEntityWhenUpdateRequestHasAllNullFields() {
        var original = ElectoralProcess.builder()
                .id(UUID.randomUUID())
                .name("Original Name")
                .scope("original-scope")
                .commitmentStart(Instant.parse("2026-01-01T00:00:00Z"))
                .commitmentEnd(Instant.parse("2026-01-10T00:00:00Z"))
                .votingStart(Instant.parse("2026-02-01T00:00:00Z"))
                .votingEnd(Instant.parse("2026-02-10T00:00:00Z"))
                .results(Instant.parse("2026-03-01T00:00:00Z"))
                .build();

        var update = new UpdateElectoralProcessRequest();

        mapper.updateEntity(original, update);

        assertAll("null update is no-op",
                () -> assertEquals("Original Name", original.getName()),
                () -> assertEquals("original-scope", original.getScope()),
                () -> assertEquals(Instant.parse("2026-01-01T00:00:00Z"), original.getCommitmentStart()),
                () -> assertEquals(Instant.parse("2026-01-10T00:00:00Z"), original.getCommitmentEnd()),
                () -> assertEquals(Instant.parse("2026-02-01T00:00:00Z"), original.getVotingStart()),
                () -> assertEquals(Instant.parse("2026-02-10T00:00:00Z"), original.getVotingEnd()),
                () -> assertEquals(Instant.parse("2026-03-01T00:00:00Z"), original.getResults())
        );
    }

    @Test
    @DisplayName("should update all fields when update request has all non-null values")
    void shouldUpdateAllFieldsWhenUpdateRequestHasAllValues() {
        var original = ElectoralProcess.builder()
                .id(UUID.randomUUID())
                .name("Old Name")
                .scope("old-scope")
                .commitmentStart(Instant.parse("2025-01-01T00:00:00Z"))
                .commitmentEnd(Instant.parse("2025-01-10T00:00:00Z"))
                .votingStart(Instant.parse("2025-02-01T00:00:00Z"))
                .votingEnd(Instant.parse("2025-02-10T00:00:00Z"))
                .results(Instant.parse("2025-03-01T00:00:00Z"))
                .build();

        var update = UpdateElectoralProcessRequest.builder()
                .name("New Name")
                .commitmentStart(Instant.parse("2026-06-01T00:00:00Z"))
                .commitmentEnd(Instant.parse("2026-06-10T00:00:00Z"))
                .votingStart(Instant.parse("2026-07-01T00:00:00Z"))
                .votingEnd(Instant.parse("2026-07-10T00:00:00Z"))
                .results(Instant.parse("2026-08-01T00:00:00Z"))
                .build();

        mapper.updateEntity(original, update);

        assertAll("full update",
                () -> assertEquals("New Name", original.getName()),
                () -> assertEquals("old-scope", original.getScope(), "scope should never change via update"),
                () -> assertEquals(Instant.parse("2026-06-01T00:00:00Z"), original.getCommitmentStart()),
                () -> assertEquals(Instant.parse("2026-06-10T00:00:00Z"), original.getCommitmentEnd()),
                () -> assertEquals(Instant.parse("2026-07-01T00:00:00Z"), original.getVotingStart()),
                () -> assertEquals(Instant.parse("2026-07-10T00:00:00Z"), original.getVotingEnd()),
                () -> assertEquals(Instant.parse("2026-08-01T00:00:00Z"), original.getResults())
        );
    }

    @Test
    @DisplayName("should map entity to response")
    void shouldMapEntityToResponse() {
        var id = UUID.randomUUID();
        var now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
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

        var response = mapper.toResponse(entity);

        assertAll("entity → response",
                () -> assertEquals(id, response.getId()),
                () -> assertEquals("Test Process", response.getName()),
                () -> assertEquals("test-scope", response.getScope()),
                () -> assertEquals(now, response.getCommitmentStart()),
                () -> assertEquals(now.plusSeconds(3600), response.getCommitmentEnd()),
                () -> assertEquals(now.plusSeconds(7200), response.getVotingStart()),
                () -> assertEquals(now.plusSeconds(10800), response.getVotingEnd()),
                () -> assertEquals(now.plusSeconds(14400), response.getResults())
        );
    }
}
