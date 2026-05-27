package com.carmenio.consensus.application.use_case.enrollment;

import com.carmenio.consensus.application.dto.enrollment.EnrollmentStatsResponse;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.domain.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for retrieving enrollment statistics for an electoral process.
 * <p>
 * Returns aggregate counts — total enrollments, participants with a claimed
 * userId, participants who submitted a commitment, and participants who voted.
 * Validates that the process exists before querying.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetEnrollmentStatsUseCase {

    private final EnrollmentRepository enrollmentRepository;
    private final ElectoralProcessRepository electoralProcessRepository;

    /**
     * Retrieves enrollment statistics for the given electoral process.
     *
     * @param processId the electoral process UUID
     * @return aggregate statistics DTO
     * @throws ElectoralProcessException if the process does not exist
     */
    public EnrollmentStatsResponse execute(UUID processId) {
        electoralProcessRepository.findById(processId)
                .orElseThrow(() -> ElectoralProcessException.notFound(processId));

        long totalParticipants = enrollmentRepository.countByElectoralProcessId(processId);
        long totalCommitments = enrollmentRepository.countByElectoralProcessIdAndCommitmentNotNull(processId);
        long totalVoted = enrollmentRepository.countByElectoralProcessIdAndHasVotedTrue(processId);

        return EnrollmentStatsResponse.builder()
                .totalParticipants(totalParticipants)
                .totalCommitments(totalCommitments)
                .totalVoted(totalVoted)
                .build();
    }
}
