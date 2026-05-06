package com.carmenio.consensus.application.dto.record;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Request DTO for creating a new vote record (ingested from Semaphore Relayer).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateVoteRecordRequest {

    @NotBlank(message = "Group ID is required")
    private String groupId;

    @NotBlank(message = "Nullifier is required")
    private String nullifier;

    @NotBlank(message = "Message is required")
    private String message;

    @NotBlank(message = "Scope is required")
    private String scope;

    private String transactionHash;
}
