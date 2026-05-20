package com.carmenio.consensus.application.dto.enrollment;

import lombok.*;

import java.util.UUID;

/**
 * Response DTO for enrollment data exposed via the API.
 * <p>
 * The {@code email} field is populated in both phases. {@code userId}
 * and {@code commitment} may be {@code null} for enrollments created
 * in the creator phase and not yet claimed by a user.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentResponse {

    private UUID id;
    private UUID electoralProcessId;
    private String email;
    private String userId;
    private String commitment;
    private boolean hasVoted;
}
