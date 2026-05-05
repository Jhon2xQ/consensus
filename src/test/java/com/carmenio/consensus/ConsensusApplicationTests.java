package com.carmenio.consensus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ConsensusApplication")
class ConsensusApplicationTests {

    @Test
    @DisplayName("should load Spring context without errors")
    void shouldLoadContextWithoutErrors() {
        assertDoesNotThrow(() -> {
            // Context is loaded by @SpringBootTest; if we reach here, it succeeded
        });
    }
}
