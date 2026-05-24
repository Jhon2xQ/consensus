package com.carmenio.consensus.domain.repository;

import com.carmenio.consensus.domain.entity.Team;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for {@link Team} persistence.
 */
public interface TeamRepository {

    Team save(Team team);

    Optional<Team> findById(UUID id);

    List<Team> findByElectoralProcessId(UUID electoralProcessId);

    void delete(Team team);

    boolean existsById(UUID id);

    boolean existsByProcessId(UUID processId);

    boolean existsByElectoralProcessIdAndName(UUID electoralProcessId, String name);

    List<Team> saveAll(List<Team> teams);

    List<String> findNamesByProcessIdAndNamesIn(UUID processId, List<String> names);
}
