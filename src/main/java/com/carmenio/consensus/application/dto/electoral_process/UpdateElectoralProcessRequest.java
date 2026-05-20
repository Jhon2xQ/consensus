package com.carmenio.consensus.application.dto.electoral_process;

import lombok.*;

import java.time.Instant;

/**
 * Request DTO for updating an existing electoral process.
 * <p>
 * All fields are optional — only provided fields will be updated.
 * The state machine auto-transitions via
 * {@code ProcessStateCalculator.transitionState()}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateElectoralProcessRequest {

    private String name;

    private String description;

    private Instant commitmentStart;

    private Instant commitmentEnd;

    private Instant votingStart;

    private Instant votingEnd;

    private Instant results;
}
