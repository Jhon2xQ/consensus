package com.carmenio.consensus.application.use_case.enrollment;

import com.carmenio.consensus.domain.entity.Enrollment;
import com.carmenio.consensus.domain.exception.EnrollmentException;
import com.carmenio.consensus.domain.repository.EnrollmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteEnrollmentUseCaseTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private DeleteEnrollmentUseCase useCase;

    @Test
    @DisplayName("Should delete enrollment when it exists")
    void shouldDeleteEnrollmentWhenFound() {
        var id = UUID.randomUUID();
        var entity = Enrollment.builder()
                .id(id)
                .electoralProcessId(UUID.randomUUID())
                .build();

        when(enrollmentRepository.findById(id)).thenReturn(Optional.of(entity));

        assertDoesNotThrow(() -> useCase.execute(id));

        verify(enrollmentRepository).findById(id);
        verify(enrollmentRepository).delete(entity);
    }

    @Test
    @DisplayName("Should throw 404 when enrollment not found")
    void shouldThrow404WhenNotFound() {
        var id = UUID.randomUUID();

        when(enrollmentRepository.findById(id)).thenReturn(Optional.empty());

        var exception = assertThrows(EnrollmentException.class,
                () -> useCase.execute(id));

        assertTrue(exception.getMessage().contains("not found"));
    }
}
