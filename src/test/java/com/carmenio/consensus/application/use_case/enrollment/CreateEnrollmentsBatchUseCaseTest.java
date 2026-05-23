package com.carmenio.consensus.application.use_case.enrollment;

import com.carmenio.consensus.application.dto.enrollment.CreateEnrollmentRequest;
import com.carmenio.consensus.application.dto.enrollment.EnrollmentResponse;
import com.carmenio.consensus.common.constant.ProcessStatus;
import com.carmenio.consensus.domain.entity.ElectoralProcess;
import com.carmenio.consensus.domain.entity.Enrollment;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.domain.exception.EnrollmentException;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.domain.repository.EnrollmentRepository;
import com.carmenio.consensus.infrastructure.mapper.EnrollmentMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateEnrollmentsBatchUseCaseTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private ElectoralProcessRepository electoralProcessRepository;

    @Mock
    private EnrollmentMapper mapper;

    @InjectMocks
    private CreateEnrollmentsBatchUseCase useCase;

    private ElectoralProcess createProcess(Instant now, ProcessStatus desiredState) {
        return switch (desiredState) {
            case COMMITMENT -> ElectoralProcess.builder()
                    .id(UUID.randomUUID())
                    .commitmentStart(now.minusSeconds(3600))
                    .commitmentEnd(now.plusSeconds(3600))
                    .votingStart(now.plusSeconds(7200))
                    .votingEnd(now.plusSeconds(14400))
                    .results(now.plusSeconds(21600))
                    .build();
            case NONE -> ElectoralProcess.builder()
                    .id(UUID.randomUUID())
                    .commitmentStart(now.plusSeconds(3600))
                    .commitmentEnd(now.plusSeconds(7200))
                    .votingStart(now.plusSeconds(10800))
                    .votingEnd(now.plusSeconds(14400))
                    .results(now.plusSeconds(21600))
                    .build();
            case VOTING -> ElectoralProcess.builder()
                    .id(UUID.randomUUID())
                    .commitmentStart(now.minusSeconds(14400))
                    .commitmentEnd(now.minusSeconds(7200))
                    .votingStart(now.minusSeconds(3600))
                    .votingEnd(now.plusSeconds(3600))
                    .results(now.plusSeconds(7200))
                    .build();
            case CLOSED -> ElectoralProcess.builder()
                    .id(UUID.randomUUID())
                    .commitmentStart(now.minusSeconds(14400))
                    .commitmentEnd(now.minusSeconds(10800))
                    .votingStart(now.minusSeconds(7200))
                    .votingEnd(now.minusSeconds(3600))
                    .results(now.minusSeconds(1))
                    .build();
        };
    }

    @Test
    @DisplayName("Should create single enrollment when process is in COMMITMENT")
    void shouldCreateSingleEnrollmentInCommitment() {
        var now = Instant.now();
        var process = createProcess(now, ProcessStatus.COMMITMENT);
        var processId = process.getId();
        var request = CreateEnrollmentRequest.builder().email("voter@example.com").build();
        var requests = List.of(request);

        var entity = new Enrollment();
        var savedEntity = new Enrollment();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setHasVoted(false);

        var response = EnrollmentResponse.builder()
                .id(savedEntity.getId())
                .electoralProcessId(processId)
                .email("voter@example.com")
                .hasVoted(false)
                .build();

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.of(process));
        when(enrollmentRepository.findEmailsByProcessIdAndEmailsIn(eq(processId), anyList()))
                .thenReturn(Collections.emptyList());
        when(mapper.toEntity(request, processId)).thenReturn(entity);
        when(enrollmentRepository.saveAll(List.of(entity))).thenReturn(List.of(savedEntity));
        when(mapper.toResponse(savedEntity)).thenReturn(response);

        var result = useCase.execute(processId, requests);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("voter@example.com", result.get(0).getEmail());
        assertEquals(processId, result.get(0).getElectoralProcessId());

        verify(electoralProcessRepository).findById(processId);
        verify(enrollmentRepository).findEmailsByProcessIdAndEmailsIn(eq(processId), anyList());
        verify(enrollmentRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should create enrollment when process is in NONE state")
    void shouldCreateEnrollmentInNoneState() {
        var now = Instant.now();
        var process = createProcess(now, ProcessStatus.NONE);
        var processId = process.getId();
        var request = CreateEnrollmentRequest.builder().email("another@example.com").build();
        var requests = List.of(request);

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.of(process));
        when(enrollmentRepository.findEmailsByProcessIdAndEmailsIn(eq(processId), anyList()))
                .thenReturn(Collections.emptyList());
        when(mapper.toEntity(request, processId)).thenReturn(new Enrollment());
        when(enrollmentRepository.saveAll(anyList())).thenReturn(List.of(new Enrollment()));
        when(mapper.toResponse(any())).thenReturn(
                EnrollmentResponse.builder().email("another@example.com").build());

        var result = useCase.execute(processId, requests);

        assertEquals(1, result.size());
        assertEquals("another@example.com", result.get(0).getEmail());
    }

    @Test
    @DisplayName("Should reject empty batch with 400")
    void shouldRejectEmptyBatch() {
        var processId = UUID.randomUUID();

        var exception = assertThrows(EnrollmentException.class,
                () -> useCase.execute(processId, Collections.emptyList()));

        assertEquals(400, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("At least one enrollment"));
        verify(enrollmentRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should reject null list with 400")
    void shouldRejectNullList() {
        var processId = UUID.randomUUID();

        var exception = assertThrows(EnrollmentException.class,
                () -> useCase.execute(processId, null));

        assertEquals(400, exception.getStatusCode());
        verify(enrollmentRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should reject when process not found")
    void shouldRejectWhenProcessNotFound() {
        var processId = UUID.randomUUID();
        var request = CreateEnrollmentRequest.builder().email("a@t.com").build();
        var requests = List.of(request);

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.empty());

        var exception = assertThrows(ElectoralProcessException.class,
                () -> useCase.execute(processId, requests));

        assertTrue(exception.getMessage().contains("not found"));
        verify(enrollmentRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should reject when process is in VOTING state")
    void shouldRejectInVotingState() {
        var now = Instant.now();
        var process = createProcess(now, ProcessStatus.VOTING);
        var processId = process.getId();
        var request = CreateEnrollmentRequest.builder().email("a@t.com").build();
        var requests = List.of(request);

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.of(process));

        var exception = assertThrows(EnrollmentException.class,
                () -> useCase.execute(processId, requests));

        assertTrue(exception.getMessage().contains("Enrollment not open"));
        assertEquals(400, exception.getStatusCode());
        verify(enrollmentRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should reject when process is in CLOSED state")
    void shouldRejectInClosedState() {
        var now = Instant.now();
        var process = createProcess(now, ProcessStatus.CLOSED);
        var processId = process.getId();
        var request = CreateEnrollmentRequest.builder().email("a@t.com").build();
        var requests = List.of(request);

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.of(process));

        var exception = assertThrows(EnrollmentException.class,
                () -> useCase.execute(processId, requests));

        assertTrue(exception.getMessage().contains("Enrollment not open"));
        assertEquals(400, exception.getStatusCode());
    }

    @Test
    @DisplayName("Should detect within-batch duplicate email")
    void shouldRejectWithinBatchDuplicateEmail() {
        var now = Instant.now();
        var process = createProcess(now, ProcessStatus.COMMITMENT);
        var processId = process.getId();
        var req1 = CreateEnrollmentRequest.builder().email("dup@test.com").build();
        var req2 = CreateEnrollmentRequest.builder().email("dup@test.com").build();
        var requests = List.of(req1, req2);

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.of(process));

        var exception = assertThrows(EnrollmentException.class,
                () -> useCase.execute(processId, requests));

        assertEquals(409, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("dup@test.com"));
        verify(enrollmentRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should reject when DB email conflict exists")
    void shouldRejectWhenDbEmailConflict() {
        var now = Instant.now();
        var process = createProcess(now, ProcessStatus.COMMITMENT);
        var processId = process.getId();
        var req1 = CreateEnrollmentRequest.builder().email("existing@test.com").build();
        var req2 = CreateEnrollmentRequest.builder().email("new@test.com").build();
        var requests = List.of(req1, req2);

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.of(process));
        when(enrollmentRepository.findEmailsByProcessIdAndEmailsIn(eq(processId), anyList()))
                .thenReturn(List.of("existing@test.com"));

        var exception = assertThrows(EnrollmentException.class,
                () -> useCase.execute(processId, requests));

        assertEquals(409, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("existing@test.com"));
    }
}
