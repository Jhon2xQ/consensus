package com.carmenio.consensus.domain.exception;

import java.util.UUID;

/**
 * Domain exception for team operations.
 * <p>
 * Uses static factory methods to create semantically named instances
 * with the appropriate HTTP status codes.
 */
public class TeamException extends DomainException {

    private TeamException(String message, int statusCode) {
        super(message, statusCode);
    }

    public static TeamException notFound(UUID id) {
        return new TeamException("Team not found: " + id, 404);
    }

    public static TeamException alreadyExists(String name) {
        return new TeamException("Team with name \"" + name + "\" already exists in this process", 409);
    }

    public static TeamException processNotFound() {
        return new TeamException("Associated electoral process not found", 404);
    }
}
