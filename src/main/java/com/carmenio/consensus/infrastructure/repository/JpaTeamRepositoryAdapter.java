package com.carmenio.consensus.infrastructure.repository;

import com.carmenio.consensus.domain.entity.Team;
import com.carmenio.consensus.domain.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that implements the domain {@link TeamRepository} port
 * by delegating to Spring Data JPA.
 */
@Component
@RequiredArgsConstructor
public class JpaTeamRepositoryAdapter implements TeamRepository {

    private final JpaTeamRepository jpaRepository;

    @Override
    public Team save(Team team) {
        return jpaRepository.save(team);
    }

    @Override
    public Optional<Team> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Team> findByElectoralProcessId(UUID electoralProcessId) {
        return jpaRepository.findByElectoralProcessId(electoralProcessId);
    }

    @Override
    public void delete(Team team) {
        jpaRepository.delete(team);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public boolean existsByProcessId(UUID processId) {
        return jpaRepository.existsByElectoralProcessId(processId);
    }
}
