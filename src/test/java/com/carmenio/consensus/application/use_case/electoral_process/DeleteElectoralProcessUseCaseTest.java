package com.carmenio.consensus.application.use_case.electoral_process;

import com.carmenio.consensus.domain.entity.ElectoralProcess;
import com.carmenio.consensus.domain.entity.Enrollment;
import com.carmenio.consensus.domain.entity.Team;
import com.carmenio.consensus.domain.entity.VoteRecord;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.domain.repository.EnrollmentRepository;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteElectoralProcessUseCaseTest {

    @Mock
    private ElectoralProcessRepository electoralProcessRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private VoteRecordRepository voteRecordRepository;

    @InjectMocks
    private DeleteElectoralProcessUseCase useCase;

    @Test
    @DisplayName("should delete process and cascade all related records")
    void shouldDeleteProcessAndCascadeAllRelatedRecords() {
        var id = UUID.randomUUID();
        var scope = "test-scope";
        var now = Instant.now();
        var entity = ElectoralProcess.builder()
                .id(id)
                .name("Test Process")
                .scope(scope)
                .commitmentStart(now)
                .commitmentEnd(now.plusSeconds(3600))
                .votingStart(now.plusSeconds(7200))
                .votingEnd(now.plusSeconds(10800))
                .results(now.plusSeconds(14400))
                .build();

        var team1 = Team.builder().id(UUID.randomUUID()).electoralProcessId(id).name("Team A").build();
        var team2 = Team.builder().id(UUID.randomUUID()).electoralProcessId(id).name("Team B").build();
        var enrollment = Enrollment.builder().id(UUID.randomUUID()).electoralProcessId(id).email("voter@test.com").build();
        var voteRecord = VoteRecord.builder().id(UUID.randomUUID()).scope(scope).nullifier("nullifier-1").build();

        when(electoralProcessRepository.findById(id)).thenReturn(Optional.of(entity));
        when(teamRepository.findByElectoralProcessId(id)).thenReturn(List.of(team1, team2));
        when(enrollmentRepository.findByElectoralProcessId(id)).thenReturn(List.of(enrollment));
        when(voteRecordRepository.findByScope(scope)).thenReturn(List.of(voteRecord));

        useCase.execute(id);

        verify(teamRepository).delete(team1);
        verify(teamRepository).delete(team2);
        verify(enrollmentRepository).delete(enrollment);
        verify(voteRecordRepository).delete(voteRecord);
        verify(electoralProcessRepository).delete(entity);
    }

    @Test
    @DisplayName("should delete process when no related records exist")
    void shouldDeleteProcessWhenNoRelatedRecordsExist() {
        var id = UUID.randomUUID();
        var scope = "test-scope";
        var now = Instant.now();
        var entity = ElectoralProcess.builder()
                .id(id)
                .name("Test Process")
                .scope(scope)
                .commitmentStart(now)
                .commitmentEnd(now.plusSeconds(3600))
                .votingStart(now.plusSeconds(7200))
                .votingEnd(now.plusSeconds(10800))
                .results(now.plusSeconds(14400))
                .build();

        when(electoralProcessRepository.findById(id)).thenReturn(Optional.of(entity));
        when(teamRepository.findByElectoralProcessId(id)).thenReturn(List.of());
        when(enrollmentRepository.findByElectoralProcessId(id)).thenReturn(List.of());
        when(voteRecordRepository.findByScope(scope)).thenReturn(List.of());

        useCase.execute(id);

        verify(teamRepository, never()).delete(any());
        verify(enrollmentRepository, never()).delete(any());
        verify(voteRecordRepository, never()).delete(any());
        verify(electoralProcessRepository).delete(entity);
    }

    @Test
    @DisplayName("should throw when process not found")
    void shouldThrowWhenProcessNotFound() {
        var id = UUID.randomUUID();
        when(electoralProcessRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ElectoralProcessException.class, () -> useCase.execute(id));

        verify(electoralProcessRepository, never()).delete(any());
        verify(teamRepository, never()).delete(any());
        verify(enrollmentRepository, never()).delete(any());
        verify(voteRecordRepository, never()).delete(any());
    }
}
