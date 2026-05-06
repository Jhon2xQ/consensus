package com.carmenio.consensus.infrastructure.mapper;

import com.carmenio.consensus.application.dto.record.CreateVoteRecordRequest;
import com.carmenio.consensus.application.dto.record.VoteRecordResponse;
import com.carmenio.consensus.domain.entity.VoteRecord;
import org.springframework.stereotype.Component;

/**
 * Mapper for {@link VoteRecord} entity ↔ DTO conversions.
 * <p>
 * Lives in the infrastructure layer because it knows about JPA entity details.
 * Application and domain layers remain JPA-free.
 */
@Component
public class VoteRecordMapper {

    /**
     * Converts a create request to a new entity (with null ID for JPA generation).
     */
    public VoteRecord toEntity(CreateVoteRecordRequest request) {
        return VoteRecord.builder()
                .groupId(request.getGroupId())
                .nullifier(request.getNullifier())
                .message(request.getMessage())
                .scope(request.getScope())
                .transactionHash(request.getTransactionHash())
                .build();
    }

    /**
     * Converts an entity to a response DTO.
     */
    public VoteRecordResponse toResponse(VoteRecord entity) {
        return VoteRecordResponse.builder()
                .id(entity.getId())
                .groupId(entity.getGroupId())
                .nullifier(entity.getNullifier())
                .message(entity.getMessage())
                .scope(entity.getScope())
                .transactionHash(entity.getTransactionHash())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
