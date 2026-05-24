package com.carmenio.consensus.domain.exception;

import java.util.UUID;

/**
 * Domain exception for electoral process operations.
 * <p>
 * Uses static factory methods to create semantically named instances
 * with the appropriate HTTP status codes.
 */
public class ElectoralProcessException extends DomainException {

    private ElectoralProcessException(String message, int statusCode) {
        super(message, statusCode);
    }

    public static ElectoralProcessException notFound(UUID id) {
        return new ElectoralProcessException("Electoral process not found: " + id, 404);
    }

    public static ElectoralProcessException notFound(String scope) {
        return new ElectoralProcessException("Electoral process not found for scope: " + scope, 404);
    }

    public static ElectoralProcessException alreadyExists(String field) {
        return new ElectoralProcessException("Electoral process with " + field + " already exists", 409);
    }

    public static ElectoralProcessException invalidState(String reason) {
        return new ElectoralProcessException(reason, 400);
    }

}
