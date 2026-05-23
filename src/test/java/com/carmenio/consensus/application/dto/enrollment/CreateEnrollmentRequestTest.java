package com.carmenio.consensus.application.dto.enrollment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CreateEnrollmentRequest} DTO.
 * <p>
 * Verifies the creator-phase enrollment request structure: email only.
 */
class CreateEnrollmentRequestTest {

    @Test
    @DisplayName("should accept email field for creator phase")
    void shouldAcceptEmailField() {
        var request = CreateEnrollmentRequest.builder()
                .email("voter@example.com")
                .build();

        assertEquals("voter@example.com", request.getEmail());
    }

    @Test
    @DisplayName("should support creator phase with email only")
    void shouldSupportCreatorPhaseWithEmailOnly() {
        var request = CreateEnrollmentRequest.builder()
                .email("creator-registered@example.com")
                .build();

        assertEquals("creator-registered@example.com", request.getEmail());
    }
}
