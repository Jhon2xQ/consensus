package com.carmenio.consensus.infrastructure.repository;

import com.carmenio.consensus.domain.entity.ElectoralProcess;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for {@link JpaElectoralProcessRepositoryAdapter}.
 * <p>
 * Uses {@link DataJpaTest} with H2 in-memory database.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import({JpaElectoralProcessRepositoryAdapter.class, com.carmenio.consensus.common.config.JpaConfig.class})
class JpaElectoralProcessRepositoryAdapterTest {

    @Autowired
    private ElectoralProcessRepository repository;

    @Autowired
    private EntityManager entityManager;

    private ElectoralProcess sampleProcess;

    @BeforeEach
    void setUp() {
        sampleProcess = ElectoralProcess.builder()
                .name("Presidential Election 2026")
                .scope("presidential-2026")
                .commitmentStart(Instant.parse("2026-06-01T00:00:00Z"))
                .commitmentEnd(Instant.parse("2026-06-10T00:00:00Z"))
                .votingStart(Instant.parse("2026-07-01T00:00:00Z"))
                .votingEnd(Instant.parse("2026-07-10T00:00:00Z"))
                .results(Instant.parse("2026-08-01T00:00:00Z"))
                .build();
    }

    @Test
    @DisplayName("should save and find by id")
    void shouldSaveAndFindById() {
        var saved = repository.save(sampleProcess);

        assertNotNull(saved.getId(), "ID should be generated after save");

        var found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Presidential Election 2026", found.get().getName());
        assertEquals("presidential-2026", found.get().getScope());
    }

    @Test
    @DisplayName("should return empty when id not found")
    void shouldReturnEmptyWhenIdNotFound() {
        var result = repository.findById(UUID.randomUUID());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should find by scope")
    void shouldFindByScope() {
        repository.save(sampleProcess);

        var found = repository.findByScope("presidential-2026");
        assertTrue(found.isPresent());
        assertEquals("Presidential Election 2026", found.get().getName());
    }

    @Test
    @DisplayName("should return empty when scope not found")
    void shouldReturnEmptyWhenScopeNotFound() {
        var result = repository.findByScope("non-existent-scope");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should check existence by name")
    void shouldCheckExistenceByName() {
        repository.save(sampleProcess);

        assertTrue(repository.existsByName("Presidential Election 2026"));
        assertFalse(repository.existsByName("Non-existent Process"));
    }

    @Test
    @DisplayName("should check existence by scope")
    void shouldCheckExistenceByScope() {
        repository.save(sampleProcess);

        assertTrue(repository.existsByScope("presidential-2026"));
        assertFalse(repository.existsByScope("non-existent-scope"));
    }

    @Test
    @DisplayName("should return all processes with pagination")
    void shouldReturnAllWithPagination() {
        for (int i = 0; i < 5; i++) {
            repository.save(ElectoralProcess.builder()
                    .name("Process " + i)
                    .scope("scope-" + i)
                    .commitmentStart(Instant.parse("2026-06-01T00:00:00Z"))
                    .commitmentEnd(Instant.parse("2026-06-10T00:00:00Z"))
                    .votingStart(Instant.parse("2026-07-01T00:00:00Z"))
                    .votingEnd(Instant.parse("2026-07-10T00:00:00Z"))
                    .results(Instant.parse("2026-08-01T00:00:00Z"))
                    .build());
        }

        var page = repository.findAll(PageRequest.of(0, 2));
        assertEquals(2, page.getContent().size());
        assertEquals(5, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
    }

    @Test
    @DisplayName("should delete a process")
    void shouldDeleteProcess() {
        var saved = repository.save(sampleProcess);
        var id = saved.getId();

        repository.delete(saved);

        var found = repository.findById(id);
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("should enforce unique name constraint")
    void shouldEnforceUniqueNameConstraint() {
        repository.save(sampleProcess);
        entityManager.flush();  // force INSERT to DB so constraint is active

        var duplicate = ElectoralProcess.builder()
                .name("Presidential Election 2026")  // same name
                .scope("different-scope")
                .commitmentStart(Instant.parse("2026-06-01T00:00:00Z"))
                .commitmentEnd(Instant.parse("2026-06-10T00:00:00Z"))
                .votingStart(Instant.parse("2026-07-01T00:00:00Z"))
                .votingEnd(Instant.parse("2026-07-10T00:00:00Z"))
                .results(Instant.parse("2026-08-01T00:00:00Z"))
                .build();

        repository.save(duplicate);
        assertThrows(Exception.class, () -> entityManager.flush());
    }

    @Test
    @DisplayName("should enforce unique scope constraint")
    void shouldEnforceUniqueScopeConstraint() {
        repository.save(sampleProcess);
        entityManager.flush();  // force INSERT to DB so constraint is active

        var duplicate = ElectoralProcess.builder()
                .name("Different Process")
                .scope("presidential-2026")  // same scope
                .commitmentStart(Instant.parse("2026-06-01T00:00:00Z"))
                .commitmentEnd(Instant.parse("2026-06-10T00:00:00Z"))
                .votingStart(Instant.parse("2026-07-01T00:00:00Z"))
                .votingEnd(Instant.parse("2026-07-10T00:00:00Z"))
                .results(Instant.parse("2026-08-01T00:00:00Z"))
                .build();

        repository.save(duplicate);
        assertThrows(Exception.class, () -> entityManager.flush());
    }
}
