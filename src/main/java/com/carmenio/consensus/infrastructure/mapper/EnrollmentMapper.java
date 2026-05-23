package com.carmenio.consensus.infrastructure.mapper;

import com.carmenio.consensus.application.dto.enrollment.CreateEnrollmentRequest;
import com.carmenio.consensus.application.dto.enrollment.EnrollmentResponse;
import com.carmenio.consensus.domain.entity.Enrollment;
import org.springframework.stereotype.Component;

import java.util.UUID;

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
     * In the creator phase, email is mandatory while userId and commitment
     * are always set to null — they are populated later in the claim phase.
     */
    public Enrollment toEntity(CreateEnrollmentRequest request, UUID processId) {
        return Enrollment.builder()
                .electoralProcessId(processId)
                .email(request.getEmail())
                .userId(null)
                .commitment(null)
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
