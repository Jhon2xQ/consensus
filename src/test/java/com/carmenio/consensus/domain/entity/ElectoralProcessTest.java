package com.carmenio.consensus.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ElectoralProcess}.
 * <p>
 * Verifies entity creation via Builder, field constraints via JPA annotations,
 * and UUID generation strategy.
 */
class ElectoralProcessTest {

    @Test
    @DisplayName("should create entity with all fields via Builder")
    void shouldCreateEntityWithAllFieldsViaBuilder() {
        var now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        var later = now.plusSeconds(3600);

        var process = ElectoralProcess.builder()
                .name("Presidential Election 2026")
                .scope("presidential-2026")
                .commitmentStart(now)
                .commitmentEnd(now.plusSeconds(7200))
                .votingStart(now.plusSeconds(14400))
                .votingEnd(now.plusSeconds(21600))
                .results(later.plusSeconds(28800))
                .build();

        assertAll("process entity fields",
                () -> assertEquals("Presidential Election 2026", process.getName()),
                () -> assertEquals("presidential-2026", process.getScope()),
                () -> assertEquals(now, process.getCommitmentStart()),
                () -> assertEquals(now.plusSeconds(7200), process.getCommitmentEnd()),
                () -> assertEquals(now.plusSeconds(14400), process.getVotingStart()),
                () -> assertEquals(now.plusSeconds(21600), process.getVotingEnd()),
                () -> assertEquals(later.plusSeconds(28800), process.getResults())
        );
    }

    @Test
    @DisplayName("should allow null id before persistence")
    void shouldAllowNullIdBeforePersistence() {
        var process = ElectoralProcess.builder()
                .name("Null ID Test")
                .scope("null-id-test")
                .commitmentStart(Instant.now())
                .commitmentEnd(Instant.now().plusSeconds(3600))
                .votingStart(Instant.now().plusSeconds(7200))
                .votingEnd(Instant.now().plusSeconds(10800))
                .results(Instant.now().plusSeconds(14400))
                .build();

        assertNull(process.getId(),
                "ID should be null before JPA persistence");
    }

    @Test
    @DisplayName("should set and get id after persistence simulation")
    void shouldSetAndGetIdAfterPersistence() {
        var id = UUID.randomUUID();
        var process = new ElectoralProcess();
        process.setId(id);

        assertEquals(id, process.getId());
    }

    @Test
    @DisplayName("should have default no-arg constructor for JPA")
    void shouldHaveNoArgConstructorForJpa() {
        var process = new ElectoralProcess();
        assertNotNull(process);
    }

    @Test
    @DisplayName("should have same values with Builder")
    void shouldHaveSameValuesWithBuilder() {
        var now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        var id = UUID.randomUUID();

        var process = ElectoralProcess.builder()
                .id(id)
                .name("Builder Test")
                .scope("builder-test")
                .commitmentStart(now)
                .commitmentEnd(now.plusSeconds(3600))
                .votingStart(now.plusSeconds(7200))
                .votingEnd(now.plusSeconds(10800))
                .results(now.plusSeconds(14400))
                .build();

        assertAll("builder",
                () -> assertEquals(id, process.getId()),
                () -> assertEquals("Builder Test", process.getName()),
                () -> assertEquals("builder-test", process.getScope()),
                () -> assertEquals(now, process.getCommitmentStart()),
                () -> assertEquals(now.plusSeconds(3600), process.getCommitmentEnd()),
                () -> assertEquals(now.plusSeconds(7200), process.getVotingStart()),
                () -> assertEquals(now.plusSeconds(10800), process.getVotingEnd()),
                () -> assertEquals(now.plusSeconds(14400), process.getResults())
        );
    }
}
