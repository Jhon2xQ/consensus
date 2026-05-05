package com.carmenio.consensus.domain.repository;

import com.carmenio.consensus.domain.entity.Enrollment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for {@link Enrollment} persistence.
 */
public interface EnrollmentRepository {

    Enrollment save(Enrollment enrollment);

    Optional<Enrollment> findById(UUID id);

    List<Enrollment> findByElectoralProcessId(UUID electoralProcessId);

    void delete(Enrollment enrollment);

    boolean existsById(UUID id);

    boolean existsByElectoralProcessIdAndUserId(UUID electoralProcessId, String userId);

    boolean existsByElectoralProcessIdAndCommitment(UUID electoralProcessId, String commitment);

    long countByElectoralProcessId(UUID electoralProcessId);
}
