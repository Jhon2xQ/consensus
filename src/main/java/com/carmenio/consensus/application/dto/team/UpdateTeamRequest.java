package com.carmenio.consensus.application.dto.team;

import lombok.*;

/**
 * Request DTO for updating an existing team.
 * <p>
 * All fields are optional — only provided fields will be updated.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTeamRequest {

    private String name;
    private String avatarUrl;
}
