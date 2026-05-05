package com.carmenio.consensus.application.dto.electoral_process;

import com.carmenio.consensus.common.constant.ProcessStatus;
import lombok.*;

import java.util.UUID;

/**
 * Response DTO representing the current state of an electoral process.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessStateResponse {

    private UUID processId;
    private ProcessStatus state;
}
