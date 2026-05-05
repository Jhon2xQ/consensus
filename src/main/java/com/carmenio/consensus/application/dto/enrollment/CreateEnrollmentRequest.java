package com.carmenio.consensus.application.dto.enrollment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

/**
 * Request DTO for creating a new enrollment within an electoral process.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEnrollmentRequest {

    @NotNull(message = "Electoral process ID is required")
    private UUID electoralProcessId;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Semaphore commitment is required")
    private String commitment;
}
