package com.carmenio.consensus.application.use_case.enrollment;

import com.carmenio.consensus.application.dto.enrollment.CreateEnrollmentRequest;
import com.carmenio.consensus.application.dto.enrollment.EnrollmentResponse;
import com.carmenio.consensus.application.util.ProcessStateCalculator;
import com.carmenio.consensus.common.constant.ProcessStatus;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.domain.exception.EnrollmentException;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.domain.repository.EnrollmentRepository;
import com.carmenio.consensus.infrastructure.mapper.EnrollmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Use case for creating a new enrollment within an electoral process.
 * <p>
 * Validates that the process exists, is in the correct state
 * (NONE or COMMITMENT), and that there are no duplicate user IDs
 * or commitments within the same process.
 */
@Component
@RequiredArgsConstructor
@Transactional
public class CreateEnrollmentUseCase {

    private final EnrollmentRepository enrollmentRepository;
    private final ElectoralProcessRepository electoralProcessRepository;
    private final EnrollmentMapper mapper;

    /**
     * Creates a new enrollment.
     *
     * @param request the creation payload with electoralProcessId, userId, and commitment
     * @return the created enrollment as a response DTO
     * @throws ElectoralProcessException if the process does not exist
     * @throws EnrollmentException       if the process is not in NONE/COMMITMENT state,
     *                                   or if a duplicate userId/commitment exists
     */
    public EnrollmentResponse execute(CreateEnrollmentRequest request) {
        var processId = request.getElectoralProcessId();

        var process = electoralProcessRepository.findById(processId)
                .orElseThrow(() -> ElectoralProcessException.notFound(processId));

        // Keep entity fresh — transitionState auto-computes and persists via dirty checking
        var now = Instant.now();
        ProcessStateCalculator.transitionState(process, now);

        var state = ProcessStateCalculator.computeState(process, now);
        if (state != ProcessStatus.NONE && state != ProcessStatus.COMMITMENT) {
            throw EnrollmentException.invalidState("Enrollment not open for this process");
        }

        if (enrollmentRepository.existsByElectoralProcessIdAndUserId(processId, request.getUserId())) {
            throw EnrollmentException.alreadyExists("userId");
        }

        if (enrollmentRepository.existsByElectoralProcessIdAndCommitment(processId, request.getCommitment())) {
            throw EnrollmentException.duplicateCommitment();
        }

        var entity = mapper.toEntity(request);
        var saved = enrollmentRepository.save(entity);
        return mapper.toResponse(saved);
    }
}
