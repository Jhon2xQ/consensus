package com.carmenio.consensus.presentation.middleware;

import com.carmenio.consensus.domain.exception.DomainException;
import com.carmenio.consensus.presentation.schema.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler that catches exceptions and returns
 * standardized {@link ApiResponse} with appropriate HTTP status codes.
 *
 * <table>
 *   <caption>Exception to HTTP status mapping</caption>
 *   <tr><th>Exception</th><th>HTTP Status</th></tr>
 *   <tr><td>{@link DomainException}</td><td>Uses exception's {@code statusCode}</td></tr>
 *   <tr><td>{@link DataIntegrityViolationException}</td><td>409 Conflict</td></tr>
 *   <tr><td>Any unhandled exception</td><td>500 Internal Server Error</td></tr>
 * </table>
 */
@Slf4j
@ControllerAdvice
public class ExceptionHandlerMiddleware {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<?>> handleDomainException(DomainException ex) {
        log.warn("Domain exception [{}]: {}", ex.getStatusCode(), ex.getMessage());
        return ResponseEntity.status(ex.getStatusCode())
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Data conflict: " + ex.getMostSpecificCause().getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error"));
    }
}
