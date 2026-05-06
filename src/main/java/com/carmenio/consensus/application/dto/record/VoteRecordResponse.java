package com.carmenio.consensus.application.dto.record;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for vote record data exposed via the API.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoteRecordResponse {

    private UUID id;
    private String groupId;
    private String nullifier;
    private String message;
    private String scope;
    private String transactionHash;
    private Instant createdAt;
}
