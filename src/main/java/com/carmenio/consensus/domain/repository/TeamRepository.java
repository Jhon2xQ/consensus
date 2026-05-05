package com.carmenio.consensus.domain.repository;

import java.util.UUID;

/**
 * Repository port for Team persistence.
 * <p>
 * Minimal interface needed by {@code DeleteElectoralProcessUseCase}
 * to validate dependency constraints. Full CRUD will be added in PR-2.
 */
public interface TeamRepository {

    /**
     * Returns whether any team exists for the given process.
     */
    boolean existsByProcessId(UUID processId);
}
