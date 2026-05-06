package com.carmenio.consensus.application.dto.electoral_process;

import com.carmenio.consensus.common.constant.ProcessStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ElectoralProcessResponse} DTO.
 */
class ElectoralProcessResponseTest {

    @Test
    @DisplayName("should include description and estatus fields")
    void shouldIncludeDescriptionAndEstatus() {
        var now = Instant.now();
        var response = ElectoralProcessResponse.builder()
                .id(UUID.randomUUID())
                .name("Test Process")
                .scope("test-scope")
                .description("A test process")
                .estatus(ProcessStatus.PAUSED)
                .commitmentStart(now)
                .commitmentEnd(now.plusSeconds(3600))
                .votingStart(now.plusSeconds(7200))
                .votingEnd(now.plusSeconds(10800))
                .results(now.plusSeconds(14400))
                .build();

        assertAll("response with optional fields",
                () -> assertEquals("A test process", response.getDescription()),
                () -> assertEquals(ProcessStatus.PAUSED, response.getEstatus())
        );
    }

    @Test
    @DisplayName("should default description and estatus to null")
    void shouldDefaultToNull() {
        var now = Instant.now();
        var response = ElectoralProcessResponse.builder()
                .id(UUID.randomUUID())
                .name("Test Process")
                .scope("test-scope")
                .commitmentStart(now)
                .commitmentEnd(now.plusSeconds(3600))
                .votingStart(now.plusSeconds(7200))
                .votingEnd(now.plusSeconds(10800))
                .results(now.plusSeconds(14400))
                .build();

        assertAll("default null",
                () -> assertNull(response.getDescription()),
                () -> assertNull(response.getEstatus())
        );
    }
}
