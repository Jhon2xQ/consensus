package com.carmenio.consensus.application.use_case.record;

import com.carmenio.consensus.application.dto.record.CreateVoteRecordRequest;
import com.carmenio.consensus.application.dto.record.VoteRecordResponse;
import com.carmenio.consensus.domain.entity.ElectoralProcess;
import com.carmenio.consensus.domain.entity.Team;
import com.carmenio.consensus.domain.entity.VoteRecord;
import com.carmenio.consensus.domain.exception.RecordException;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.domain.repository.TeamRepository;
import com.carmenio.consensus.domain.repository.VoteRecordRepository;
import com.carmenio.consensus.infrastructure.mapper.VoteRecordMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateVoteRecordUseCaseTest {

    @Mock
    private ElectoralProcessRepository electoralProcessRepository;

    @Mock
    private VoteRecordRepository voteRecordRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private VoteRecordMapper mapper;

    @InjectMocks
    private CreateVoteRecordUseCase useCase;

    @Test
    @DisplayName("Should create record when scope and message are valid")
    void shouldCreateRecordWhenScopeAndMessageAreValid() {
        var scope = "scope-123";
        var processId = UUID.randomUUID();
        var process = ElectoralProcess.builder()
                .id(processId)
                .scope(scope)
                .name("Test Process")
                .build();
        var team = Team.builder()
                .id(UUID.randomUUID())
                .electoralProcessId(processId)
                .name("Team Alpha")
                .build();
        var request = CreateVoteRecordRequest.builder()
                .groupId("1")
                .nullifier("nullifier-1")
                .message("Team Alpha")
                .scope(scope)
                .transactionHash("0xabc")
                .build();

        var entity = new VoteRecord();
        var savedEntity = VoteRecord.builder()
                .id(UUID.randomUUID())
                .groupId("1")
                .nullifier("nullifier-1")
                .message("Team Alpha")
                .scope(scope)
                .transactionHash("0xabc")
                .createdAt(Instant.now())
                .build();

        var expectedResponse = VoteRecordResponse.builder()
                .id(savedEntity.getId())
                .groupId("1")
                .nullifier("nullifier-1")
                .message("Team Alpha")
                .scope(scope)
                .transactionHash("0xabc")
                .createdAt(savedEntity.getCreatedAt())
                .build();

        when(electoralProcessRepository.findByScope(scope))
                .thenReturn(Optional.of(process));
        when(voteRecordRepository.findByNullifier("nullifier-1"))
                .thenReturn(Optional.empty());
        when(teamRepository.findByElectoralProcessId(processId))
                .thenReturn(List.of(team));
        when(mapper.toEntity(request)).thenReturn(entity);
        when(voteRecordRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toResponse(savedEntity)).thenReturn(expectedResponse);

        var result = useCase.execute(request);

        assertNotNull(result);
        assertEquals("nullifier-1", result.getNullifier());
        assertEquals("Team Alpha", result.getMessage());
        assertEquals(scope, result.getScope());
        assertEquals("0xabc", result.getTransactionHash());
        verify(voteRecordRepository).findByNullifier("nullifier-1");
        verify(voteRecordRepository).save(entity);
        verify(mapper).toEntity(request);
        verify(mapper).toResponse(savedEntity);
    }

    @Test
    @DisplayName("Should return existing record when nullifier already exists (idempotent)")
    void shouldReturnExistingRecordWhenNullifierAlreadyExists() {
        var scope = "scope-123";
        var processId = UUID.randomUUID();
        var process = ElectoralProcess.builder()
                .id(processId)
                .scope(scope)
                .build();
        var request = CreateVoteRecordRequest.builder()
                .groupId("1")
                .nullifier("existing-nullifier")
                .message("Team Alpha")
                .scope(scope)
                .build();

        var existingRecord = VoteRecord.builder()
                .id(UUID.randomUUID())
                .nullifier("existing-nullifier")
                .message("Team Alpha")
                .scope(scope)
                .build();

        var existingResponse = VoteRecordResponse.builder()
                .id(existingRecord.getId())
                .nullifier("existing-nullifier")
                .message("Team Alpha")
                .scope(scope)
                .build();

        when(electoralProcessRepository.findByScope(scope))
                .thenReturn(Optional.of(process));
        when(voteRecordRepository.findByNullifier("existing-nullifier"))
                .thenReturn(Optional.of(existingRecord));
        when(mapper.toResponse(existingRecord)).thenReturn(existingResponse);

        var result = useCase.execute(request);

        assertNotNull(result);
        assertEquals("existing-nullifier", result.getNullifier());
        verify(voteRecordRepository).findByNullifier("existing-nullifier");
        verify(voteRecordRepository, never()).save(any());
        verify(mapper, never()).toEntity(any());
    }

    @Test
    @DisplayName("Should throw RecordException when scope does not match any process")
    void shouldThrowRecordExceptionWhenScopeNotFound() {
        var request = CreateVoteRecordRequest.builder()
                .groupId("1")
                .nullifier("nullifier-1")
                .message("Team Alpha")
                .scope("nonexistent-scope")
                .build();

        when(electoralProcessRepository.findByScope("nonexistent-scope"))
                .thenReturn(Optional.empty());

        var exception = assertThrows(RecordException.class,
                () -> useCase.execute(request));

        assertTrue(exception.getMessage().contains("scope"));
        assertEquals(400, exception.getStatusCode());
        verify(voteRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw RecordException when message does not match any team")
    void shouldThrowRecordExceptionWhenMessageDoesNotMatchTeam() {
        var scope = "scope-123";
        var processId = UUID.randomUUID();
        var process = ElectoralProcess.builder()
                .id(processId)
                .scope(scope)
                .build();
        var request = CreateVoteRecordRequest.builder()
                .groupId("1")
                .nullifier("nullifier-1")
                .message("Nonexistent Team")
                .scope(scope)
                .build();

        when(electoralProcessRepository.findByScope(scope))
                .thenReturn(Optional.of(process));
        when(voteRecordRepository.findByNullifier("nullifier-1"))
                .thenReturn(Optional.empty());
        when(teamRepository.findByElectoralProcessId(processId))
                .thenReturn(List.of()); // No teams

        var exception = assertThrows(RecordException.class,
                () -> useCase.execute(request));

        assertTrue(exception.getMessage().contains("message"));
        assertEquals(400, exception.getStatusCode());
        verify(voteRecordRepository, never()).save(any());
    }
}
