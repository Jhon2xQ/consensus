package com.carmenio.consensus.application.use_case.record;

import com.carmenio.consensus.domain.entity.ElectoralProcess;
import com.carmenio.consensus.domain.entity.Team;
import com.carmenio.consensus.domain.entity.VoteRecord;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.domain.repository.TeamRepository;
import com.carmenio.consensus.domain.repository.VoteRecordRepository;
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
class GetProcessResultsUseCaseTest {

    @Mock
    private ElectoralProcessRepository electoralProcessRepository;

    @Mock
    private VoteRecordRepository voteRecordRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private GetProcessResultsUseCase useCase;

    private ElectoralProcess createClosedProcess() {
        var now = Instant.now();
        return ElectoralProcess.builder()
                .id(UUID.randomUUID())
                .name("Test Process")
                .scope("scope-123")
                .commitmentStart(now.minusSeconds(14400))
                .commitmentEnd(now.minusSeconds(10800))
                .votingStart(now.minusSeconds(7200))
                .votingEnd(now.minusSeconds(3600))
                .results(now.minusSeconds(1))
                .build();
    }

    @Test
    @DisplayName("Should return results when process is CLOSED and records exist")
    void shouldReturnResultsWhenProcessIsClosed() {
        var process = createClosedProcess();
        var processId = process.getId();
        var teamA = Team.builder().id(UUID.randomUUID()).electoralProcessId(processId).name("Team A").build();
        var teamB = Team.builder().id(UUID.randomUUID()).electoralProcessId(processId).name("Team B").build();

        var recordA1 = VoteRecord.builder().id(UUID.randomUUID()).scope("scope-123").message("Team A").build();
        var recordA2 = VoteRecord.builder().id(UUID.randomUUID()).scope("scope-123").message("Team A").build();
        var recordB = VoteRecord.builder().id(UUID.randomUUID()).scope("scope-123").message("Team B").build();

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.of(process));
        when(voteRecordRepository.findByScope("scope-123"))
                .thenReturn(List.of(recordA1, recordA2, recordB));
        when(teamRepository.findByElectoralProcessId(processId))
                .thenReturn(List.of(teamA, teamB));

        var result = useCase.execute(processId);

        assertNotNull(result);
        assertEquals(processId, result.getProcessId());
        assertEquals("Test Process", result.getProcessName());
        assertEquals(3, result.getTotalVotes());
        assertEquals("CLOSED", result.getStatus());

        // Team A (2 votes) should be first, Team B (1 vote) second
        assertEquals(2, result.getTeamResults().size());
        assertEquals("Team A", result.getTeamResults().get(0).getTeamName());
        assertEquals(2, result.getTeamResults().get(0).getVoteCount());
        assertEquals("Team B", result.getTeamResults().get(1).getTeamName());
        assertEquals(1, result.getTeamResults().get(1).getVoteCount());
    }

    @Test
    @DisplayName("Should throw 404 when process does not exist")
    void shouldThrow404WhenProcessNotFound() {
        var processId = UUID.randomUUID();

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.empty());

        var exception = assertThrows(ElectoralProcessException.class,
                () -> useCase.execute(processId));

        assertTrue(exception.getMessage().contains("not found"));
        assertEquals(404, exception.getStatusCode());
        verify(voteRecordRepository, never()).findByScope(any());
    }

    @Test
    @DisplayName("Should throw 400 when process is not CLOSED")
    void shouldThrow400WhenProcessNotClosed() {
        var now = Instant.now();
        var process = ElectoralProcess.builder()
                .id(UUID.randomUUID())
                .scope("scope-123")
                .commitmentStart(now.minusSeconds(3600))
                .commitmentEnd(now.plusSeconds(3600))
                .votingStart(now.plusSeconds(7200))
                .votingEnd(now.plusSeconds(14400))
                .results(now.plusSeconds(21600))
                .build();
        var processId = process.getId();

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.of(process));

        var exception = assertThrows(ElectoralProcessException.class,
                () -> useCase.execute(processId));

        assertTrue(exception.getMessage().contains("closed"));
        assertEquals(400, exception.getStatusCode());
        verify(voteRecordRepository, never()).findByScope(any());
    }

    @Test
    @DisplayName("Should return results with zero votes when no records exist")
    void shouldReturnResultsWithZeroVotesWhenNoRecords() {
        var process = createClosedProcess();
        var processId = process.getId();

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.of(process));
        when(voteRecordRepository.findByScope("scope-123"))
                .thenReturn(List.of());
        when(teamRepository.findByElectoralProcessId(processId))
                .thenReturn(List.of());

        var result = useCase.execute(processId);

        assertNotNull(result);
        assertEquals(0, result.getTotalVotes());
        assertTrue(result.getTeamResults().isEmpty());
        assertEquals("CLOSED", result.getStatus());
    }
}
