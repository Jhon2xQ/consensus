package com.carmenio.consensus.application.dto.electoral_process;

import com.carmenio.consensus.common.constant.ProcessStatus;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for electoral process data exposed via the API.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElectoralProcessResponse {

    private UUID id;
    private String name;
    private String scope;
    private String description;
    private ProcessStatus estatus;
    private Instant commitmentStart;
    private Instant commitmentEnd;
    private Instant votingStart;
    private Instant votingEnd;
    private Instant results;
}
