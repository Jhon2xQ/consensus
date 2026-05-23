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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateEnrollmentUseCaseTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private ElectoralProcessRepository electoralProcessRepository;

    @Mock
    private EnrollmentMapper mapper;

    @InjectMocks
    private CreateEnrollmentUseCase useCase;

    @Captor
    private ArgumentCaptor<Enrollment> enrollmentCaptor;

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
    @DisplayName("Should create enrollment with email only in creator phase when process is in COMMITMENT")
    void shouldCreateEnrollmentWithEmailWhenProcessInCommitment() {
        var now = Instant.now();
        var process = createProcess(now, ProcessStatus.COMMITMENT);
        var processId = process.getId();
        var request = CreateEnrollmentRequest.builder()
                .email("voter@example.com")
                .build();

        var entity = new Enrollment();
        var savedEntity = new Enrollment();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setHasVoted(false);

        var expectedResponse = EnrollmentResponse.builder()
                .id(savedEntity.getId())
                .electoralProcessId(processId)
                .email("voter@example.com")
                .hasVoted(false)
                .build();

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.of(process));
        when(enrollmentRepository.existsByElectoralProcessIdAndEmail(processId, "voter@example.com"))
                .thenReturn(false);
        when(mapper.toEntity(request, processId)).thenReturn(entity);
        when(enrollmentRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toResponse(savedEntity)).thenReturn(expectedResponse);

        var result = useCase.execute(processId, request);

        assertNotNull(result);
        assertEquals("voter@example.com", result.getEmail());
        assertEquals(processId, result.getElectoralProcessId());
        assertNull(result.getUserId());
        assertNull(result.getCommitment());
        assertFalse(result.isHasVoted());

        verify(electoralProcessRepository).findById(processId);
        verify(enrollmentRepository).existsByElectoralProcessIdAndEmail(processId, "voter@example.com");
        verify(enrollmentRepository, never()).existsByElectoralProcessIdAndCommitment(any(), any());
        verify(mapper).toEntity(request, processId);
        verify(enrollmentRepository).save(entity);
        verify(mapper).toResponse(savedEntity);
    }

    @Test
    @DisplayName("Should create enrollment with email when process is in NONE state")
    void shouldCreateEnrollmentWhenProcessInNone() {
        var now = Instant.now();
        var process = createProcess(now, ProcessStatus.NONE);
        var processId = process.getId();
        var request = CreateEnrollmentRequest.builder()
                .email("another@example.com")
                .build();

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.of(process));
        when(enrollmentRepository.existsByElectoralProcessIdAndEmail(processId, "another@example.com"))
                .thenReturn(false);
        when(mapper.toEntity(request, processId)).thenReturn(new Enrollment());
        when(enrollmentRepository.save(any())).thenReturn(new Enrollment());
        when(mapper.toResponse(any())).thenReturn(
                EnrollmentResponse.builder().email("another@example.com").build());

        var result = useCase.execute(processId, request);
        assertEquals("another@example.com", result.getEmail());
    }

    @Test
    @DisplayName("Should throw 409 when duplicate email exists in same process")
    void shouldThrow409WhenDuplicateEmail() {
        var now = Instant.now();
        var process = createProcess(now, ProcessStatus.COMMITMENT);
        var processId = process.getId();
        var request = CreateEnrollmentRequest.builder()
                .email("duplicate@example.com")
                .build();

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.of(process));
        when(enrollmentRepository.existsByElectoralProcessIdAndEmail(processId, "duplicate@example.com"))
                .thenReturn(true);

        var exception = assertThrows(EnrollmentException.class,
                () -> useCase.execute(processId, request));

        assertTrue(exception.getMessage().contains("already registered"));
        assertEquals(409, exception.getStatusCode());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw 404 when process does not exist")
    void shouldThrow404WhenProcessNotFound() {
        var processId = UUID.randomUUID();
        var request = CreateEnrollmentRequest.builder()
                .email("voter@example.com")
                .build();

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.empty());

        var exception = assertThrows(ElectoralProcessException.class,
                () -> useCase.execute(processId, request));

        assertTrue(exception.getMessage().contains("not found"));
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw 400 when process is in VOTING state")
    void shouldThrow400WhenProcessInVoting() {
        var now = Instant.now();
        var process = createProcess(now, ProcessStatus.VOTING);
        var processId = process.getId();
        var request = CreateEnrollmentRequest.builder()
                .email("voter@example.com")
                .build();

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.of(process));

        var exception = assertThrows(EnrollmentException.class,
                () -> useCase.execute(processId, request));

        assertTrue(exception.getMessage().contains("Enrollment not open"));
        assertEquals(400, exception.getStatusCode());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw 400 when process is in CLOSED state")
    void shouldThrow400WhenProcessInClosed() {
        var now = Instant.now();
        var process = createProcess(now, ProcessStatus.CLOSED);
        var processId = process.getId();
        var request = CreateEnrollmentRequest.builder()
                .email("voter@example.com")
                .build();

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.of(process));

        var exception = assertThrows(EnrollmentException.class,
                () -> useCase.execute(processId, request));

        assertTrue(exception.getMessage().contains("Enrollment not open"));
        assertEquals(400, exception.getStatusCode());
    }

    @Test
    @DisplayName("Should NOT check commitment uniqueness when commitment is null")
    void shouldNotCheckCommitmentUniquenessWhenCommitmentIsNull() {
        var now = Instant.now();
        var process = createProcess(now, ProcessStatus.COMMITMENT);
        var processId = process.getId();
        var request = CreateEnrollmentRequest.builder()
                .email("voter@example.com")
                .build();

        var entity = new Enrollment();
        var savedEntity = new Enrollment();
        savedEntity.setId(UUID.randomUUID());

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.of(process));
        when(enrollmentRepository.existsByElectoralProcessIdAndEmail(processId, "voter@example.com"))
                .thenReturn(false);
        when(mapper.toEntity(request, processId)).thenReturn(entity);
        when(enrollmentRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toResponse(savedEntity)).thenReturn(
                EnrollmentResponse.builder().email("voter@example.com").build());

        var result = useCase.execute(processId, request);

        assertEquals("voter@example.com", result.getEmail());
        verify(enrollmentRepository, never()).existsByElectoralProcessIdAndCommitment(any(), any());
    }
}
