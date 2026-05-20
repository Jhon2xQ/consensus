package com.carmenio.consensus.application.dto.enrollment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

/**
 * Request DTO for claiming an existing enrollment.
 * <p>
 * Used in the <b>user phase</b> of the two-phase enrollment flow.
 * The user provides the electoral process ID and their Semaphore
 * commitment. The JWT {@code email} claim is matched against the
 * enrollment's pre-registered email by the use case.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimEnrollmentRequest {

    @NotNull(message = "Electoral process ID is required")
    private UUID electoralProcessId;

    @NotBlank(message = "Semaphore commitment is required")
    private String commitment;
}
