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
import java.util.UUID;

/**
 * Use case for creating a new enrollment within an electoral process.
 * <p>
 * <b>Two-phase enrollment flow — Creator Phase</b>:
 * <ol>
 *   <li>Validates the electoral process exists and is in NONE or COMMITMENT state</li>
 *   <li>Enforces email uniqueness per process</li>
 *   <li>Creates the enrollment with email populated; userId and commitment are null</li>
 * </ol>
 * The user later claims the enrollment by matching their JWT email and providing
 * a commitment (see {@code ClaimEnrollmentUseCase}).
 */
@Component
@RequiredArgsConstructor
@Transactional
public class CreateEnrollmentUseCase {

    private final EnrollmentRepository enrollmentRepository;
    private final ElectoralProcessRepository electoralProcessRepository;
    private final EnrollmentMapper mapper;

    /**
     * Creates a new enrollment in the creator phase.
     * <p>
     * Only {@code email} is required. {@code userId} and {@code commitment}
     * are always null at this stage — they are set later when the user claims
     * the enrollment.
     *
     * @param processId the electoral process to create the enrollment in
     * @param request   the creation payload with email
     * @return the created enrollment as a response DTO
     * @throws ElectoralProcessException if the process does not exist (404)
     * @throws EnrollmentException       if the process is not in NONE/COMMITMENT state (400),
     *                                   or if the email is already registered in this process (409)
     */
    public EnrollmentResponse execute(UUID processId, CreateEnrollmentRequest request) {

        var process = electoralProcessRepository.findById(processId)
                .orElseThrow(() -> ElectoralProcessException.notFound(processId));

        // Keep entity fresh — transitionState auto-computes and persists via dirty checking
        var now = Instant.now();
        ProcessStateCalculator.transitionState(process, now);

        var state = ProcessStateCalculator.computeState(process, now);
        if (state != ProcessStatus.NONE && state != ProcessStatus.COMMITMENT) {
            throw EnrollmentException.invalidState("Enrollment not open for this process");
        }

        // Enforce email uniqueness per process
        if (enrollmentRepository.existsByElectoralProcessIdAndEmail(processId, request.getEmail())) {
            throw EnrollmentException.emailAlreadyRegistered(processId, request.getEmail());
        }

        var entity = mapper.toEntity(request, processId);
        var saved = enrollmentRepository.save(entity);
        return mapper.toResponse(saved);
    }
}
