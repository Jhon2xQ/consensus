package com.carmenio.consensus.application.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link JwtClaimExtractor}.
 * <p>
 * Verifies extraction of {@code email} and {@code sub} claims from JWT,
 * including error cases when claims are missing.
 */
class JwtClaimExtractorTest {

    private final JwtClaimExtractor extractor = new JwtClaimExtractor();

    @Test
    @DisplayName("Should extract email claim from valid JWT")
    void shouldExtractEmailFromValidJwt() {
        var jwt = createJwt(Map.of("email", "voter@example.com", "sub", "user-123"));

        var email = extractor.extractEmail(jwt);

        assertEquals("voter@example.com", email);
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when email claim is missing")
    void shouldThrowWhenEmailClaimMissing() {
        var jwt = createJwt(Map.of("sub", "user-123"));

        assertThrows(BadCredentialsException.class, () -> extractor.extractEmail(jwt));
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when email claim is null")
    void shouldThrowWhenEmailClaimIsNull() {
        var claims = new java.util.HashMap<String, Object>();
        claims.put("email", null);
        claims.put("sub", "user-123");
        var jwt = createJwt(claims);

        assertThrows(BadCredentialsException.class, () -> extractor.extractEmail(jwt));
    }

    @Test
    @DisplayName("Should extract sub claim as userId from valid JWT")
    void shouldExtractUserIdFromValidJwt() {
        var jwt = createJwt(Map.of("email", "voter@example.com", "sub", "user-123"));

        var userId = extractor.extractUserId(jwt);

        assertEquals("user-123", userId);
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when sub claim is missing")
    void shouldThrowWhenSubClaimMissing() {
        var jwt = createJwt(Map.of("email", "voter@example.com"));

        assertThrows(BadCredentialsException.class, () -> extractor.extractUserId(jwt));
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when sub claim is null")
    void shouldThrowWhenSubClaimIsNull() {
        var claims = new java.util.HashMap<String, Object>();
        claims.put("email", "voter@example.com");
        claims.put("sub", null);
        var jwt = createJwt(claims);

        assertThrows(BadCredentialsException.class, () -> extractor.extractUserId(jwt));
    }

    @Test
    @DisplayName("Should extract email from JWT with different email values")
    void shouldExtractDifferentEmails() {
        var jwt1 = createJwt(Map.of("email", "alice@example.com", "sub", "user-1"));
        var jwt2 = createJwt(Map.of("email", "bob@test.org", "sub", "user-2"));

        assertEquals("alice@example.com", extractor.extractEmail(jwt1));
        assertEquals("bob@test.org", extractor.extractEmail(jwt2));
    }

    /**
     * Creates a Jwt instance with the given claims for testing.
     */
    private Jwt createJwt(Map<String, Object> claims) {
        return new Jwt(
                "test-token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                claims
        );
    }
}
