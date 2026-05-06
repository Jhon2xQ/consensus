package com.carmenio.consensus.infrastructure.repository;

import com.carmenio.consensus.domain.entity.VoteRecord;
import com.carmenio.consensus.domain.repository.VoteRecordRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for {@link JpaVoteRecordRepositoryAdapter}.
 * <p>
 * Uses {@link DataJpaTest} with H2 in-memory database.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(JpaVoteRecordRepositoryAdapter.class)
class JpaVoteRecordRepositoryAdapterTest {

    @Autowired
    private VoteRecordRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Should save and find vote record by ID")
    void shouldSaveAndFindById() {
        var record = VoteRecord.builder()
                .groupId("1")
                .nullifier("11111111111111111111111111111111")
                .message("Team Alpha")
                .scope("33333333333333333333333333333333")
                .build();

        var saved = repository.save(record);
        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt(), "createdAt should be auto-set on persist");

        var found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("11111111111111111111111111111111", found.get().getNullifier());
        assertEquals("Team Alpha", found.get().getMessage());
        assertEquals("33333333333333333333333333333333", found.get().getScope());
        assertEquals("1", found.get().getGroupId());
    }

    @Test
    @DisplayName("Should return empty when id not found")
    void shouldReturnEmptyWhenIdNotFound() {
        var result = repository.findById(UUID.randomUUID());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find vote record by nullifier")
    void shouldFindByNullifier() {
        var nullifier = "22222222222222222222222222222222";
        repository.save(VoteRecord.builder()
                .groupId("2")
                .nullifier(nullifier)
                .message("Team Beta")
                .scope("44444444444444444444444444444444")
                .build());

        var found = repository.findByNullifier(nullifier);
        assertTrue(found.isPresent());
        assertEquals("Team Beta", found.get().getMessage());

        var notFound = repository.findByNullifier("nonexistent");
        assertTrue(notFound.isEmpty());
    }

    @Test
    @DisplayName("Should find vote records by scope")
    void shouldFindByScope() {
        var scope = "55555555555555555555555555555555";
        repository.save(VoteRecord.builder()
                .groupId("1")
                .nullifier("aaaa")
                .message("Team Alpha")
                .scope(scope)
                .build());
        repository.save(VoteRecord.builder()
                .groupId("1")
                .nullifier("bbbb")
                .message("Team Beta")
                .scope(scope)
                .build());

        var records = repository.findByScope(scope);
        assertEquals(2, records.size());

        var noRecords = repository.findByScope("nonexistent");
        assertTrue(noRecords.isEmpty());
    }

    @Test
    @DisplayName("Should find all vote records by scope")
    void shouldFindAllByScope() {
        var scope = "66666666666666666666666666666666";
        repository.save(VoteRecord.builder()
                .groupId("1")
                .nullifier("cccc")
                .message("Team Alpha")
                .scope(scope)
                .build());
        repository.save(VoteRecord.builder()
                .groupId("1")
                .nullifier("dddd")
                .message("Team Beta")
                .scope(scope)
                .build());

        var records = repository.findAllByScope(scope);
        assertEquals(2, records.size());
    }

    @Test
    @DisplayName("Should check existence by nullifier")
    void shouldCheckExistsByNullifier() {
        var nullifier = "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee";
        repository.save(VoteRecord.builder()
                .groupId("1")
                .nullifier(nullifier)
                .message("Team Gamma")
                .scope("77777777777777777777777777777777")
                .build());

        assertTrue(repository.existsByNullifier(nullifier));
        assertFalse(repository.existsByNullifier("unknown"));
    }

    @Test
    @DisplayName("Should count vote records by scope")
    void shouldCountByScope() {
        var scope = "88888888888888888888888888888888";
        assertEquals(0, repository.countByScope(scope));

        repository.save(VoteRecord.builder()
                .groupId("1")
                .nullifier("ffff")
                .message("Team Alpha")
                .scope(scope)
                .build());
        repository.save(VoteRecord.builder()
                .groupId("1")
                .nullifier("gggg")
                .message("Team Beta")
                .scope(scope)
                .build());

        assertEquals(2, repository.countByScope(scope));
    }

    @Test
    @DisplayName("Should delete a vote record")
    void shouldDelete() {
        var record = VoteRecord.builder()
                .groupId("1")
                .nullifier("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh")
                .message("Team Delta")
                .scope("99999999999999999999999999999999")
                .build();

        var saved = repository.save(record);
        assertTrue(repository.existsByNullifier("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh"));

        repository.delete(saved);
        assertFalse(repository.existsByNullifier("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh"));
    }

    @Test
    @DisplayName("Should enforce unique nullifier constraint")
    void shouldEnforceUniqueNullifier() {
        var nullifier = "iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii";
        repository.save(VoteRecord.builder()
                .groupId("1")
                .nullifier(nullifier)
                .message("Team Alpha")
                .scope("scope-1")
                .build());
        entityManager.flush();

        var duplicate = VoteRecord.builder()
                .groupId("2")
                .nullifier(nullifier)  // same nullifier
                .message("Team Beta")
                .scope("scope-2")
                .build();

        repository.save(duplicate);
        assertThrows(Exception.class, () -> entityManager.flush());
    }

    @Test
    @DisplayName("Should return empty list for scope with no records")
    void shouldReturnEmptyListForNonExistentScope() {
        var findAllResult = repository.findAllByScope("nonexistent-scope");
        assertTrue(findAllResult.isEmpty());

        var findByScopeResult = repository.findByScope("nonexistent-scope");
        assertTrue(findByScopeResult.isEmpty());
    }
}
