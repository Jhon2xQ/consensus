package com.carmenio.consensus.presentation.middleware;

import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.presentation.schema.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExceptionHandlerMiddleware")
class ExceptionHandlerMiddlewareTest {

    private ExceptionHandlerMiddleware handler;

    @BeforeEach
    void setUp() {
        handler = new ExceptionHandlerMiddleware();
    }

    @Test
    @DisplayName("should return 404 when DomainException has statusCode 404")
    void shouldReturn404WhenDomainExceptionHasStatusCode404() {
        var exception = ElectoralProcessException.notFound(UUID.randomUUID());

        ResponseEntity<ApiResponse<?>> response = handler.handleDomainException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("not found"));
    }

    @Test
    @DisplayName("should return 400 when DomainException has statusCode 400")
    void shouldReturn400WhenDomainExceptionHasStatusCode400() {
        var exception = ElectoralProcessException.invalidState("Enrollment not open");

        ResponseEntity<ApiResponse<?>> response = handler.handleDomainException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Enrollment not open", response.getBody().getMessage());
    }

    @Test
    @DisplayName("should return 409 when DomainException has statusCode 409")
    void shouldReturn409WhenDomainExceptionHasStatusCode409() {
        var exception = ElectoralProcessException.alreadyExists("name \"test\"");

        ResponseEntity<ApiResponse<?>> response = handler.handleDomainException(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("already exists"));
    }

    @Test
    @DisplayName("should return 409 when DataIntegrityViolationException is thrown")
    void shouldReturn409WhenDataIntegrityViolation() {
        var exception = new DataIntegrityViolationException("Duplicate key value violates unique constraint");

        ResponseEntity<ApiResponse<?>> response = handler.handleDataIntegrity(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Data conflict"));
    }

    @Test
    @DisplayName("should return 500 when unknown RuntimeException is thrown")
    void shouldReturn500WhenUnknownException() {
        var exception = new RuntimeException("Unexpected error");

        ResponseEntity<ApiResponse<?>> response = handler.handleUnexpected(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Internal server error", response.getBody().getMessage());
    }
}
