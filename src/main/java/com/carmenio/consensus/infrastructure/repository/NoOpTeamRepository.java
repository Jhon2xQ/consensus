package com.carmenio.consensus.infrastructure.repository;

import com.carmenio.consensus.domain.repository.TeamRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Temporary stub implementation of {@link TeamRepository}.
 * <p>
 * Always returns {@code false} (no teams) since the Team entity
 * and its JPA adapter are implemented in PR-2.
 * This allows the application context to start
 * and {@code DeleteElectoralProcessUseCase} to be injected.
 */
@Component
public class NoOpTeamRepository implements TeamRepository {

    @Override
    public boolean existsByProcessId(UUID processId) {
        return false;
    }
}
