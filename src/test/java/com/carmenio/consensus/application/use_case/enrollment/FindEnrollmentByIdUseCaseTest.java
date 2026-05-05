package com.carmenio.consensus.application.use_case.enrollment;

import com.carmenio.consensus.application.dto.enrollment.EnrollmentResponse;
import com.carmenio.consensus.domain.entity.Enrollment;
import com.carmenio.consensus.domain.exception.EnrollmentException;
import com.carmenio.consensus.domain.repository.EnrollmentRepository;
import com.carmenio.consensus.infrastructure.mapper.EnrollmentMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindEnrollmentByIdUseCaseTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private EnrollmentMapper mapper;

    @InjectMocks
    private FindEnrollmentByIdUseCase useCase;

    @Test
    @DisplayName("Should return enrollment when it exists")
    void shouldReturnEnrollmentWhenFound() {
        var id = UUID.randomUUID();
        var processId = UUID.randomUUID();
        var entity = Enrollment.builder()
                .id(id)
                .electoralProcessId(processId)
                .userId("user-123")
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .hasVoted(false)
                .build();

        var expectedResponse = EnrollmentResponse.builder()
                .id(id)
                .electoralProcessId(processId)
                .userId("user-123")
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .hasVoted(false)
                .build();

        when(enrollmentRepository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(expectedResponse);

        var result = useCase.execute(id);

        assertNotNull(result);
        assertEquals("user-123", result.getUserId());
        assertEquals(id, result.getId());
        verify(enrollmentRepository).findById(id);
        verify(mapper).toResponse(entity);
    }

    @Test
    @DisplayName("Should throw 404 when enrollment not found")
    void shouldThrow404WhenNotFound() {
        var id = UUID.randomUUID();

        when(enrollmentRepository.findById(id)).thenReturn(Optional.empty());

        var exception = assertThrows(EnrollmentException.class,
                () -> useCase.execute(id));

        assertTrue(exception.getMessage().contains("not found"));
        assertTrue(exception.getMessage().contains(id.toString()));
    }
}
