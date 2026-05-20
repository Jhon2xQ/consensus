package com.carmenio.consensus.application.dto.enrollment;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

/**
 * Request DTO for creating a new enrollment within an electoral process.
 * <p>
 * Two-phase flow: in the <b>creator phase</b>, only {@code electoralProcessId}
 * and {@code email} are required. The {@code userId} and {@code commitment}
 * fields are optional and remain {@code null} until the user claims the
 * enrollment in phase 2.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEnrollmentRequest {

    @NotNull(message = "Electoral process ID is required")
    private UUID electoralProcessId;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String userId;

    private String commitment;
}
