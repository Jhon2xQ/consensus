package com.carmenio.consensus.infrastructure.mapper;

import com.carmenio.consensus.application.dto.enrollment.CreateEnrollmentRequest;
import com.carmenio.consensus.application.dto.enrollment.EnrollmentResponse;
import com.carmenio.consensus.domain.entity.Enrollment;
import org.springframework.stereotype.Component;

/**
 * Mapper for {@link Enrollment} entity ↔ DTO conversions.
 * <p>
 * Lives in the infrastructure layer because it knows about JPA entity details.
 * Application and domain layers remain JPA-free.
 */
@Component
public class EnrollmentMapper {

    /**
     * Converts a create request to a new entity (with null ID for JPA generation).
     */
    public Enrollment toEntity(CreateEnrollmentRequest request) {
        return Enrollment.builder()
                .electoralProcessId(request.getElectoralProcessId())
                .userId(request.getUserId())
                .commitment(request.getCommitment())
                .build();
    }

    /**
     * Converts an entity to a response DTO.
     */
    public EnrollmentResponse toResponse(Enrollment entity) {
        return EnrollmentResponse.builder()
                .id(entity.getId())
                .electoralProcessId(entity.getElectoralProcessId())
                .userId(entity.getUserId())
                .commitment(entity.getCommitment())
                .hasVoted(entity.isHasVoted())
                .build();
    }
}
