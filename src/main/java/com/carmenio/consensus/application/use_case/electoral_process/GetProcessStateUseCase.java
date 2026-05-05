package com.carmenio.consensus.application.use_case.electoral_process;

import com.carmenio.consensus.application.dto.electoral_process.ProcessStateResponse;
import com.carmenio.consensus.application.util.ProcessStateCalculator;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Use case for retrieving the real-time state of an electoral process.
 * <p>
 * The state is calculated dynamically from the process dates.
 */
@Component
@RequiredArgsConstructor
public class GetProcessStateUseCase {

    private final ElectoralProcessRepository repository;

    /**
     * Computes and returns the current state of a process.
     *
     * @param processId the process UUID
     * @param now       the reference instant
     * @return the current state response
     * @throws ElectoralProcessException if no process exists with the given ID
     */
    public ProcessStateResponse execute(UUID processId, Instant now) {
        var entity = repository.findById(processId)
                .orElseThrow(() -> ElectoralProcessException.notFound(processId));

        var state = ProcessStateCalculator.computeState(entity, now);

        return ProcessStateResponse.builder()
                .processId(processId)
                .state(state)
                .build();
    }
}
