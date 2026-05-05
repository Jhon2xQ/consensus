package com.carmenio.consensus.application.dto.team;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

/**
 * Request DTO for creating a new team within an electoral process.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTeamRequest {

    @NotBlank(message = "Team name is required")
    private String name;

    private String avatarUrl;

    @NotNull(message = "Electoral process ID is required")
    private UUID electoralProcessId;
}
