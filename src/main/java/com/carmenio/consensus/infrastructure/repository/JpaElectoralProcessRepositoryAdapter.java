package com.carmenio.consensus.infrastructure.repository;

import com.carmenio.consensus.domain.entity.ElectoralProcess;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that implements the domain {@link ElectoralProcessRepository} port
 * by delegating to Spring Data JPA.
 */
@Component
@RequiredArgsConstructor
public class JpaElectoralProcessRepositoryAdapter implements ElectoralProcessRepository {

    private final JpaElectoralProcessRepository jpaRepository;

    @Override
    public ElectoralProcess save(ElectoralProcess process) {
        return jpaRepository.save(process);
    }

    @Override
    public Optional<ElectoralProcess> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Page<ElectoralProcess> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }

    @Override
    public Optional<ElectoralProcess> findByScope(String scope) {
        return jpaRepository.findByScope(scope);
    }

    @Override
    public void delete(ElectoralProcess process) {
        jpaRepository.delete(process);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public boolean existsByScope(String scope) {
        return jpaRepository.existsByScope(scope);
    }

    @Override
    public Page<ElectoralProcess> findByCreatedBy(String createdBy, Pageable pageable) {
        return jpaRepository.findByCreatedBy(createdBy, pageable);
    }
}
