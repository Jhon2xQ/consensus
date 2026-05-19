package com.carmenio.consensus.presentation.middleware;

import com.carmenio.consensus.domain.exception.DomainException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
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
 *   <tr><td>{@link AuthenticationException}</td><td>401 Unauthorized</td></tr>
 *   <tr><td>{@link AccessDeniedException}</td><td>403 Forbidden</td></tr>
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

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthentication(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Full authentication is required to access this resource"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied: insufficient permissions"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error"));
    }
}
