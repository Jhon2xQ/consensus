package com.carmenio.consensus.domain.exception;

import lombok.Getter;

/**
 * Base class for all domain-specific exceptions.
 * <p>
 * All business rule violations, not-found scenarios, and conflict conditions
 * should extend this class so that {@code ExceptionHandlerMiddleware}
 * can handle them uniformly by reading the {@code statusCode}.
 */
@Getter
public abstract class DomainException extends RuntimeException {

    private final int statusCode;

    protected DomainException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}
