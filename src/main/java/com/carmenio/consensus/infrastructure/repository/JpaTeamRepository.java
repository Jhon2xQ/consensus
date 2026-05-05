package com.carmenio.consensus.infrastructure.repository;

import com.carmenio.consensus.domain.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Team}.
 * <p>
 * Not exposed outside the infrastructure layer — use
 * {@link JpaTeamRepositoryAdapter} instead.
 */
@Repository
interface JpaTeamRepository extends JpaRepository<Team, UUID> {

    List<Team> findByElectoralProcessId(UUID electoralProcessId);

    boolean existsByElectoralProcessId(UUID electoralProcessId);
}
