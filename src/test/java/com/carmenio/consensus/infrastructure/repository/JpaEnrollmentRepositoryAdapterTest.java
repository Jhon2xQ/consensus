package com.carmenio.consensus.infrastructure.repository;

import com.carmenio.consensus.domain.entity.Enrollment;
import com.carmenio.consensus.domain.repository.EnrollmentRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for {@link JpaEnrollmentRepositoryAdapter}.
 * <p>
 * Uses {@link DataJpaTest} with H2 in-memory database.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(JpaEnrollmentRepositoryAdapter.class)
class JpaEnrollmentRepositoryAdapterTest {

    @Autowired
    private EnrollmentRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Should save and find enrollment by ID")
    void shouldSaveAndFindById() {
        var processId = UUID.randomUUID();
        var enrollment = Enrollment.builder()
                .electoralProcessId(processId)
                .userId("user-1")
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .build();

        var saved = repository.save(enrollment);
        assertNotNull(saved.getId());

        var found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("user-1", found.get().getUserId());
        assertEquals(processId, found.get().getElectoralProcessId());
        assertFalse(found.get().isHasVoted());
    }

    @Test
    @DisplayName("Should return empty when id not found")
    void shouldReturnEmptyWhenIdNotFound() {
        var result = repository.findById(UUID.randomUUID());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find enrollments by electoral process ID")
    void shouldFindByElectoralProcessId() {
        var processId = UUID.randomUUID();
        repository.save(Enrollment.builder()
                .electoralProcessId(processId)
                .userId("user-1")
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .build());
        repository.save(Enrollment.builder()
                .electoralProcessId(processId)
                .userId("user-2")
                .commitment("2222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222")
                .build());

        var enrollments = repository.findByElectoralProcessId(processId);
        assertEquals(2, enrollments.size());
    }

    @Test
    @DisplayName("Should return empty list for process with no enrollments")
    void shouldReturnEmptyWhenNoEnrollments() {
        var enrollments = repository.findByElectoralProcessId(UUID.randomUUID());
        assertTrue(enrollments.isEmpty());
    }

    @Test
    @DisplayName("Should check existence by process ID and user ID")
    void shouldCheckExistsByProcessIdAndUserId() {
        var processId = UUID.randomUUID();
        repository.save(Enrollment.builder()
                .electoralProcessId(processId)
                .userId("user-1")
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .build());

        assertTrue(repository.existsByElectoralProcessIdAndUserId(processId, "user-1"));
        assertFalse(repository.existsByElectoralProcessIdAndUserId(processId, "user-unknown"));
        assertFalse(repository.existsByElectoralProcessIdAndUserId(UUID.randomUUID(), "user-1"));
    }

    @Test
    @DisplayName("Should check existence by process ID and commitment")
    void shouldCheckExistsByProcessIdAndCommitment() {
        var processId = UUID.randomUUID();
        var commitment = "1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111";
        repository.save(Enrollment.builder()
                .electoralProcessId(processId)
                .userId("user-1")
                .commitment(commitment)
                .build());

        assertTrue(repository.existsByElectoralProcessIdAndCommitment(processId, commitment));
        assertFalse(repository.existsByElectoralProcessIdAndCommitment(processId, "different-commitment"));
    }

    @Test
    @DisplayName("Should count enrollments by process ID")
    void shouldCountByProcessId() {
        var processId = UUID.randomUUID();
        assertEquals(0, repository.countByElectoralProcessId(processId));

        repository.save(Enrollment.builder()
                .electoralProcessId(processId)
                .userId("user-1")
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .build());
        repository.save(Enrollment.builder()
                .electoralProcessId(processId)
                .userId("user-2")
                .commitment("2222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222")
                .build());

        assertEquals(2, repository.countByElectoralProcessId(processId));
    }

    @Test
    @DisplayName("Should delete an enrollment")
    void shouldDelete() {
        var enrollment = Enrollment.builder()
                .electoralProcessId(UUID.randomUUID())
                .userId("user-1")
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .build();

        var saved = repository.save(enrollment);
        assertTrue(repository.existsById(saved.getId()));

        repository.delete(saved);
        assertFalse(repository.existsById(saved.getId()));
    }

    @Test
    @DisplayName("Should enforce unique (processId, userId) constraint")
    void shouldEnforceUniqueProcessIdUserId() {
        var processId = UUID.randomUUID();
        var enrollment = Enrollment.builder()
                .electoralProcessId(processId)
                .userId("user-1")
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .build();

        repository.save(enrollment);
        entityManager.flush();

        var duplicate = Enrollment.builder()
                .electoralProcessId(processId)
                .userId("user-1")  // same userId within same process
                .commitment("2222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222")
                .build();

        repository.save(duplicate);
        assertThrows(Exception.class, () -> entityManager.flush());
    }

    @Test
    @DisplayName("Should enforce unique (processId, commitment) constraint")
    void shouldEnforceUniqueProcessIdCommitment() {
        var processId = UUID.randomUUID();
        var commitment = "1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111";
        var enrollment = Enrollment.builder()
                .electoralProcessId(processId)
                .userId("user-1")
                .commitment(commitment)
                .build();

        repository.save(enrollment);
        entityManager.flush();

        var duplicate = Enrollment.builder()
                .electoralProcessId(processId)
                .userId("user-2")  // different user
                .commitment(commitment)  // same commitment within same process
                .build();

        repository.save(duplicate);
        assertThrows(Exception.class, () -> entityManager.flush());
    }
}
