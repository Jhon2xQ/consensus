package com.carmenio.consensus.application.use_case.enrollment;

import com.carmenio.consensus.domain.entity.ElectoralProcess;
import com.carmenio.consensus.domain.entity.Enrollment;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.domain.repository.EnrollmentRepository;
import com.carmenio.consensus.infrastructure.mapper.EnrollmentMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListEnrollmentsByProcessUseCaseTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private ElectoralProcessRepository electoralProcessRepository;

    @Mock
    private EnrollmentMapper mapper;

    @InjectMocks
    private ListEnrollmentsByProcessUseCase useCase;

    @Test
    @DisplayName("Should return list of enrollments for existing process")
    void shouldReturnEnrollmentsForExistingProcess() {
        var processId = UUID.randomUUID();
        var enrollment1 = new Enrollment();
        enrollment1.setUserId("user-1");
        var enrollment2 = new Enrollment();
        enrollment2.setUserId("user-2");

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.of(new ElectoralProcess()));
        when(enrollmentRepository.findByElectoralProcessId(processId))
                .thenReturn(List.of(enrollment1, enrollment2));
        when(mapper.toResponse(enrollment1)).thenReturn(null);
        when(mapper.toResponse(enrollment2)).thenReturn(null);

        var result = useCase.execute(processId);

        assertEquals(2, result.size());
        verify(electoralProcessRepository).findById(processId);
        verify(enrollmentRepository).findByElectoralProcessId(processId);
        verify(mapper).toResponse(enrollment1);
        verify(mapper).toResponse(enrollment2);
    }

    @Test
    @DisplayName("Should throw 404 when process does not exist")
    void shouldThrow404WhenProcessNotFound() {
        var processId = UUID.randomUUID();

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.empty());

        assertThrows(ElectoralProcessException.class,
                () -> useCase.execute(processId));

        verify(enrollmentRepository, never()).findByElectoralProcessId(any());
    }

    @Test
    @DisplayName("Should return empty list when process has no enrollments")
    void shouldReturnEmptyListWhenNoEnrollments() {
        var processId = UUID.randomUUID();

        when(electoralProcessRepository.findById(processId))
                .thenReturn(Optional.of(new ElectoralProcess()));
        when(enrollmentRepository.findByElectoralProcessId(processId))
                .thenReturn(List.of());

        var result = useCase.execute(processId);

        assertTrue(result.isEmpty());
    }
}
