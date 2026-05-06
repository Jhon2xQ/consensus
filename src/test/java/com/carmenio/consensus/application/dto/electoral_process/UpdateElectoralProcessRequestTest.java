package com.carmenio.consensus.application.dto.electoral_process;

import com.carmenio.consensus.common.constant.ProcessStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link UpdateElectoralProcessRequest} DTO.
 */
class UpdateElectoralProcessRequestTest {

    @Test
    @DisplayName("should accept optional description field")
    void shouldAcceptOptionalDescription() {
        var request = UpdateElectoralProcessRequest.builder()
                .name("Updated Name")
                .description("Updated description")
                .build();

        assertEquals("Updated description", request.getDescription());
    }

    @Test
    @DisplayName("should accept estatus for manual override")
    void shouldAcceptEstatusForManualOverride() {
        var request = UpdateElectoralProcessRequest.builder()
                .estatus(ProcessStatus.PAUSED)
                .build();

        assertEquals(ProcessStatus.PAUSED, request.getEstatus());
    }

    @Test
    @DisplayName("should default description to null when not provided")
    void shouldDefaultDescriptionToNull() {
        var request = new UpdateElectoralProcessRequest();

        assertNull(request.getDescription());
    }

    @Test
    @DisplayName("should build with description via builder")
    void shouldBuildWithDescriptionViaBuilder() {
        var request = UpdateElectoralProcessRequest.builder()
                .description("A description")
                .build();

        assertEquals("A description", request.getDescription());
    }
}
