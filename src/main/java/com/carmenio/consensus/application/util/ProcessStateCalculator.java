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
 * The state is never persisted; it is computed on every request.
 */
public final class ProcessStateCalculator {

    private ProcessStateCalculator() {
        // Utility class — no instantiation
    }

    /**
     * Computes the current {@link ProcessStatus} for the given process at {@code now}.
     *
     * @param process the electoral process (must have non-null dates)
     * @param now     the reference instant (typically {@link Instant#now()})
     * @return the computed state
     * @throws IllegalArgumentException if dates are invalid (commitmentStart after commitmentEnd, etc.)
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
