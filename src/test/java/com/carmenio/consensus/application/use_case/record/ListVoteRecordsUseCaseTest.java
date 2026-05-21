package com.carmenio.consensus.application.use_case.record;

import com.carmenio.consensus.application.dto.PaginatedResponse;
import com.carmenio.consensus.application.dto.record.VoteRecordResponse;
import com.carmenio.consensus.domain.entity.VoteRecord;
import com.carmenio.consensus.domain.repository.VoteRecordRepository;
import com.carmenio.consensus.infrastructure.mapper.VoteRecordMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListVoteRecordsUseCaseTest {

    @Mock
    private VoteRecordRepository repository;

    @Mock
    private VoteRecordMapper mapper;

    @InjectMocks
    private ListVoteRecordsUseCase useCase;

    @Test
    @DisplayName("should return paginated records when no scope")
    void shouldReturnPaginatedRecordsWhenNoScope() {
        var pageable = PageRequest.of(0, 10);
        var now = Instant.now();

        var entity1 = VoteRecord.builder()
                .id(UUID.randomUUID())
                .groupId("1")
                .nullifier("nullifier-1")
                .message("Team Alpha")
                .scope("scope-1")
                .transactionHash("0xabc")
                .createdAt(now)
                .build();

        var entity2 = VoteRecord.builder()
                .id(UUID.randomUUID())
                .groupId("1")
                .nullifier("nullifier-2")
                .message("Team Beta")
                .scope("scope-1")
                .transactionHash("0xdef")
                .createdAt(now)
                .build();

        Page<VoteRecord> page = new PageImpl<>(List.of(entity1, entity2), pageable, 2);

        when(repository.findAll(pageable)).thenReturn(page);
        when(mapper.toResponse(entity1)).thenReturn(
                VoteRecordResponse.builder()
                        .id(entity1.getId()).groupId("1").nullifier("nullifier-1")
                        .message("Team Alpha").scope("scope-1").transactionHash("0xabc")
                        .createdAt(now).build());
        when(mapper.toResponse(entity2)).thenReturn(
                VoteRecordResponse.builder()
                        .id(entity2.getId()).groupId("1").nullifier("nullifier-2")
                        .message("Team Beta").scope("scope-1").transactionHash("0xdef")
                        .createdAt(now).build());

        var result = useCase.executePaginated(pageable);

        assertAll("paginated records",
                () -> assertNotNull(result),
                () -> assertEquals(2, result.getContent().size()),
                () -> assertEquals("Team Alpha", result.getContent().get(0).getMessage()),
                () -> assertEquals("Team Beta", result.getContent().get(1).getMessage()),
                () -> assertEquals(0, result.getPage()),
                () -> assertEquals(10, result.getSize()),
                () -> assertEquals(2L, result.getTotalElements()),
                () -> assertEquals(1, result.getTotalPages())
        );

        verify(repository).findAll(pageable);
    }

    @Test
    @DisplayName("should return all records by scope")
    void shouldReturnAllRecordsByScope() {
        var scope = "scope-1";
        var now = Instant.now();

        var entity = VoteRecord.builder()
                .id(UUID.randomUUID())
                .groupId("1")
                .nullifier("nullifier-1")
                .message("Team Alpha")
                .scope(scope)
                .transactionHash("0xabc")
                .createdAt(now)
                .build();

        when(repository.findByScope(scope)).thenReturn(List.of(entity));
        when(mapper.toResponse(entity)).thenReturn(
                VoteRecordResponse.builder()
                        .id(entity.getId()).groupId("1").nullifier("nullifier-1")
                        .message("Team Alpha").scope(scope).transactionHash("0xabc")
                        .createdAt(now).build());

        var result = useCase.executeByScope(scope);

        assertAll("records by scope",
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size()),
                () -> assertEquals("Team Alpha", result.get(0).getMessage()),
                () -> assertEquals(scope, result.get(0).getScope())
        );

        verify(repository).findByScope(scope);
    }

    @Test
    @DisplayName("should return empty page when no records exist")
    void shouldReturnEmptyPageWhenNoRecords() {
        var pageable = PageRequest.of(0, 10);
        var emptyPage = Page.<VoteRecord>empty(pageable);

        when(repository.findAll(pageable)).thenReturn(emptyPage);

        var result = useCase.executePaginated(pageable);

        assertAll("empty paginated result",
                () -> assertNotNull(result),
                () -> assertTrue(result.getContent().isEmpty()),
                () -> assertEquals(0L, result.getTotalElements()),
                () -> assertEquals(0, result.getTotalPages())
        );
    }

    @Test
    @DisplayName("should return empty list for unknown scope")
    void shouldReturnEmptyListForUnknownScope() {
        var unknownScope = "unknown-scope";

        when(repository.findByScope(unknownScope)).thenReturn(List.of());

        var result = useCase.executeByScope(unknownScope);

        assertAll("empty list for unknown scope",
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty())
        );
    }

    @Test
    @DisplayName("should paginate correctly with Spring Page metadata")
    void shouldPaginateCorrectly() {
        var pageable = PageRequest.of(1, 5);
        var now = Instant.now();

        var entity = VoteRecord.builder()
                .id(UUID.randomUUID())
                .groupId("1")
                .nullifier("nullifier-x")
                .message("Team X")
                .scope("scope-x")
                .transactionHash("0xghi")
                .createdAt(now)
                .build();

        var entity2 = VoteRecord.builder()
                .id(UUID.randomUUID())
                .groupId("1")
                .nullifier("nullifier-y")
                .message("Team Y")
                .scope("scope-x")
                .transactionHash("0xjkl")
                .createdAt(now)
                .build();

        var entities = List.of(entity, entity2);
        // Second page of 5 items, total 7 → page 1 has items 5-6 (2 items)
        Page<VoteRecord> page = new PageImpl<>(entities, pageable, 7);

        when(repository.findAll(pageable)).thenReturn(page);
        when(mapper.toResponse(entity)).thenReturn(
                VoteRecordResponse.builder()
                        .id(entity.getId()).groupId("1").nullifier("nullifier-x")
                        .message("Team X").scope("scope-x").transactionHash("0xghi")
                        .createdAt(now).build());
        when(mapper.toResponse(entity2)).thenReturn(
                VoteRecordResponse.builder()
                        .id(entity2.getId()).groupId("1").nullifier("nullifier-y")
                        .message("Team Y").scope("scope-x").transactionHash("0xjkl")
                        .createdAt(now).build());

        var result = useCase.executePaginated(pageable);

        assertAll("pagination metadata",
                () -> assertEquals(1, result.getPage()),
                () -> assertEquals(5, result.getSize()),
                () -> assertEquals(7L, result.getTotalElements()),
                () -> assertEquals(2, result.getTotalPages()),
                () -> assertFalse(result.isFirst()),
                () -> assertTrue(result.isLast())
        );
    }
}
