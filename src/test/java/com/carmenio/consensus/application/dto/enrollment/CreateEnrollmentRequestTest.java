package com.carmenio.consensus.application.dto.enrollment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CreateEnrollmentRequest} DTO.
 * <p>
 * Verifies the two-phase enrollment request structure: creator provides
 * email only, userId and commitment are optional (null in creator phase).
 */
class CreateEnrollmentRequestTest {

    @Test
    @DisplayName("should accept email field for creator phase")
    void shouldAcceptEmailField() {
        var processId = UUID.randomUUID();
        var request = CreateEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .email("voter@example.com")
                .build();

        assertEquals(processId, request.getElectoralProcessId());
        assertEquals("voter@example.com", request.getEmail());
        assertNull(request.getUserId(), "userId should be null in creator phase");
        assertNull(request.getCommitment(), "commitment should be null in creator phase");
    }

    @Test
    @DisplayName("should default userId to null when not provided")
    void shouldDefaultUserIdToNull() {
        var processId = UUID.randomUUID();
        var request = CreateEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .email("voter@example.com")
                .build();

        assertNull(request.getUserId());
    }

    @Test
    @DisplayName("should default commitment to null when not provided")
    void shouldDefaultCommitmentToNull() {
        var processId = UUID.randomUUID();
        var request = CreateEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .email("voter@example.com")
                .build();

        assertNull(request.getCommitment());
    }

    @Test
    @DisplayName("should require electoralProcessId and email for creator phase")
    void shouldSupportCreatorPhaseWithEmailOnly() {
        var processId = UUID.randomUUID();
        var request = CreateEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .email("creator-registered@example.com")
                .build();

        assertAll("creator phase enrollment request",
                () -> assertEquals(processId, request.getElectoralProcessId()),
                () -> assertEquals("creator-registered@example.com", request.getEmail()),
                () -> assertNull(request.getUserId()),
                () -> assertNull(request.getCommitment())
        );
    }

    @Test
    @DisplayName("should allow setting userId and commitment for backward compatibility")
    void shouldAllowSettingUserIdAndCommitment() {
        var processId = UUID.randomUUID();
        var request = CreateEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .email("voter@example.com")
                .userId("user-123")
                .commitment("0xabc123")
                .build();

        assertEquals("user-123", request.getUserId());
        assertEquals("0xabc123", request.getCommitment());
    }
}
