package com.carmenio.consensus.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link EnrollmentException} factory methods.
 * <p>
 * Verifies each factory method produces the correct HTTP status code
 * and descriptive message for the error condition.
 */
class EnrollmentExceptionTest {

    @Test
    @DisplayName("Should create emailAlreadyRegistered with status 409 and descriptive message")
    void shouldCreateEmailAlreadyRegisteredException() {
        var processId = UUID.randomUUID();
        var email = "voter@example.com";

        var ex = EnrollmentException.emailAlreadyRegistered(processId, email);

        assertEquals(409, ex.getStatusCode());
        assertTrue(ex.getMessage().contains(processId.toString()),
                "Message should contain the process ID");
        assertTrue(ex.getMessage().contains(email),
                "Message should contain the email");
    }

    @Test
    @DisplayName("Should create emailNotFound with status 404 and descriptive message")
    void shouldCreateEmailNotFoundException() {
        var processId = UUID.randomUUID();
        var email = "unknown@example.com";

        var ex = EnrollmentException.emailNotFound(processId, email);

        assertEquals(404, ex.getStatusCode());
        assertTrue(ex.getMessage().contains(processId.toString()),
                "Message should contain the process ID");
        assertTrue(ex.getMessage().contains(email),
                "Message should contain the email");
    }

    @Test
    @DisplayName("Should create emailMismatch with status 404")
    void shouldCreateEmailMismatchException() {
        var ex = EnrollmentException.emailMismatch();

        assertEquals(404, ex.getStatusCode());
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Should create missingJwtClaims with status 401")
    void shouldCreateMissingJwtClaimsException() {
        var ex = EnrollmentException.missingJwtClaims();

        assertEquals(401, ex.getStatusCode());
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Should preserve existing notFound factory method")
    void shouldPreserveExistingNotFoundFactory() {
        var id = UUID.randomUUID();
        var ex = EnrollmentException.notFound(id);

        assertEquals(404, ex.getStatusCode());
        assertTrue(ex.getMessage().contains(id.toString()));
    }

    @Test
    @DisplayName("Should preserve existing alreadyExists factory method")
    void shouldPreserveExistingAlreadyExistsFactory() {
        var ex = EnrollmentException.alreadyExists("userId");

        assertEquals(409, ex.getStatusCode());
        assertTrue(ex.getMessage().contains("userId"));
    }

    @Test
    @DisplayName("Should preserve existing invalidState factory method")
    void shouldPreserveExistingInvalidStateFactory() {
        var ex = EnrollmentException.invalidState("Process is closed");

        assertEquals(400, ex.getStatusCode());
        assertEquals("Process is closed", ex.getMessage());
    }

    @Test
    @DisplayName("Should preserve existing duplicateCommitment factory method")
    void shouldPreserveExistingDuplicateCommitmentFactory() {
        var ex = EnrollmentException.duplicateCommitment();

        assertEquals(409, ex.getStatusCode());
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Should create emptyBatch with status 400")
    void shouldCreateEmptyBatchException() {
        var ex = EnrollmentException.emptyBatch();

        assertEquals(400, ex.getStatusCode());
        assertTrue(ex.getMessage().contains("At least one enrollment"));
    }

    @Test
    @DisplayName("Should create duplicateEmailInBatch with status 409 and email in message")
    void shouldCreateDuplicateEmailInBatchException() {
        var ex = EnrollmentException.duplicateEmailInBatch("dup@test.com");

        assertEquals(409, ex.getStatusCode());
        assertTrue(ex.getMessage().contains("dup@test.com"));
        assertTrue(ex.getMessage().contains("Duplicate"));
    }
}
