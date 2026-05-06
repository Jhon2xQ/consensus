package com.carmenio.consensus.application.dto.electoral_process;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

/**
 * Request DTO for creating a new electoral process.
 * <p>
 * {@code estatus} is not part of the create request — it defaults to
 * {@code NONE} on the entity and is auto-transitioned by
 * {@code ProcessStateCalculator.transitionState()} after save.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateElectoralProcessRequest {

    @NotBlank(message = "Process name is required")
    private String name;

    @NotBlank(message = "Process scope is required")
    private String scope;

    private String description;

    @NotNull(message = "Commitment start date is required")
    private Instant commitmentStart;

    @NotNull(message = "Commitment end date is required")
    private Instant commitmentEnd;

    @NotNull(message = "Voting start date is required")
    private Instant votingStart;

    @NotNull(message = "Voting end date is required")
    private Instant votingEnd;

    @NotNull(message = "Results date is required")
    private Instant results;
}
