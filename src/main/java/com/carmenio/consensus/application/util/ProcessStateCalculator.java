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
 * All states are date-driven with no manual overrides.
 */
public final class ProcessStateCalculator {

    private ProcessStateCalculator() {
        // Utility class — no instantiation
    }

    /**
     * Computes the current {@link ProcessStatus} for the given process at {@code now}.
     * <p>
     * <strong>Pure function</strong> — no side effects, no entity mutation.
     * All states are computed in real-time from the process dates.
     */
    public static ProcessStatus computeState(ElectoralProcess process, Instant now) {
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
     * Transitions the entity's {@code estatus} to the computed date-based state.
     * <p>
     * <strong>Mutates the entity</strong> — sets {@code entity.estatus} to the
     * computed value. Dirty checking will persist the change on the next flush.
     */
    public static ProcessStatus transitionState(ElectoralProcess process, Instant now) {
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
