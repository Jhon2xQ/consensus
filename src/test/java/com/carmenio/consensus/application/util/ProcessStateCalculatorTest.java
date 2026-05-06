package com.carmenio.consensus.application.util;

import com.carmenio.consensus.common.constant.ProcessStatus;
import com.carmenio.consensus.domain.entity.ElectoralProcess;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ProcessStateCalculator}.
 * <p>
 * Verifies all 7 state transitions including boundary edge cases,
 * lock states (PAUSED/CANCELLED), and the new transitionState() method.
 */
class ProcessStateCalculatorTest {

    private static final Instant BASE = Instant.parse("2026-06-01T00:00:00Z");

    private static ElectoralProcess createProcess(
            Instant commitmentStart, Instant commitmentEnd,
            Instant votingStart, Instant votingEnd, Instant results) {
        return ElectoralProcess.builder()
                .name("Test Process")
                .scope("test-process")
                .commitmentStart(commitmentStart)
                .commitmentEnd(commitmentEnd)
                .votingStart(votingStart)
                .votingEnd(votingEnd)
                .results(results)
                .build();
    }

    private static ElectoralProcess createDefaultProcess() {
        return createProcess(
                BASE,                         // commitmentStart
                BASE.plus(10, ChronoUnit.DAYS),
                BASE.plus(20, ChronoUnit.DAYS),
                BASE.plus(30, ChronoUnit.DAYS),
                BASE.plus(40, ChronoUnit.DAYS) // results
        );
    }

    private static Stream<Arguments> stateTransitions() {
        var cs = BASE;                         // commitmentStart
        var ce = BASE.plus(10, ChronoUnit.DAYS); // commitmentEnd
        var vs = BASE.plus(20, ChronoUnit.DAYS); // votingStart
        var ve = BASE.plus(30, ChronoUnit.DAYS); // votingEnd
        var rs = BASE.plus(40, ChronoUnit.DAYS); // results

        return Stream.of(
                // BEFORE commitmentStart
                Arguments.of(cs, ce, vs, ve, rs,
                        BASE.minus(1, ChronoUnit.DAYS), ProcessStatus.NONE,
                        "before commitmentStart → NONE"),

                // AT commitmentStart (boundary)
                Arguments.of(cs, ce, vs, ve, rs,
                        cs, ProcessStatus.COMMITMENT,
                        "at commitmentStart → COMMITMENT"),

                // MIDDLE of commitment window
                Arguments.of(cs, ce, vs, ve, rs,
                        BASE.plus(5, ChronoUnit.DAYS), ProcessStatus.COMMITMENT,
                        "mid-commitment → COMMITMENT"),

                // AT commitmentEnd (boundary)
                Arguments.of(cs, ce, vs, ve, rs,
                        ce, ProcessStatus.COMMITMENT,
                        "at commitmentEnd → COMMITMENT"),

                // BETWEEN commitmentEnd and votingStart
                Arguments.of(cs, ce, vs, ve, rs,
                        BASE.plus(15, ChronoUnit.DAYS), ProcessStatus.NONE,
                        "between commitmentEnd and votingStart → NONE"),

                // AT votingStart (boundary)
                Arguments.of(cs, ce, vs, ve, rs,
                        vs, ProcessStatus.VOTING,
                        "at votingStart → VOTING"),

                // MIDDLE of voting window
                Arguments.of(cs, ce, vs, ve, rs,
                        BASE.plus(25, ChronoUnit.DAYS), ProcessStatus.VOTING,
                        "mid-voting → VOTING"),

                // AT votingEnd (boundary)
                Arguments.of(cs, ce, vs, ve, rs,
                        ve, ProcessStatus.VOTING,
                        "at votingEnd → VOTING"),

                // BETWEEN votingEnd and results
                Arguments.of(cs, ce, vs, ve, rs,
                        BASE.plus(35, ChronoUnit.DAYS), ProcessStatus.NONE,
                        "between votingEnd and results → NONE"),

                // AT results (boundary)
                Arguments.of(cs, ce, vs, ve, rs,
                        rs, ProcessStatus.CLOSED,
                        "at results → CLOSED"),

                // AFTER results
                Arguments.of(cs, ce, vs, ve, rs,
                        BASE.plus(50, ChronoUnit.DAYS), ProcessStatus.CLOSED,
                        "after results → CLOSED")
        );
    }

    @ParameterizedTest(name = "{7}")
    @MethodSource("stateTransitions")
    @DisplayName("should compute correct state for each phase")
    void shouldComputeCorrectStateForEachPhase(
            Instant commitmentStart, Instant commitmentEnd,
            Instant votingStart, Instant votingEnd, Instant results,
            Instant now, ProcessStatus expected, String description) {
        var process = createProcess(commitmentStart, commitmentEnd, votingStart, votingEnd, results);
        var actual = ProcessStateCalculator.computeState(process, now);
        assertEquals(expected, actual, description);
    }

    @Test
    @DisplayName("should return PAUSED when estatus is PAUSED (lock override)")
    void shouldReturnPausedWhenEstatusIsPaused() {
        var process = createDefaultProcess();
        process.setEstatus(ProcessStatus.PAUSED);

        // Even though now is during COMMITMENT window, PAUSED is a lock
        var now = BASE.plus(5, ChronoUnit.DAYS);
        var result = ProcessStateCalculator.computeState(process, now);

        assertEquals(ProcessStatus.PAUSED, result);
    }

    @Test
    @DisplayName("should return CANCELLED when estatus is CANCELLED (lock override)")
    void shouldReturnCancelledWhenEstatusIsCancelled() {
        var process = createDefaultProcess();
        process.setEstatus(ProcessStatus.CANCELLED);

        var now = BASE.plus(5, ChronoUnit.DAYS);
        var result = ProcessStateCalculator.computeState(process, now);

        assertEquals(ProcessStatus.CANCELLED, result);
    }

    @Test
    @DisplayName("should compute real-time state when estatus is NONE (default)")
    void shouldComputeRealTimeStateWhenEstatusIsNone() {
        var process = createDefaultProcess();
        // estatus is NONE by default (from @Builder.Default)

        var now = BASE.plus(5, ChronoUnit.DAYS);
        var result = ProcessStateCalculator.computeState(process, now);

        assertEquals(ProcessStatus.COMMITMENT, result,
                "should compute COMMITMENT from dates when estatus is NONE");
    }

    @Test
    @DisplayName("should compute real-time state ignoring NONE/COMMITMENT/VOTING/CLOSED as overrides")
    void shouldComputeRealTimeStateIgnoringNonOverrideEstatus() {
        var process = createDefaultProcess();
        // Set estatus to COMMITMENT (not a lock state — should be ignored in computeState)
        process.setEstatus(ProcessStatus.COMMITMENT);

        // Now is between votingEnd and results, so computeState should return NONE
        var now = BASE.plus(35, ChronoUnit.DAYS);
        var result = ProcessStateCalculator.computeState(process, now);

        assertEquals(ProcessStatus.NONE, result,
                "COMMITMENT is not a lock state — computeState should return NONE from dates");
    }

    @Test
    @DisplayName("should throw exception when commitmentStart is after commitmentEnd")
    void shouldThrowExceptionWhenCommitmentStartAfterCommitmentEnd() {
        var process = createProcess(
                BASE.plus(10, ChronoUnit.DAYS),  // commitmentStart AFTER
                BASE,                             // commitmentEnd
                BASE.plus(20, ChronoUnit.DAYS),
                BASE.plus(30, ChronoUnit.DAYS),
                BASE.plus(40, ChronoUnit.DAYS)
        );

        assertThrows(IllegalArgumentException.class,
                () -> ProcessStateCalculator.computeState(process, BASE));
    }

    // ─── transitionState() tests ───────────────────────────────────────────

    @Test
    @DisplayName("transitionState should set estatus when it differs from computed")
    void transitionStateShouldSetEstatusWhenDifferentFromComputed() {
        var process = createDefaultProcess();
        process.setEstatus(ProcessStatus.COMMITMENT); // stale persisted value
        var now = BASE.plus(35, ChronoUnit.DAYS);      // between votingEnd and results → NONE

        ProcessStateCalculator.transitionState(process, now);

        assertEquals(ProcessStatus.NONE, process.getEstatus(),
                "should auto-transition from COMMITMENT to NONE");
    }

    @Test
    @DisplayName("transitionState should not change estatus when it already matches computed")
    void transitionStateShouldNotChangeWhenAlreadyMatchesComputed() {
        var process = createDefaultProcess();
        var now = BASE.plus(5, ChronoUnit.DAYS); // commitment window → COMMITMENT

        // First transition sets it to COMMITMENT
        ProcessStateCalculator.transitionState(process, now);
        assertEquals(ProcessStatus.COMMITMENT, process.getEstatus());

        // Second transition with same time should not change it
        ProcessStateCalculator.transitionState(process, now);
        assertEquals(ProcessStatus.COMMITMENT, process.getEstatus(),
                "should not change when already matches computed");
    }

    @Test
    @DisplayName("transitionState should NOT mutate when estatus is PAUSED (lock)")
    void transitionStateShouldNotMutateWhenPaused() {
        var process = createDefaultProcess();
        process.setEstatus(ProcessStatus.PAUSED);

        // Now is during COMMITMENT, but PAUSED is a lock
        var now = BASE.plus(5, ChronoUnit.DAYS);
        ProcessStateCalculator.transitionState(process, now);

        assertEquals(ProcessStatus.PAUSED, process.getEstatus(),
                "PAUSED should block auto-transition");
    }

    @Test
    @DisplayName("transitionState should NOT mutate when estatus is CANCELLED (lock)")
    void transitionStateShouldNotMutateWhenCancelled() {
        var process = createDefaultProcess();
        process.setEstatus(ProcessStatus.CANCELLED);

        var now = BASE.plus(5, ChronoUnit.DAYS);
        ProcessStateCalculator.transitionState(process, now);

        assertEquals(ProcessStatus.CANCELLED, process.getEstatus(),
                "CANCELLED should block auto-transition");
    }
}
