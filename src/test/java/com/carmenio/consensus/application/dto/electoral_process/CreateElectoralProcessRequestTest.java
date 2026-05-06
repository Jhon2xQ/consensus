package com.carmenio.consensus.application.dto.electoral_process;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CreateElectoralProcessRequest} DTO.
 */
class CreateElectoralProcessRequestTest {

    @Test
    @DisplayName("should accept optional description field")
    void shouldAcceptOptionalDescription() {
        var now = Instant.now();
        var request = CreateElectoralProcessRequest.builder()
                .name("Test Process")
                .scope("test-scope")
                .description("A test process")
                .commitmentStart(now)
                .commitmentEnd(now.plusSeconds(3600))
                .votingStart(now.plusSeconds(7200))
                .votingEnd(now.plusSeconds(10800))
                .results(now.plusSeconds(14400))
                .build();

        assertEquals("A test process", request.getDescription());
    }

    @Test
    @DisplayName("should default description to null when not provided")
    void shouldDefaultDescriptionToNull() {
        var now = Instant.now();
        var request = CreateElectoralProcessRequest.builder()
                .name("Test Process")
                .scope("test-scope")
                .commitmentStart(now)
                .commitmentEnd(now.plusSeconds(3600))
                .votingStart(now.plusSeconds(7200))
                .votingEnd(now.plusSeconds(10800))
                .results(now.plusSeconds(14400))
                .build();

        assertNull(request.getDescription());
    }
}
