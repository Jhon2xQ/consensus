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

    public static EnrollmentException emailAlreadyRegistered(UUID processId, String email) {
        return new EnrollmentException(
                "Email already registered in this process: " + processId + " - " + email, 409);
    }

    public static EnrollmentException emailNotFound(UUID processId, String email) {
        return new EnrollmentException(
                "No enrollment found for this email in this process: " + processId + " - " + email, 404);
    }

    public static EnrollmentException emailMismatch() {
        return new EnrollmentException("Email does not match the enrollment email", 404);
    }

    public static EnrollmentException missingJwtClaims() {
        return new EnrollmentException("Missing required claims in JWT", 401);
    }

    public static EnrollmentException processIdMismatch() {
        return new EnrollmentException("Enrollment does not belong to the specified process", 404);
    }

    public static EnrollmentException emptyBatch() {
        return new EnrollmentException("At least one enrollment is required", 400);
    }

    public static EnrollmentException duplicateEmailInBatch(String email) {
        return new EnrollmentException("Duplicate email in request: " + email, 409);
    }
}
