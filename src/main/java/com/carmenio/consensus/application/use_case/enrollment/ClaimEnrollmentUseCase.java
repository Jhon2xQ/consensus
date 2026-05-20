package com.carmenio.consensus.application.use_case.enrollment;

import com.carmenio.consensus.application.dto.enrollment.ClaimEnrollmentRequest;
import com.carmenio.consensus.application.dto.enrollment.EnrollmentResponse;
import com.carmenio.consensus.application.util.JwtClaimExtractor;
import com.carmenio.consensus.application.util.ProcessStateCalculator;
import com.carmenio.consensus.common.constant.ProcessStatus;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.domain.exception.EnrollmentException;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.domain.repository.EnrollmentRepository;
import com.carmenio.consensus.infrastructure.mapper.EnrollmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Use case for claiming an existing enrollment with a Semaphore commitment.
 * <p>
 * <b>Two-phase enrollment flow — User Phase</b>:
 * <ol>
 *   <li>Extracts {@code email} and {@code sub} (userId) from the authenticated JWT</li>
 *   <li>Finds the enrollment by its path ID</li>
 *   <li>Verifies the JWT email matches the enrollment's pre-registered email</li>
 *   <li>Verifies the body's processId matches the enrollment's processId</li>
 *   <li>Ensures the enrollment has not already been claimed</li>
 *   <li>Validates the electoral process is in NONE or COMMITMENT state</li>
 *   <li>Enforces commitment uniqueness per process</li>
 *   <li>Sets {@code userId} (from JWT sub) and {@code commitment} on the enrollment</li>
 *   <li>Saves the enrollment and returns the response DTO</li>
 * </ol>
 * The creator registers voter emails via {@code CreateEnrollmentUseCase},
 * leaving userId and commitment null. This use case completes the enrollment
 * when the actual user authenticates and provides their commitment.
 */
@Component
@RequiredArgsConstructor
@Transactional
public class ClaimEnrollmentUseCase {

    private final EnrollmentRepository enrollmentRepository;
    private final ElectoralProcessRepository electoralProcessRepository;
    private final JwtClaimExtractor jwtClaimExtractor;
    private final EnrollmentMapper mapper;

    /**
     * Claims an existing enrollment by matching JWT claims to the pre-registered
     * email and setting the Semaphore commitment.
     *
     * @param enrollmentId the ID of the enrollment to claim (from path)
     * @param request      the claim payload with processId and commitment
     * @param jwt          the authenticated JWT from the request
     * @return the updated enrollment as a response DTO
     * @throws org.springframework.security.authentication.BadCredentialsException if JWT claims are missing (401)
     * @throws EnrollmentException if enrollment not found (404),
     *                             email mismatch (404),
     *                             processId mismatch (404),
     *                             already claimed (409),
     *                             invalid process state (400),
     *                             or duplicate commitment (409)
     * @throws ElectoralProcessException if the process does not exist (404)
     */
    public EnrollmentResponse execute(UUID enrollmentId, ClaimEnrollmentRequest request, Jwt jwt) {
        var email = jwtClaimExtractor.extractEmail(jwt);
        var userId = jwtClaimExtractor.extractUserId(jwt);

        var enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> EnrollmentException.notFound(enrollmentId));

        if (!email.equals(enrollment.getEmail())) {
            throw EnrollmentException.emailMismatch();
        }

        if (!request.getElectoralProcessId().equals(enrollment.getElectoralProcessId())) {
            throw EnrollmentException.processIdMismatch();
        }

        if (enrollment.getUserId() != null) {
            throw EnrollmentException.alreadyExists("userId");
        }

        var processId = enrollment.getElectoralProcessId();
        var process = electoralProcessRepository.findById(processId)
                .orElseThrow(() -> ElectoralProcessException.notFound(processId));

        var now = Instant.now();
        ProcessStateCalculator.transitionState(process, now);

        var state = ProcessStateCalculator.computeState(process, now);
        if (state != ProcessStatus.NONE && state != ProcessStatus.COMMITMENT) {
            throw EnrollmentException.invalidState("Enrollment not open for this process");
        }

        if (enrollmentRepository.existsByElectoralProcessIdAndCommitment(processId, request.getCommitment())) {
            throw EnrollmentException.duplicateCommitment();
        }

        enrollment.setUserId(userId);
        enrollment.setCommitment(request.getCommitment());

        var saved = enrollmentRepository.save(enrollment);
        return mapper.toResponse(saved);
    }
}
