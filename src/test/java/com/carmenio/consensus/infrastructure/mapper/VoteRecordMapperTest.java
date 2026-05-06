package com.carmenio.consensus.infrastructure.mapper;

import com.carmenio.consensus.application.dto.record.CreateVoteRecordRequest;
import com.carmenio.consensus.application.dto.record.ProcessResultsResponse;
import com.carmenio.consensus.application.dto.record.TeamResult;
import com.carmenio.consensus.application.dto.record.VoteRecordResponse;
import com.carmenio.consensus.domain.entity.VoteRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link VoteRecordMapper}.
 * <p>
 * Verifies entity ↔ DTO conversion mappings.
 */
class VoteRecordMapperTest {

    private final VoteRecordMapper mapper = new VoteRecordMapper();

    @Test
    @DisplayName("Should map CreateVoteRecordRequest to VoteRecord entity")
    void shouldMapRequestToEntity() {
        var request = CreateVoteRecordRequest.builder()
                .groupId("1")
                .nullifier("11111111111111111111111111111111")
                .message("Team Alpha")
                .scope("33333333333333333333333333333333")
                .transactionHash("0xabc123")
                .build();

        var entity = mapper.toEntity(request);

        assertNull(entity.getId(), "ID should be null for JPA generation");
        assertEquals("1", entity.getGroupId());
        assertEquals("11111111111111111111111111111111", entity.getNullifier());
        assertEquals("Team Alpha", entity.getMessage());
        assertEquals("33333333333333333333333333333333", entity.getScope());
        assertEquals("0xabc123", entity.getTransactionHash());
    }

    @Test
    @DisplayName("Should map VoteRecord entity to VoteRecordResponse")
    void shouldMapEntityToResponse() {
        var id = UUID.randomUUID();
        var createdAt = LocalDateTime.now();
        var entity = VoteRecord.builder()
                .id(id)
                .groupId("1")
                .nullifier("11111111111111111111111111111111")
                .message("Team Alpha")
                .scope("33333333333333333333333333333333")
                .transactionHash("0xabc123")
                .createdAt(createdAt)
                .build();

        var response = mapper.toResponse(entity);

        assertEquals(id, response.getId());
        assertEquals("1", response.getGroupId());
        assertEquals("11111111111111111111111111111111", response.getNullifier());
        assertEquals("Team Alpha", response.getMessage());
        assertEquals("33333333333333333333333333333333", response.getScope());
        assertEquals("0xabc123", response.getTransactionHash());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    @DisplayName("Should map entity with null transactionHash to response")
    void shouldMapEntityWithNullTransactionHash() {
        var entity = VoteRecord.builder()
                .id(UUID.randomUUID())
                .groupId("2")
                .nullifier("22222222222222222222222222222222")
                .message("Team Beta")
                .scope("44444444444444444444444444444444")
                .createdAt(LocalDateTime.now())
                .build();

        var response = mapper.toResponse(entity);

        assertEquals("2", response.getGroupId());
        assertNull(response.getTransactionHash());
    }

    @Test
    @DisplayName("Should map request with null transactionHash")
    void shouldMapRequestWithNullTransactionHash() {
        var request = CreateVoteRecordRequest.builder()
                .groupId("3")
                .nullifier("33333333333333333333333333333333")
                .message("Team Gamma")
                .scope("55555555555555555555555555555555")
                .build();

        var entity = mapper.toEntity(request);
        assertNull(entity.getTransactionHash());
    }

    @Test
    @DisplayName("Should create CreateVoteRecordRequest with all fields via builder")
    void shouldCreateCreateVoteRecordRequest() {
        var request = CreateVoteRecordRequest.builder()
                .groupId("1")
                .nullifier("11111111111111111111111111111111")
                .message("Team Alpha")
                .scope("33333333333333333333333333333333")
                .transactionHash("0xabc123")
                .build();

        assertNotNull(request);
        assertEquals("1", request.getGroupId());
        assertEquals("11111111111111111111111111111111", request.getNullifier());
        assertEquals("Team Alpha", request.getMessage());
        assertEquals("33333333333333333333333333333333", request.getScope());
        assertEquals("0xabc123", request.getTransactionHash());
    }

    @Test
    @DisplayName("Should create VoteRecordResponse with all fields via builder")
    void shouldCreateVoteRecordResponse() {
        var id = UUID.randomUUID();
        var createdAt = LocalDateTime.now();
        var response = VoteRecordResponse.builder()
                .id(id)
                .groupId("1")
                .nullifier("11111111111111111111111111111111")
                .message("Team Alpha")
                .scope("33333333333333333333333333333333")
                .transactionHash("0xabc123")
                .createdAt(createdAt)
                .build();

        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals("1", response.getGroupId());
        assertEquals("11111111111111111111111111111111", response.getNullifier());
        assertEquals("Team Alpha", response.getMessage());
        assertEquals("33333333333333333333333333333333", response.getScope());
        assertEquals("0xabc123", response.getTransactionHash());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    @DisplayName("Should create ProcessResultsResponse with teams and total votes")
    void shouldCreateProcessResultsResponse() {
        var teamResults = java.util.List.of(
                new TeamResult("Team Alpha", 3L),
                new TeamResult("Team Beta", 2L)
        );

        var response = ProcessResultsResponse.builder()
                .processId(UUID.randomUUID())
                .processName("Test Process")
                .teamResults(teamResults)
                .totalVotes(5L)
                .status("CLOSED")
                .build();

        assertNotNull(response);
        assertEquals(5L, response.getTotalVotes());
        assertEquals(2, response.getTeamResults().size());
        assertEquals("Team Alpha", response.getTeamResults().get(0).getTeamName());
        assertEquals(3L, response.getTeamResults().get(0).getVoteCount());
    }

    @Test
    @DisplayName("Should create TeamResult with name and vote count")
    void shouldCreateTeamResult() {
        var result = new TeamResult("Team Gamma", 7L);

        assertEquals("Team Gamma", result.getTeamName());
        assertEquals(7L, result.getVoteCount());
    }
}
