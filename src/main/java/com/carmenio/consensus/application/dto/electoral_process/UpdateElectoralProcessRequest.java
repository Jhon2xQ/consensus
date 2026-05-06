package com.carmenio.consensus.application.dto.electoral_process;

import com.carmenio.consensus.common.constant.ProcessStatus;
import lombok.*;

import java.time.Instant;

/**
 * Request DTO for updating an existing electoral process.
 * <p>
 * All fields are optional — only provided fields will be updated.
 * <p>
 * When {@code estatus} is provided, it acts as a manual override
 * (typically {@code PAUSED} or {@code CANCELLED}). When not provided,
 * the state machine auto-transitions via
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

    private ProcessStatus estatus;
}
