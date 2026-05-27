package com.carmenio.consensus.application.dto.enrollment;

import lombok.*;

/**
 * Response DTO for public enrollment statistics of an electoral process.
 * <p>
 * Provides aggregate counts without exposing individual voter data.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentStatsResponse {

    private long totalParticipants;
    private long totalCommitments;
    private long totalVoted;
}
