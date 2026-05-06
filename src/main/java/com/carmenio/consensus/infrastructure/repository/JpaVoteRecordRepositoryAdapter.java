package com.carmenio.consensus.infrastructure.repository;

import com.carmenio.consensus.domain.entity.VoteRecord;
import com.carmenio.consensus.domain.repository.VoteRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that implements the domain {@link VoteRecordRepository} port
 * by delegating to Spring Data JPA.
 */
@Component
@RequiredArgsConstructor
public class JpaVoteRecordRepositoryAdapter implements VoteRecordRepository {

    private final JpaVoteRecordRepository jpaRepository;

    @Override
    public VoteRecord save(VoteRecord voteRecord) {
        return jpaRepository.save(voteRecord);
    }

    @Override
    public Optional<VoteRecord> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<VoteRecord> findByNullifier(String nullifier) {
        return jpaRepository.findByNullifier(nullifier);
    }

    @Override
    public List<VoteRecord> findByScope(String scope) {
        return jpaRepository.findByScope(scope);
    }

    @Override
    public List<VoteRecord> findAllByScope(String scope) {
        return jpaRepository.findAllByScope(scope);
    }

    @Override
    public void delete(VoteRecord voteRecord) {
        jpaRepository.delete(voteRecord);
    }

    @Override
    public boolean existsByNullifier(String nullifier) {
        return jpaRepository.existsByNullifier(nullifier);
    }

    @Override
    public long countByScope(String scope) {
        return jpaRepository.countByScope(scope);
    }
}
