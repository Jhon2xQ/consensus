package com.carmenio.consensus.domain.exception;

import java.util.UUID;

/**
 * Domain exception for record (vote) operations.
 * <p>
 * Uses static factory methods to create semantically named instances
 * with the appropriate HTTP status codes.
 */
public class RecordException extends DomainException {

    private RecordException(String message, int statusCode) {
        super(message, statusCode);
    }

    public static RecordException notFound(UUID id) {
        return new RecordException("Record not found: " + id, 404);
    }

    public static RecordException invalidScope() {
        return new RecordException("Record scope does not match any active process", 400);
    }

    public static RecordException duplicateNullifier() {
        return new RecordException("A record with this nullifier already exists", 409);
    }

    public static RecordException invalidMessage() {
        return new RecordException("Record message does not match any team in the process", 400);
    }
}
