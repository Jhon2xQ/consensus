package com.carmenio.consensus.domain.entity;

import com.carmenio.consensus.common.constant.ProcessStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ElectoralProcess} entity fields.
 */
class ElectoralProcessTest {

    @Test
    @DisplayName("should persist and retrieve description and estatus")
    void shouldPersistAndRetrieveDescriptionAndEstatus() {
        var now = Instant.now();
        var process = ElectoralProcess.builder()
                .id(UUID.randomUUID())
                .name("Test Process")
                .scope("test-scope")
                .description("A test electoral process")
                .estatus(ProcessStatus.VOTING)
                .commitmentStart(now)
                .commitmentEnd(now.plusSeconds(3600))
                .votingStart(now.plusSeconds(7200))
                .votingEnd(now.plusSeconds(10800))
                .results(now.plusSeconds(14400))
                .build();

        assertAll("entity fields",
                () -> assertEquals("A test electoral process", process.getDescription(),
                        "description should be stored and retrieved"),
                () -> assertEquals(ProcessStatus.VOTING, process.getEstatus(),
                        "estatus should be stored and retrieved")
        );
    }

    @Test
    @DisplayName("should default description to null and estatus to NONE")
    void shouldDefaultDescriptionToNullAndEstatusToNone() {
        var now = Instant.now();
        var process = ElectoralProcess.builder()
                .id(UUID.randomUUID())
                .name("Test Process")
                .scope("test-scope")
                .commitmentStart(now)
                .commitmentEnd(now.plusSeconds(3600))
                .votingStart(now.plusSeconds(7200))
                .votingEnd(now.plusSeconds(10800))
                .results(now.plusSeconds(14400))
                .build();

        assertAll("default values",
                () -> assertNull(process.getDescription(),
                        "description should default to null"),
                () -> assertEquals(ProcessStatus.NONE, process.getEstatus(),
                        "estatus should default to NONE")
        );
    }
}
