package com.carmenio.consensus.infrastructure.repository;

import com.carmenio.consensus.domain.entity.Enrollment;
import com.carmenio.consensus.domain.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that implements the domain {@link EnrollmentRepository} port
 * by delegating to Spring Data JPA.
 */
@Component
@RequiredArgsConstructor
public class JpaEnrollmentRepositoryAdapter implements EnrollmentRepository {

    private final JpaEnrollmentRepository jpaRepository;

    @Override
    public Enrollment save(Enrollment enrollment) {
        return jpaRepository.save(enrollment);
    }

    @Override
    public Optional<Enrollment> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Enrollment> findByElectoralProcessId(UUID electoralProcessId) {
        return jpaRepository.findByElectoralProcessId(electoralProcessId);
    }

    @Override
    public void delete(Enrollment enrollment) {
        jpaRepository.delete(enrollment);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public boolean existsByElectoralProcessIdAndUserId(UUID electoralProcessId, String userId) {
        return jpaRepository.existsByElectoralProcessIdAndUserId(electoralProcessId, userId);
    }

    @Override
    public boolean existsByElectoralProcessIdAndCommitment(UUID electoralProcessId, String commitment) {
        return jpaRepository.existsByElectoralProcessIdAndCommitment(electoralProcessId, commitment);
    }

    @Override
    public Optional<Enrollment> findByElectoralProcessIdAndEmail(UUID electoralProcessId, String email) {
        return jpaRepository.findByElectoralProcessIdAndEmail(electoralProcessId, email);
    }

    @Override
    public boolean existsByElectoralProcessIdAndEmail(UUID electoralProcessId, String email) {
        return jpaRepository.existsByElectoralProcessIdAndEmail(electoralProcessId, email);
    }

    @Override
    public long countByElectoralProcessId(UUID electoralProcessId) {
        return jpaRepository.countByElectoralProcessId(electoralProcessId);
    }

    @Override
    public List<Enrollment> saveAll(List<Enrollment> enrollments) {
        return jpaRepository.saveAll(enrollments);
    }

    @Override
    public List<String> findEmailsByProcessIdAndEmailsIn(UUID processId, List<String> emails) {
        return jpaRepository.findEmailsByElectoralProcessIdAndEmailIn(processId, emails);
    }
}
