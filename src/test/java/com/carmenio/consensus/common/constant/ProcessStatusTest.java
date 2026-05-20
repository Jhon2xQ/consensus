package com.carmenio.consensus.common.constant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ProcessStatus} enum.
 */
class ProcessStatusTest {

    @Test
    @DisplayName("should have all expected values")
    void shouldHaveAllExpectedValues() {
        var values = ProcessStatus.values();
        assertAll(
                () -> assertEquals(4, values.length),
                () -> assertEquals(ProcessStatus.NONE, values[0]),
                () -> assertEquals(ProcessStatus.COMMITMENT, values[1]),
                () -> assertEquals(ProcessStatus.VOTING, values[2]),
                () -> assertEquals(ProcessStatus.CLOSED, values[3])
        );
    }
}
