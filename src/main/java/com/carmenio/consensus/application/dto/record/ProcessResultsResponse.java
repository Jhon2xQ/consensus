package com.carmenio.consensus.application.dto.record;

import lombok.*;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for electoral process results.
 * <p>
 * Contains the total vote count and per-team tallies,
 * only available when the process is in CLOSED state.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessResultsResponse {

    private UUID processId;
    private String processName;
    private List<TeamResult> teamResults;
    private long totalVotes;
    private String status;
}
