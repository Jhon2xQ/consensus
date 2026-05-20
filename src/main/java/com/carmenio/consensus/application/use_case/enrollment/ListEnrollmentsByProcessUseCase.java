package com.carmenio.consensus.application.use_case.enrollment;

import com.carmenio.consensus.application.dto.enrollment.EnrollmentResponse;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.domain.repository.EnrollmentRepository;
import com.carmenio.consensus.infrastructure.mapper.EnrollmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Use case for listing all enrollments of an electoral process.
 * <p>
 * Validates that the process exists before returning enrollments.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListEnrollmentsByProcessUseCase {

    private final EnrollmentRepository enrollmentRepository;
    private final ElectoralProcessRepository electoralProcessRepository;
    private final EnrollmentMapper mapper;

    /**
     * Lists all enrollments for the given electoral process.
     *
     * @param processId the electoral process UUID
     * @return a list of enrollment response DTOs (possibly empty)
     * @throws ElectoralProcessException if the process does not exist
     */
    public List<EnrollmentResponse> execute(UUID processId) {
        electoralProcessRepository.findById(processId)
                .orElseThrow(() -> ElectoralProcessException.notFound(processId));

        return enrollmentRepository.findByElectoralProcessId(processId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
