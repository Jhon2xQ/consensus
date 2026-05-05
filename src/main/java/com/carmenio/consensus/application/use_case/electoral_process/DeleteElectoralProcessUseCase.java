package com.carmenio.consensus.application.use_case.electoral_process;

import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.domain.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Use case for deleting an electoral process.
 * <p>
 * Validates that the process has no dependencies (teams) before deletion.
 */
@Component
@RequiredArgsConstructor
public class DeleteElectoralProcessUseCase {

    private final ElectoralProcessRepository electoralProcessRepository;
    private final TeamRepository teamRepository;

    /**
     * Deletes an electoral process by ID.
     *
     * @param id the process UUID
     * @throws ElectoralProcessException if no process exists or it has dependencies
     */
    public void execute(UUID id) {
        var entity = electoralProcessRepository.findById(id)
                .orElseThrow(() -> ElectoralProcessException.notFound(id));

        if (teamRepository.existsByProcessId(id)) {
            throw ElectoralProcessException.hasDependencies();
        }

        electoralProcessRepository.delete(entity);
    }
}
