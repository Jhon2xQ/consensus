package com.carmenio.consensus.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link VoteRecord} entity.
 * <p>
 * Verifies entity creation, builder usage, field access, and nullable fields.
 */
class VoteRecordTest {

    @Test
    @DisplayName("Should create vote record with all fields using builder")
    void shouldCreateVoteRecordWithAllFields() {
        var id = UUID.randomUUID();
        var groupId = "1";
        var nullifier = "11111111111111111111111111111111";
        var message = "Team Alpha";
        var scope = "33333333333333333333333333333333";
        var transactionHash = "0xabc123def456";

        var record = VoteRecord.builder()
                .id(id)
                .groupId(groupId)
                .nullifier(nullifier)
                .message(message)
                .scope(scope)
                .transactionHash(transactionHash)
                .build();

        assertNotNull(record);
        assertEquals(id, record.getId());
        assertEquals(groupId, record.getGroupId());
        assertEquals(nullifier, record.getNullifier());
        assertEquals(message, record.getMessage());
        assertEquals(scope, record.getScope());
        assertEquals(transactionHash, record.getTransactionHash());
    }

    @Test
    @DisplayName("Should create vote record with minimal required fields")
    void shouldCreateVoteRecordWithMinimalFields() {
        var groupId = "2";
        var nullifier = "22222222222222222222222222222222";
        var message = "Team Beta";
        var scope = "44444444444444444444444444444444";

        var record = VoteRecord.builder()
                .groupId(groupId)
                .nullifier(nullifier)
                .message(message)
                .scope(scope)
                .build();

        assertNotNull(record);
        assertEquals(groupId, record.getGroupId());
        assertEquals(nullifier, record.getNullifier());
        assertEquals(message, record.getMessage());
        assertEquals(scope, record.getScope());
        assertNull(record.getTransactionHash(), "transactionHash should be null when not set");
    }

    @Test
    @DisplayName("Should create vote record and verify default transactionHash is null")
    void shouldHaveNullTransactionHashByDefault() {
        var record = VoteRecord.builder()
                .groupId("3")
                .nullifier("33333333333333333333333333333333")
                .message("Team Gamma")
                .scope("55555555555555555555555555555555")
                .build();

        assertNull(record.getTransactionHash());
    }
}
