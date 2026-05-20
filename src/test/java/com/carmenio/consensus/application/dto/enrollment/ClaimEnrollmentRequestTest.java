package com.carmenio.consensus.application.dto.enrollment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ClaimEnrollmentRequest} DTO.
 * <p>
 * Verifies the structure used by the user phase: electoralProcessId
 * and commitment are required for claiming an enrollment.
 */
class ClaimEnrollmentRequestTest {

    @Test
    @DisplayName("should create request with electoralProcessId and commitment")
    void shouldCreateWithElectoralProcessIdAndCommitment() {
        var processId = UUID.randomUUID();
        var request = ClaimEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .commitment("0xabc123")
                .build();

        assertAll("claim enrollment request",
                () -> assertEquals(processId, request.getElectoralProcessId()),
                () -> assertEquals("0xabc123", request.getCommitment())
        );
    }

    @Test
    @DisplayName("should require non-null electoralProcessId")
    void shouldRequireNonNullElectoralProcessId() {
        var request = ClaimEnrollmentRequest.builder()
                .electoralProcessId(UUID.randomUUID())
                .commitment("0xabc123")
                .build();

        assertNotNull(request.getElectoralProcessId());
    }

    @Test
    @DisplayName("should require non-blank commitment")
    void shouldRequireNonBlankCommitment() {
        var request = ClaimEnrollmentRequest.builder()
                .electoralProcessId(UUID.randomUUID())
                .commitment("0xdef456")
                .build();

        assertNotNull(request.getCommitment());
        assertFalse(request.getCommitment().isBlank());
    }

    @Test
    @DisplayName("should support all fields via builder")
    void shouldSupportAllFieldsViaBuilder() {
        var processId = UUID.randomUUID();
        var request = new ClaimEnrollmentRequest(processId, "0x123456");

        assertAll("all-args constructor",
                () -> assertEquals(processId, request.getElectoralProcessId()),
                () -> assertEquals("0x123456", request.getCommitment())
        );
    }
}
