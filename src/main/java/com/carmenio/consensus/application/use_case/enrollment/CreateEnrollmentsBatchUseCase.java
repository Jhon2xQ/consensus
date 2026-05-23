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
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Batch use case for creating multiple enrollments within an electoral process
 * in a single all-or-nothing transaction.
 * <p>
 * <b>Algorithm</b>:
 * <ol>
 *   <li>Validate the request list is not empty</li>
 *   <li>Validate the electoral process exists</li>
 *   <li>Compute the process state and reject if not in NONE or COMMITMENT</li>
 *   <li>Within-batch dedup via {@link HashSet} on emails</li>
 *   <li>Cross-batch email conflict via single DB {@code WHERE IN} query</li>
 *   <li>Map requests to entities via {@link EnrollmentMapper}</li>
 *   <li>Batch persist via {@link EnrollmentRepository#saveAll(List)}</li>
 *   <li>Map saved entities back to response DTOs</li>
 * </ol>
 * All validation happens before any persistence — any failure rolls back
 * the entire transaction.
 * <p>
 * <b>Creator phase only</b>: email is mandatory; {@code userId} and
 * {@code commitment} are always set to null by the mapper. The user
 * later claims the enrollment and provides these values.
 */
@Component
@RequiredArgsConstructor
@Transactional
public class CreateEnrollmentsBatchUseCase {

    private final EnrollmentRepository enrollmentRepository;
    private final ElectoralProcessRepository electoralProcessRepository;
    private final EnrollmentMapper mapper;

    /**
     * Creates multiple enrollments in a single atomic operation.
     *
     * @param processId the electoral process to create enrollments in
     * @param requests  the list of enrollment creation payloads (must not be empty)
     * @return the list of created enrollments as response DTOs
     * @throws EnrollmentException        if the list is empty, the process is not
     *                                    in NONE/COMMITMENT state, or duplicate
     *                                    emails exist within the batch or in the database
     * @throws ElectoralProcessException  if the process does not exist
     */
    public List<EnrollmentResponse> execute(UUID processId, List<CreateEnrollmentRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw EnrollmentException.emptyBatch();
        }

        var process = electoralProcessRepository.findById(processId)
                .orElseThrow(() -> ElectoralProcessException.notFound(processId));

        var now = Instant.now();
        ProcessStateCalculator.transitionState(process, now);
        var state = ProcessStateCalculator.computeState(process, now);
        if (state != ProcessStatus.NONE && state != ProcessStatus.COMMITMENT) {
            throw EnrollmentException.invalidState("Enrollment not open for this process");
        }

        var emails = requests.stream()
                .map(CreateEnrollmentRequest::getEmail)
                .collect(Collectors.toList());

        // Within-batch email dedup
        var emailSet = new HashSet<>(emails);
        if (emailSet.size() < emails.size()) {
            var seen = new HashSet<String>();
            for (var email : emails) {
                if (!seen.add(email)) {
                    throw EnrollmentException.duplicateEmailInBatch(email);
                }
            }
        }

        // Cross-batch email conflict
        var existingEmails = enrollmentRepository.findEmailsByProcessIdAndEmailsIn(processId, emails);
        if (!existingEmails.isEmpty()) {
            throw EnrollmentException.emailAlreadyRegistered(processId, existingEmails.get(0));
        }

        var entities = requests.stream()
                .map(req -> mapper.toEntity(req, processId))
                .collect(Collectors.toList());

        var saved = enrollmentRepository.saveAll(entities);

        return saved.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
}
