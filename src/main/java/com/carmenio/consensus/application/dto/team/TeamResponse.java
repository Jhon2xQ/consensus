package com.carmenio.consensus.application.dto.team;

import lombok.*;

import java.util.UUID;

/**
 * Response DTO for team data exposed via the API.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamResponse {

    private UUID id;
    private String name;
    private String avatarUrl;
    private UUID electoralProcessId;
}
