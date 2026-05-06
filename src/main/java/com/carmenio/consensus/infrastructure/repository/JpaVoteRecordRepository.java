package com.carmenio.consensus.infrastructure.repository;

import com.carmenio.consensus.domain.entity.VoteRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link VoteRecord}.
 * <p>
 * Not exposed outside the infrastructure layer — use
 * {@link JpaVoteRecordRepositoryAdapter} instead.
 */
@Repository
interface JpaVoteRecordRepository extends JpaRepository<VoteRecord, UUID> {

    Optional<VoteRecord> findByNullifier(String nullifier);

    List<VoteRecord> findByScope(String scope);

    List<VoteRecord> findAllByScope(String scope);

    boolean existsByNullifier(String nullifier);

    long countByScope(String scope);
}
