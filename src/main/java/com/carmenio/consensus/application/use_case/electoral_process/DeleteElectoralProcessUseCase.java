package com.carmenio.consensus.application.use_case.electoral_process;

import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.domain.repository.EnrollmentRepository;
import com.carmenio.consensus.domain.repository.TeamRepository;
import com.carmenio.consensus.domain.repository.VoteRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for deleting an electoral process.
 * <p>
 * Deletes the process and all related records in a single transaction:
 * teams, enrollments, and vote records are removed before the process itself.
 */
@Component
@RequiredArgsConstructor
@Transactional
public class DeleteElectoralProcessUseCase {

    private final ElectoralProcessRepository electoralProcessRepository;
    private final TeamRepository teamRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final VoteRecordRepository voteRecordRepository;

    /**
     * Deletes an electoral process by ID, cascading to all related records.
     *
     * @param id the process UUID
     * @throws ElectoralProcessException if no process exists with the given ID
     */
    public void execute(UUID id) {
        var entity = electoralProcessRepository.findById(id)
                .orElseThrow(() -> ElectoralProcessException.notFound(id));

        teamRepository.findByElectoralProcessId(id).forEach(teamRepository::delete);
        enrollmentRepository.findByElectoralProcessId(id).forEach(enrollmentRepository::delete);
        voteRecordRepository.findByScope(entity.getScope()).forEach(voteRecordRepository::delete);

        electoralProcessRepository.delete(entity);
    }
}
