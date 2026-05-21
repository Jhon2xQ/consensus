package com.carmenio.consensus.domain.repository;

import com.carmenio.consensus.domain.entity.VoteRecord;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for {@link VoteRecord} persistence.
 */
public interface VoteRecordRepository {

    VoteRecord save(VoteRecord voteRecord);

    Optional<VoteRecord> findById(UUID id);

    Optional<VoteRecord> findByNullifier(String nullifier);

    List<VoteRecord> findByScope(String scope);

    List<VoteRecord> findAllByScope(String scope);

    void delete(VoteRecord voteRecord);

    boolean existsByNullifier(String nullifier);

    long countByScope(String scope);

    /**
     * Returns a paginated view of all vote records.
     *
     * @param pageable pagination information (page number, size, sort)
     * @return a page of vote records
     */
    Page<VoteRecord> findAll(Pageable pageable);
}
