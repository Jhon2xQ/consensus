package com.carmenio.consensus.application.use_case.enrollment;

import com.carmenio.consensus.application.dto.enrollment.EnrollmentResponse;
import com.carmenio.consensus.domain.exception.EnrollmentException;
import com.carmenio.consensus.domain.repository.EnrollmentRepository;
import com.carmenio.consensus.infrastructure.mapper.EnrollmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Use case for finding an enrollment by its unique identifier.
 */
@Component
@RequiredArgsConstructor
public class FindEnrollmentByIdUseCase {

    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentMapper mapper;

    /**
     * Finds an enrollment by ID.
     *
     * @param id the enrollment UUID
     * @return the enrollment response DTO
     * @throws EnrollmentException if no enrollment exists with the given ID
     */
    public EnrollmentResponse execute(UUID id) {
        var entity = enrollmentRepository.findById(id)
                .orElseThrow(() -> EnrollmentException.notFound(id));
        return mapper.toResponse(entity);
    }
}
