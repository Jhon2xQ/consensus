package com.carmenio.consensus.domain.exception;

import java.util.UUID;

/**
 * Domain exception for enrollment operations.
 * <p>
 * Uses static factory methods to create semantically named instances
 * with the appropriate HTTP status codes.
 */
public class EnrollmentException extends DomainException {

    private EnrollmentException(String message, int statusCode) {
        super(message, statusCode);
    }

    public static EnrollmentException notFound(UUID id) {
        return new EnrollmentException("Enrollment not found: " + id, 404);
    }

    public static EnrollmentException alreadyExists(String field) {
        return new EnrollmentException("Enrollment with " + field + " already exists", 409);
    }

    public static EnrollmentException invalidState(String reason) {
        return new EnrollmentException(reason, 400);
    }

    public static EnrollmentException duplicateCommitment() {
        return new EnrollmentException("A commitment with this value already exists in the process", 409);
    }
}
