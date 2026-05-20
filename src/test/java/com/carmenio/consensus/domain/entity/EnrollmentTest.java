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
        var email = "voter@example.com";
        var commitment = "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";

        var enrollment = Enrollment.builder()
                .id(id)
                .electoralProcessId(processId)
                .email(email)
                .userId(userId)
                .commitment(commitment)
                .hasVoted(false)
                .build();

        assertNotNull(enrollment);
        assertEquals(id, enrollment.getId());
        assertEquals(processId, enrollment.getElectoralProcessId());
        assertEquals(email, enrollment.getEmail());
        assertEquals(userId, enrollment.getUserId());
        assertEquals(commitment, enrollment.getCommitment());
        assertFalse(enrollment.isHasVoted());
    }

    @Test
    @DisplayName("Should create enrollment with default hasVoted false")
    void shouldCreateEnrollmentWithDefaultHasVoted() {
        var enrollment = Enrollment.builder()
                .electoralProcessId(UUID.randomUUID())
                .email("voter-default@example.com")
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
                .email("voter-hasvoted@example.com")
                .userId("user-789")
                .commitment("2222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222")
                .hasVoted(true)
                .build();

        assertTrue(enrollment.isHasVoted());
    }

    @Test
    @DisplayName("Should allow null userId for phase-1 enrollment (creator registers email only)")
    void shouldAllowNullUserId() {
        var enrollment = Enrollment.builder()
                .electoralProcessId(UUID.randomUUID())
                .email("voter@example.com")
                .build();

        assertNull(enrollment.getUserId());
        assertNotNull(enrollment.getEmail());
    }

    @Test
    @DisplayName("Should allow null commitment for phase-1 enrollment (creator registers email only)")
    void shouldAllowNullCommitment() {
        var enrollment = Enrollment.builder()
                .electoralProcessId(UUID.randomUUID())
                .email("voter@example.com")
                .build();

        assertNull(enrollment.getCommitment());
        assertNotNull(enrollment.getEmail());
    }

    @Test
    @DisplayName("Should create phase-1 enrollment with email only (userId and commitment both null)")
    void shouldCreateEnrollmentWithEmailOnly() {
        var processId = UUID.randomUUID();
        var email = "voter@example.com";

        var enrollment = Enrollment.builder()
                .electoralProcessId(processId)
                .email(email)
                .build();

        assertEquals(processId, enrollment.getElectoralProcessId());
        assertEquals(email, enrollment.getEmail());
        assertNull(enrollment.getUserId());
        assertNull(enrollment.getCommitment());
        assertFalse(enrollment.isHasVoted());
    }
}
