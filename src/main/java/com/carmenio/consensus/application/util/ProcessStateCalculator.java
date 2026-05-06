package com.carmenio.consensus.application.util;

import com.carmenio.consensus.common.constant.ProcessStatus;
import com.carmenio.consensus.domain.entity.ElectoralProcess;

import java.time.Instant;

/**
 * Utility that calculates the real-time state of an {@link ElectoralProcess}
 * based on its configured dates.
 * <p>
 * State transitions:
 * <pre>
 * NONE → COMMITMENT → NONE → VOTING → NONE → CLOSED
 * </pre>
 * PAUSED and CANCELLED are manual lock states that <strong>always</strong>
 * return their value from both {@link #computeState} and {@link #transitionState},
 * blocking any auto-transition.
 * <p>
 * <strong>Important</strong>: Only PAUSED and CANCELLED act as overrides.
 * NONE, COMMITMENT, VOTING, and CLOSED are always computed from dates.
 */
public final class ProcessStateCalculator {

    private ProcessStateCalculator() {
        // Utility class — no instantiation
    }

    /**
     * Computes the current {@link ProcessStatus} for the given process at {@code now}.
     * <p>
     * <strong>Pure function</strong> — no side effects, no entity mutation.
     * <p>
     * Only {@code PAUSED} and {@code CANCELLED} act as lock overrides (returned immediately).
     * All other states are computed in real-time from the process dates.
     *
     * @param process the electoral process (must have non-null dates)
     * @param now     the reference instant (typically {@link Instant#now()})
     * @return the computed or lock-overridden state
     * @throws IllegalArgumentException if dates are invalid (commitmentStart after commitmentEnd, etc.)
     */
    public static ProcessStatus computeState(ElectoralProcess process, Instant now) {
        if (process.getEstatus() == ProcessStatus.PAUSED
                || process.getEstatus() == ProcessStatus.CANCELLED) {
            return process.getEstatus();
        }
        validateDates(process);

        if (!now.isBefore(process.getCommitmentStart())) {
            if (!now.isAfter(process.getCommitmentEnd())) {
                return ProcessStatus.COMMITMENT;
            }
            if (!now.isBefore(process.getVotingStart())) {
                if (!now.isAfter(process.getVotingEnd())) {
                    return ProcessStatus.VOTING;
                }
                if (!now.isBefore(process.getResults())) {
                    return ProcessStatus.CLOSED;
                }
            }
        }
        return ProcessStatus.NONE;
    }

    /**
     * Transitions the entity's {@code estatus} to the computed date-based state,
     * respecting PAUSED/CANCELLED as immutable lock states.
     * <p>
     * <strong>Mutates the entity</strong> — sets {@code entity.estatus} to the
     * computed value. Dirty checking will persist the change on the next flush.
     *
     * @param process the electoral process to transition (will be mutated)
     * @param now     the reference instant (typically {@link Instant#now()})
     * @return the computed state (same as {@link #computeState} would return)
     */
    public static ProcessStatus transitionState(ElectoralProcess process, Instant now) {
        if (process.getEstatus() == ProcessStatus.PAUSED
                || process.getEstatus() == ProcessStatus.CANCELLED) {
            return process.getEstatus();
        }
        var computed = computeState(process, now);
        process.setEstatus(computed);
        return computed;
    }

    private static void validateDates(ElectoralProcess process) {
        if (process.getCommitmentStart().isAfter(process.getCommitmentEnd())) {
            throw new IllegalArgumentException(
                    "commitmentStart must not be after commitmentEnd");
        }
        if (process.getVotingStart().isAfter(process.getVotingEnd())) {
            throw new IllegalArgumentException(
                    "votingStart must not be after votingEnd");
        }
    }
}
