package com.carmenio.consensus.application.dto.enrollment;

import lombok.*;

import java.util.UUID;

/**
 * Response DTO for enrollment data exposed via the API.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentResponse {

    private UUID id;
    private UUID electoralProcessId;
    private String userId;
    private String commitment;
    private boolean hasVoted;
}
