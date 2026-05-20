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
     * <p>
     * In the creator phase, email is mandatory while userId and commitment may be null.
     */
    public Enrollment toEntity(CreateEnrollmentRequest request) {
        return Enrollment.builder()
                .electoralProcessId(request.getElectoralProcessId())
                .email(request.getEmail())
                .userId(request.getUserId())
                .commitment(request.getCommitment())
                .build();
    }

    /**
     * Converts an entity to a response DTO.
     * <p>
     * Includes the email field for the two-phase enrollment flow.
     * userId and commitment may be null for enrollments not yet claimed.
     */
    public EnrollmentResponse toResponse(Enrollment entity) {
        return EnrollmentResponse.builder()
                .id(entity.getId())
                .electoralProcessId(entity.getElectoralProcessId())
                .email(entity.getEmail())
                .userId(entity.getUserId())
                .commitment(entity.getCommitment())
                .hasVoted(entity.isHasVoted())
                .build();
    }
}
