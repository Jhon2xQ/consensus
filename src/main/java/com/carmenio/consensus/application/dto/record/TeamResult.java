package com.carmenio.consensus.application.dto.record;

import lombok.*;

/**
 * Represents a team's vote tally in process results.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamResult {

    private String teamName;
    private long voteCount;
}
