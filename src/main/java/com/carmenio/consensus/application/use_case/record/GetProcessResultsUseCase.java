package com.carmenio.consensus.application.use_case.record;

import com.carmenio.consensus.application.dto.record.ProcessResultsResponse;
import com.carmenio.consensus.application.util.ProcessStateCalculator;
import com.carmenio.consensus.application.util.VoteCounter;
import com.carmenio.consensus.common.constant.ProcessStatus;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.domain.repository.TeamRepository;
import com.carmenio.consensus.domain.repository.VoteRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Use case for retrieving the results of an electoral process.
 * <p>
 * Results are only available when the process is in CLOSED state.
 * They are calculated in real-time by tallying votes against candidate teams.
 */
@Component
@RequiredArgsConstructor
public class GetProcessResultsUseCase {

    private final ElectoralProcessRepository electoralProcessRepository;
    private final VoteRecordRepository voteRecordRepository;
    private final TeamRepository teamRepository;

    /**
     * Retrieves the results for a given electoral process.
     *
     * @param processId the UUID of the electoral process
     * @return the process results with per-team vote tallies
     * @throws ElectoralProcessException if the process does not exist
     *                                   or is not yet in CLOSED state
     */
    public ProcessResultsResponse execute(UUID processId) {
        var process = electoralProcessRepository.findById(processId)
                .orElseThrow(() -> ElectoralProcessException.notFound(processId));

        var state = ProcessStateCalculator.computeState(process, Instant.now());
        if (state != ProcessStatus.CLOSED) {
            throw ElectoralProcessException.invalidState(
                    "Results only available when process is closed");
        }

        var records = voteRecordRepository.findByScope(process.getScope());
        var teams = teamRepository.findByElectoralProcessId(processId);

        var teamResults = VoteCounter.calculateResults(records, teams);
        var totalVotes = records.size();

        return ProcessResultsResponse.builder()
                .processId(processId)
                .processName(process.getName())
                .teamResults(teamResults)
                .totalVotes(totalVotes)
                .status("CLOSED")
                .build();
    }
}
