package com.carmenio.consensus.application.dto.enrollment;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Request DTO for creating a new enrollment within an electoral process.
 * <p>
 * In the <b>creator phase</b>, only {@code email} is required.
 * The {@code userId} and {@code commitment} are set later when the
 * user claims the enrollment.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEnrollmentRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}
