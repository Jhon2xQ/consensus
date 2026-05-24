package com.carmenio.consensus.infrastructure.repository;

import com.carmenio.consensus.domain.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Enrollment}.
 * <p>
 * Not exposed outside the infrastructure layer — use
 * {@link JpaEnrollmentRepositoryAdapter} instead.
 */
@Repository
interface JpaEnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    List<Enrollment> findByElectoralProcessId(UUID electoralProcessId);

    boolean existsByElectoralProcessIdAndUserId(UUID electoralProcessId, String userId);

    boolean existsByElectoralProcessIdAndCommitment(UUID electoralProcessId, String commitment);

    java.util.Optional<Enrollment> findByElectoralProcessIdAndEmail(UUID electoralProcessId, String email);

    boolean existsByElectoralProcessIdAndEmail(UUID electoralProcessId, String email);

    long countByElectoralProcessId(UUID electoralProcessId);

    @Query("SELECT e.email FROM Enrollment e WHERE e.electoralProcessId = :processId AND e.email IN :emails")
    List<String> findEmailsByElectoralProcessIdAndEmailIn(UUID processId, List<String> emails);
}
