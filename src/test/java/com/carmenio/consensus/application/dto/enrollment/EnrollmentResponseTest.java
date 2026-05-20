package com.carmenio.consensus.application.dto.enrollment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link EnrollmentResponse} DTO.
 * <p>
 * Verifies the response includes the email field and supports
 * nullable userId/commitment from the two-phase enrollment flow.
 */
class EnrollmentResponseTest {

    @Test
    @DisplayName("should include email field in response")
    void shouldIncludeEmailField() {
        var response = EnrollmentResponse.builder()
                .id(UUID.randomUUID())
                .electoralProcessId(UUID.randomUUID())
                .email("voter@example.com")
                .userId("user-123")
                .commitment("0xabc123")
                .hasVoted(false)
                .build();

        assertAll("response with email field",
                () -> assertNotNull(response.getId()),
                () -> assertNotNull(response.getElectoralProcessId()),
                () -> assertEquals("voter@example.com", response.getEmail()),
                () -> assertEquals("user-123", response.getUserId()),
                () -> assertEquals("0xabc123", response.getCommitment()),
                () -> assertFalse(response.isHasVoted())
        );
    }

    @Test
    @DisplayName("should support null userId and commitment in creator phase response")
    void shouldSupportNullUserIdAndCommitment() {
        var response = EnrollmentResponse.builder()
                .id(UUID.randomUUID())
                .electoralProcessId(UUID.randomUUID())
                .email("voter@example.com")
                .hasVoted(false)
                .build();

        assertAll("creator phase response with null fields",
                () -> assertEquals("voter@example.com", response.getEmail()),
                () -> assertNull(response.getUserId()),
                () -> assertNull(response.getCommitment()),
                () -> assertFalse(response.isHasVoted())
        );
    }

    @Test
    @DisplayName("should default email to null when not provided")
    void shouldDefaultEmailToNull() {
        var response = EnrollmentResponse.builder()
                .id(UUID.randomUUID())
                .electoralProcessId(UUID.randomUUID())
                .userId("user-123")
                .commitment("0xabc123")
                .hasVoted(false)
                .build();

        assertNull(response.getEmail());
    }
}
