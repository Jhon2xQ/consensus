package com.carmenio.consensus.application.use_case.record;

import com.carmenio.consensus.application.dto.record.CreateVoteRecordRequest;
import com.carmenio.consensus.application.dto.record.VoteRecordResponse;
import com.carmenio.consensus.domain.exception.RecordException;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.domain.repository.TeamRepository;
import com.carmenio.consensus.domain.repository.VoteRecordRepository;
import com.carmenio.consensus.infrastructure.mapper.VoteRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for creating a new vote record ingested from the Semaphore Relayer.
 * <p>
 * Validates that the scope corresponds to an existing electoral process and
 * that the message matches a team within that process. Idempotent on nullifier
 * — if a record with the same nullifier already exists, it returns the existing
 * record instead of creating a duplicate.
 */
@Component
@RequiredArgsConstructor
@Transactional
public class CreateVoteRecordUseCase {

    private final ElectoralProcessRepository electoralProcessRepository;
    private final VoteRecordRepository voteRecordRepository;
    private final TeamRepository teamRepository;
    private final VoteRecordMapper mapper;

    /**
     * Creates a vote record after validating scope and message.
     *
     * @param request the creation payload from the Semaphore Relayer
     * @return the created (or existing, if idempotent) record as a response DTO
     * @throws RecordException if the scope does not match any process
     *                         or the message does not match any team
     */
    public VoteRecordResponse execute(CreateVoteRecordRequest request) {
        // Validate scope → existing electoral process
        var process = electoralProcessRepository.findByScope(request.getScope())
                .orElseThrow(RecordException::invalidScope);

        // Idempotency: if nullifier already exists, return existing record
        var existingRecord = voteRecordRepository.findByNullifier(request.getNullifier());
        if (existingRecord.isPresent()) {
            return mapper.toResponse(existingRecord.get());
        }

        // Validate message → existing team for this process
        var teams = teamRepository.findByElectoralProcessId(process.getId());
        var teamExists = teams.stream()
                .anyMatch(team -> team.getName().equals(request.getMessage()));
        if (!teamExists) {
            throw RecordException.invalidMessage();
        }

        // Persist and return
        var entity = mapper.toEntity(request);
        var saved = voteRecordRepository.save(entity);
        return mapper.toResponse(saved);
    }
}
