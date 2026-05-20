package com.carmenio.consensus.application.use_case.enrollment;

import com.carmenio.consensus.domain.exception.EnrollmentException;
import com.carmenio.consensus.domain.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for deleting an enrollment.
 * <p>
 * Enrollments have no child entities, so no dependency checks are needed.
 */
@Component
@RequiredArgsConstructor
@Transactional
public class DeleteEnrollmentUseCase {

    private final EnrollmentRepository enrollmentRepository;

    /**
     * Deletes an enrollment by ID.
     *
     * @param id the enrollment UUID
     * @throws EnrollmentException if no enrollment exists with the given ID
     */
    public void execute(UUID id) {
        var enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> EnrollmentException.notFound(id));
        enrollmentRepository.delete(enrollment);
    }
}
