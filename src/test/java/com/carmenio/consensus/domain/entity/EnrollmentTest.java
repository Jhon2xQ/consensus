package com.carmenio.consensus.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Enrollment} entity.
 * <p>
 * Verifies entity creation, builder usage, field access, and default values.
 */
class EnrollmentTest {

    @Test
    @DisplayName("Should create enrollment with all fields using builder")
    void shouldCreateEnrollmentWithAllFields() {
        var id = UUID.randomUUID();
        var processId = UUID.randomUUID();
        var userId = "user-123";
        var commitment = "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";

        var enrollment = Enrollment.builder()
                .id(id)
                .electoralProcessId(processId)
                .userId(userId)
                .commitment(commitment)
                .hasVoted(false)
                .build();

        assertNotNull(enrollment);
        assertEquals(id, enrollment.getId());
        assertEquals(processId, enrollment.getElectoralProcessId());
        assertEquals(userId, enrollment.getUserId());
        assertEquals(commitment, enrollment.getCommitment());
        assertFalse(enrollment.isHasVoted());
    }

    @Test
    @DisplayName("Should create enrollment with default hasVoted false")
    void shouldCreateEnrollmentWithDefaultHasVoted() {
        var enrollment = Enrollment.builder()
                .electoralProcessId(UUID.randomUUID())
                .userId("user-456")
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .build();

        assertNotNull(enrollment);
        assertFalse(enrollment.isHasVoted());
    }

    @Test
    @DisplayName("Should allow setting hasVoted to true")
    void shouldAllowSettingHasVotedToTrue() {
        var enrollment = Enrollment.builder()
                .electoralProcessId(UUID.randomUUID())
                .userId("user-789")
                .commitment("2222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222")
                .hasVoted(true)
                .build();

        assertTrue(enrollment.isHasVoted());
    }
}
