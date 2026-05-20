package com.carmenio.consensus.application.util;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Utility for extracting claims from a JWT token.
 * <p>
 * Isolates JWT claim reading from use cases. Throws
 * {@link BadCredentialsException} when required claims are missing,
 * which is caught by {@code ExceptionHandlerMiddleware} and returned
 * as HTTP 401.
 */
@Component
public class JwtClaimExtractor {

    /**
     * Extracts the {@code email} claim from the JWT.
     *
     * @param jwt the authenticated JWT token
     * @return the email string from the {@code email} claim
     * @throws BadCredentialsException if the email claim is missing or null
     */
    public String extractEmail(Jwt jwt) {
        var email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new BadCredentialsException("Missing email claim in JWT");
        }
        return email;
    }

    /**
     * Extracts the {@code sub} claim from the JWT as the user ID.
     *
     * @param jwt the authenticated JWT token
     * @return the subject string from the {@code sub} claim
     * @throws BadCredentialsException if the sub claim is missing or null
     */
    public String extractUserId(Jwt jwt) {
        var sub = jwt.getSubject();
        if (sub == null || sub.isBlank()) {
            throw new BadCredentialsException("Missing sub claim in JWT");
        }
        return sub;
    }
}
