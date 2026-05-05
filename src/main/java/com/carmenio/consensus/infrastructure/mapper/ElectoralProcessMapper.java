package com.carmenio.consensus.infrastructure.mapper;

import com.carmenio.consensus.application.dto.electoral_process.CreateElectoralProcessRequest;
import com.carmenio.consensus.application.dto.electoral_process.ElectoralProcessResponse;
import com.carmenio.consensus.application.dto.electoral_process.UpdateElectoralProcessRequest;
import com.carmenio.consensus.domain.entity.ElectoralProcess;
import org.springframework.stereotype.Component;

/**
 * Mapper for {@link ElectoralProcess} entity ↔ DTO conversions.
 * <p>
 * Lives in the infrastructure layer because it knows about JPA entity details.
 * Application and domain layers remain JPA-free.
 */
@Component
public class ElectoralProcessMapper {

    /**
     * Converts a create request to a new entity (with null ID for JPA generation).
     */
    public ElectoralProcess toEntity(CreateElectoralProcessRequest request) {
        return ElectoralProcess.builder()
                .name(request.getName())
                .scope(request.getScope())
                .commitmentStart(request.getCommitmentStart())
                .commitmentEnd(request.getCommitmentEnd())
                .votingStart(request.getVotingStart())
                .votingEnd(request.getVotingEnd())
                .results(request.getResults())
                .build();
    }

    /**
     * Applies non-null fields from an update request to an existing entity.
     */
    public void updateEntity(ElectoralProcess entity, UpdateElectoralProcessRequest request) {
        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getCommitmentStart() != null) {
            entity.setCommitmentStart(request.getCommitmentStart());
        }
        if (request.getCommitmentEnd() != null) {
            entity.setCommitmentEnd(request.getCommitmentEnd());
        }
        if (request.getVotingStart() != null) {
            entity.setVotingStart(request.getVotingStart());
        }
        if (request.getVotingEnd() != null) {
            entity.setVotingEnd(request.getVotingEnd());
        }
        if (request.getResults() != null) {
            entity.setResults(request.getResults());
        }
    }

    /**
     * Converts an entity to a response DTO.
     */
    public ElectoralProcessResponse toResponse(ElectoralProcess entity) {
        return ElectoralProcessResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .scope(entity.getScope())
                .commitmentStart(entity.getCommitmentStart())
                .commitmentEnd(entity.getCommitmentEnd())
                .votingStart(entity.getVotingStart())
                .votingEnd(entity.getVotingEnd())
                .results(entity.getResults())
                .build();
    }
}
