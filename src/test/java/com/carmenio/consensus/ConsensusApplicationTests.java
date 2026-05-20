package com.carmenio.consensus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ConsensusApplication")
class ConsensusApplicationTests {

    /**
     * Mock JwtDecoder to prevent Spring Boot from trying to connect to the
     * real JWKS endpoint during context loading.
     */
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("should load Spring context without errors")
    void shouldLoadContextWithoutErrors() {
        assertDoesNotThrow(() -> {
            // Context is loaded by @SpringBootTest; if we reach here, it succeeded
        });
    }
}
