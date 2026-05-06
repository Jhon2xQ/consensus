package com.carmenio.consensus.infrastructure.mapper;

import com.carmenio.consensus.application.dto.electoral_process.CreateElectoralProcessRequest;
import com.carmenio.consensus.application.dto.electoral_process.ElectoralProcessResponse;
import com.carmenio.consensus.application.dto.electoral_process.UpdateElectoralProcessRequest;
import com.carmenio.consensus.common.constant.ProcessStatus;
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
     * <p>
     * {@code estatus} is not set from the request — the entity builder defaults
     * it to {@code NONE}. The use case will call {@code transitionState()}
     * after save to set the correct initial state.
     */
    public ElectoralProcess toEntity(CreateElectoralProcessRequest request) {
        return ElectoralProcess.builder()
                .name(request.getName())
                .scope(request.getScope())
                .description(request.getDescription())
                .commitmentStart(request.getCommitmentStart())
                .commitmentEnd(request.getCommitmentEnd())
                .votingStart(request.getVotingStart())
                .votingEnd(request.getVotingEnd())
                .results(request.getResults())
                .build();
    }

    /**
     * Applies non-null fields from an update request to an existing entity.
     * <p>
     * If {@code estatus} is non-null, it is applied as a manual override
     * (typically PAUSED or CANCELLED). The use case will call
     * {@code transitionState()} afterwards for date-based auto-transition.
     */
    public void updateEntity(ElectoralProcess entity, UpdateElectoralProcessRequest request) {
        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
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
        if (request.getEstatus() != null) {
            entity.setEstatus(request.getEstatus());
        }
    }

    /**
     * Converts an entity to a response DTO using the provided {@code computedStatus}.
     * <p>
     * The {@code computedStatus} is the result of either
     * {@code ProcessStateCalculator.transitionState()} or
     * {@code ProcessStateCalculator.computeState()}, ensuring the response
     * always reflects the current state regardless of the stored DB value.
     *
     * @param entity         the entity to map
     * @param computedStatus the current computed state (not the raw DB value)
     * @return the response DTO with fresh estatus
     */
    public ElectoralProcessResponse toResponse(ElectoralProcess entity, ProcessStatus computedStatus) {
        return ElectoralProcessResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .scope(entity.getScope())
                .description(entity.getDescription())
                .estatus(computedStatus)
                .commitmentStart(entity.getCommitmentStart())
                .commitmentEnd(entity.getCommitmentEnd())
                .votingStart(entity.getVotingStart())
                .votingEnd(entity.getVotingEnd())
                .results(entity.getResults())
                .build();
    }
}
