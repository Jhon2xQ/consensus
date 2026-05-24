package com.carmenio.consensus.application.use_case.team;

import com.carmenio.consensus.application.dto.team.CreateTeamRequest;
import com.carmenio.consensus.application.dto.team.TeamResponse;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.domain.exception.TeamException;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.domain.repository.TeamRepository;
import com.carmenio.consensus.infrastructure.mapper.TeamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Batch use case for creating multiple teams within an electoral process
 * in a single all-or-nothing transaction.
 * <p>
 * <b>Algorithm</b>:
 * <ol>
 *   <li>Validate the request list is not empty</li>
 *   <li>Validate the electoral process exists</li>
 *   <li>Within-batch dedup via {@link HashSet} on team names</li>
 *   <li>Cross-batch conflict via single DB {@code WHERE IN} query</li>
 *   <li>Map requests to entities via {@link TeamMapper}</li>
 *   <li>Batch persist via {@link TeamRepository#saveAll(List)}</li>
 *   <li>Map saved entities back to response DTOs</li>
 * </ol>
 * All validation happens before any persistence — any failure rolls back
 * the entire transaction.
 */
@Component
@RequiredArgsConstructor
@Transactional
public class CreateTeamsBatchUseCase {

    private final TeamRepository teamRepository;
    private final ElectoralProcessRepository electoralProcessRepository;
    private final TeamMapper mapper;

    /**
     * Creates multiple teams in a single atomic operation.
     *
     * @param processId the electoral process to create the teams in
     * @param requests  the list of team creation payloads (must not be empty)
     * @return the list of created teams as response DTOs
     * @throws TeamException              if the list is empty, or if duplicate
     *                                    names exist within the batch or in the database
     * @throws ElectoralProcessException  if the process does not exist
     */
    public List<TeamResponse> execute(UUID processId, List<CreateTeamRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw TeamException.emptyBatch();
        }

        electoralProcessRepository.findById(processId)
                .orElseThrow(() -> ElectoralProcessException.notFound(processId));

        var names = requests.stream()
                .map(CreateTeamRequest::getName)
                .collect(Collectors.toList());

        // Within-batch dedup
        var nameSet = new HashSet<>(names);
        if (nameSet.size() < names.size()) {
            var seen = new HashSet<String>();
            for (var name : names) {
                if (!seen.add(name)) {
                    throw TeamException.duplicateInBatch(name);
                }
            }
        }

        // Cross-batch conflict
        var existingNames = teamRepository.findNamesByProcessIdAndNamesIn(processId, names);
        if (!existingNames.isEmpty()) {
            throw TeamException.alreadyExists(existingNames.get(0));
        }

        var entities = requests.stream()
                .map(req -> mapper.toEntity(req, processId))
                .collect(Collectors.toList());

        var saved = teamRepository.saveAll(entities);

        return saved.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
}
