package com.carmenio.consensus.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TeamException} factory methods.
 * <p>
 * Verifies each factory method produces the correct HTTP status code
 * and descriptive message for the error condition.
 */
class TeamExceptionTest {

    @Test
    @DisplayName("Should create notFound with status 404 and ID in message")
    void shouldCreateNotFoundException() {
        var id = UUID.randomUUID();

        var ex = TeamException.notFound(id);

        assertEquals(404, ex.getStatusCode());
        assertTrue(ex.getMessage().contains(id.toString()));
    }

    @Test
    @DisplayName("Should create alreadyExists with status 409 and name in message")
    void shouldCreateAlreadyExistsException() {
        var ex = TeamException.alreadyExists("Team Alpha");

        assertEquals(409, ex.getStatusCode());
        assertTrue(ex.getMessage().contains("Team Alpha"));
    }

    @Test
    @DisplayName("Should create processNotFound with status 404")
    void shouldCreateProcessNotFoundException() {
        var ex = TeamException.processNotFound();

        assertEquals(404, ex.getStatusCode());
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("Should create emptyBatch with status 400")
    void shouldCreateEmptyBatchException() {
        var ex = TeamException.emptyBatch();

        assertEquals(400, ex.getStatusCode());
        assertTrue(ex.getMessage().contains("At least one team"));
    }

    @Test
    @DisplayName("Should create duplicateInBatch with status 409 and name in message")
    void shouldCreateDuplicateInBatchException() {
        var ex = TeamException.duplicateInBatch("Team Alpha");

        assertEquals(409, ex.getStatusCode());
        assertTrue(ex.getMessage().contains("Team Alpha"));
        assertTrue(ex.getMessage().contains("Duplicate"));
    }
}
