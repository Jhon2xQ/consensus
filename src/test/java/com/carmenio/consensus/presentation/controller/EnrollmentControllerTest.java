package com.carmenio.consensus.presentation.controller;

import com.carmenio.consensus.application.dto.enrollment.ClaimEnrollmentRequest;
import com.carmenio.consensus.application.dto.enrollment.CreateEnrollmentRequest;
import com.carmenio.consensus.application.dto.enrollment.EnrollmentResponse;
import com.carmenio.consensus.application.use_case.enrollment.ClaimEnrollmentUseCase;
import com.carmenio.consensus.application.use_case.enrollment.CreateEnrollmentsBatchUseCase;
import com.carmenio.consensus.application.use_case.enrollment.DeleteEnrollmentUseCase;
import com.carmenio.consensus.application.use_case.enrollment.FindEnrollmentByIdUseCase;
import com.carmenio.consensus.application.use_case.enrollment.ListEnrollmentsByProcessUseCase;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.domain.exception.EnrollmentException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnrollmentController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateEnrollmentsBatchUseCase createEnrollmentsBatchUseCase;

    @MockitoBean
    private FindEnrollmentByIdUseCase findEnrollmentByIdUseCase;

    @MockitoBean
    private ListEnrollmentsByProcessUseCase listEnrollmentsByProcessUseCase;

    @MockitoBean
    private ClaimEnrollmentUseCase claimEnrollmentUseCase;

    @MockitoBean
    private DeleteEnrollmentUseCase deleteEnrollmentUseCase;

    // ── POST /private/processes/{processId}/enrollments (batch) ──

    @Test
    @DisplayName("POST /private/processes/{processId}/enrollments should create single enrollment in batch")
    void shouldCreateSingleEnrollmentInBatch() throws Exception {
        var processId = UUID.randomUUID();
        var request = List.of(CreateEnrollmentRequest.builder()
                .email("voter@example.com")
                .build());

        var response = List.of(EnrollmentResponse.builder()
                .id(UUID.randomUUID())
                .electoralProcessId(processId)
                .email("voter@example.com")
                .userId(null)
                .commitment(null)
                .hasVoted(false)
                .build());

        when(createEnrollmentsBatchUseCase.execute(any(UUID.class), any()))
                .thenReturn(response);

        mockMvc.perform(post("/private/processes/{processId}/enrollments", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].email").value("voter@example.com"))
                .andExpect(jsonPath("$.data[0].electoralProcessId").value(processId.toString()))
                .andExpect(jsonPath("$.data[0].hasVoted").value(false));
    }

    @Test
    @DisplayName("POST /private/processes/{processId}/enrollments should create multiple enrollments in batch")
    void shouldCreateMultipleEnrollmentsInBatch() throws Exception {
        var processId = UUID.randomUUID();
        var requests = List.of(
                CreateEnrollmentRequest.builder().email("voter1@example.com").build(),
                CreateEnrollmentRequest.builder().email("voter2@example.com").build(),
                CreateEnrollmentRequest.builder().email("voter3@example.com").build()
        );

        var responses = List.of(
                EnrollmentResponse.builder().id(UUID.randomUUID()).electoralProcessId(processId).email("voter1@example.com").hasVoted(false).build(),
                EnrollmentResponse.builder().id(UUID.randomUUID()).electoralProcessId(processId).email("voter2@example.com").hasVoted(false).build(),
                EnrollmentResponse.builder().id(UUID.randomUUID()).electoralProcessId(processId).email("voter3@example.com").hasVoted(false).build()
        );

        when(createEnrollmentsBatchUseCase.execute(any(UUID.class), any()))
                .thenReturn(responses);

        mockMvc.perform(post("/private/processes/{processId}/enrollments", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].email").value("voter1@example.com"))
                .andExpect(jsonPath("$.data[1].email").value("voter2@example.com"))
                .andExpect(jsonPath("$.data[2].email").value("voter3@example.com"));
    }

    @Test
    @DisplayName("POST /private/processes/{processId}/enrollments should return 400 for empty array")
    void shouldReturn400WhenEmptyArray() throws Exception {
        var processId = UUID.randomUUID();

        when(createEnrollmentsBatchUseCase.execute(any(UUID.class), any()))
                .thenThrow(EnrollmentException.emptyBatch());

        mockMvc.perform(post("/private/processes/{processId}/enrollments", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /private/processes/{processId}/enrollments should return 400 when email is blank")
    void shouldReturn400WhenEmailIsBlank() throws Exception {
        var processId = UUID.randomUUID();
        var body = "[{\"email\":\"\"}]";

        // Spring MVC validation rejects before use case is called
        mockMvc.perform(post("/private/processes/{processId}/enrollments", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /private/processes/{processId}/enrollments should return 400 when using single object body (not array)")
    void shouldReturn400WhenSingleObjectNotArray() throws Exception {
        var processId = UUID.randomUUID();
        var body = objectMapper.writeValueAsString(
                CreateEnrollmentRequest.builder().email("voter@example.com").build()
        );

        mockMvc.perform(post("/private/processes/{processId}/enrollments", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /private/processes/{processId}/enrollments should return 404 when process not found")
    void shouldReturn404WhenProcessNotFound() throws Exception {
        var processId = UUID.randomUUID();
        var request = List.of(CreateEnrollmentRequest.builder()
                .email("voter@example.com")
                .build());

        when(createEnrollmentsBatchUseCase.execute(any(UUID.class), any()))
                .thenThrow(ElectoralProcessException.notFound(processId));

        mockMvc.perform(post("/private/processes/{processId}/enrollments", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /private/processes/{processId}/enrollments should return 409 on within-batch duplicate email")
    void shouldReturn409WhenWithinBatchDuplicateEmail() throws Exception {
        var processId = UUID.randomUUID();

        when(createEnrollmentsBatchUseCase.execute(any(UUID.class), any()))
                .thenThrow(EnrollmentException.duplicateEmailInBatch("dup@example.com"));

        var body = "[{\"email\":\"dup@example.com\"},{\"email\":\"dup@example.com\"}]";

        mockMvc.perform(post("/private/processes/{processId}/enrollments", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /private/processes/{processId}/enrollments should return 409 on cross-batch DB email conflict")
    void shouldReturn409WhenCrossBatchDbEmailConflict() throws Exception {
        var processId = UUID.randomUUID();
        var request = List.of(CreateEnrollmentRequest.builder()
                .email("existing@example.com")
                .build());

        when(createEnrollmentsBatchUseCase.execute(any(UUID.class), any()))
                .thenThrow(EnrollmentException.emailAlreadyRegistered(processId, "existing@example.com"));

        mockMvc.perform(post("/private/processes/{processId}/enrollments", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /private/processes/{processId}/enrollments should return 400 when process state is invalid")
    void shouldReturn400WhenInvalidState() throws Exception {
        var processId = UUID.randomUUID();
        var request = List.of(CreateEnrollmentRequest.builder()
                .email("voter@example.com")
                .build());

        when(createEnrollmentsBatchUseCase.execute(any(UUID.class), any()))
                .thenThrow(EnrollmentException.invalidState("Enrollment not open for this process"));

        mockMvc.perform(post("/private/processes/{processId}/enrollments", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── PUT /private/enrollments/{id}/commitment ──

    @Test
    @DisplayName("PUT /private/enrollments/{id}/commitment should claim enrollment")
    void shouldClaimEnrollment() throws Exception {
        var enrollmentId = UUID.randomUUID();
        var processId = UUID.randomUUID();

        var request = ClaimEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .build();

        var response = EnrollmentResponse.builder()
                .id(enrollmentId)
                .electoralProcessId(processId)
                .email("voter@example.com")
                .userId("user-123")
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .hasVoted(false)
                .build();

        when(claimEnrollmentUseCase.execute(eq(enrollmentId), any(), any())).thenReturn(response);

        mockMvc.perform(put("/private/enrollments/{id}/commitment", enrollmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("voter@example.com"))
                .andExpect(jsonPath("$.data.userId").value("user-123"))
                .andExpect(jsonPath("$.data.commitment").value("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111"));
    }

    @Test
    @DisplayName("PUT should return 404 when enrollment not found or email mismatch")
    void shouldReturn404WhenClaimNotFound() throws Exception {
        var enrollmentId = UUID.randomUUID();
        var processId = UUID.randomUUID();

        var request = ClaimEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .build();

        when(claimEnrollmentUseCase.execute(eq(enrollmentId), any(), any()))
                .thenThrow(EnrollmentException.emailMismatch());

        mockMvc.perform(put("/private/enrollments/{id}/commitment", enrollmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("PUT should return 409 when commitment already exists")
    void shouldReturn409WhenClaimCommitmentDuplicate() throws Exception {
        var enrollmentId = UUID.randomUUID();
        var processId = UUID.randomUUID();

        var request = ClaimEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .build();

        when(claimEnrollmentUseCase.execute(eq(enrollmentId), any(), any()))
                .thenThrow(EnrollmentException.duplicateCommitment());

        mockMvc.perform(put("/private/enrollments/{id}/commitment", enrollmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("PUT should return 400 when process is in wrong state")
    void shouldReturn400WhenClaimInvalidState() throws Exception {
        var enrollmentId = UUID.randomUUID();
        var processId = UUID.randomUUID();

        var request = ClaimEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .build();

        when(claimEnrollmentUseCase.execute(eq(enrollmentId), any(), any()))
                .thenThrow(EnrollmentException.invalidState("Enrollment not open for this process"));

        mockMvc.perform(put("/private/enrollments/{id}/commitment", enrollmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("PUT should return 409 when enrollment is already claimed")
    void shouldReturn409WhenAlreadyClaimed() throws Exception {
        var enrollmentId = UUID.randomUUID();
        var processId = UUID.randomUUID();

        var request = ClaimEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .build();

        when(claimEnrollmentUseCase.execute(eq(enrollmentId), any(), any()))
                .thenThrow(EnrollmentException.alreadyExists("userId"));

        mockMvc.perform(put("/private/enrollments/{id}/commitment", enrollmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── GET /private/processes/{processId}/enrollments ──

    @Test
    @DisplayName("GET /private/processes/{processId}/enrollments should list enrollments")
    void shouldListEnrollments() throws Exception {
        var processId = UUID.randomUUID();
        var enrollment = EnrollmentResponse.builder()
                .id(UUID.randomUUID())
                .electoralProcessId(processId)
                .userId("user-123")
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .hasVoted(false)
                .build();

        when(listEnrollmentsByProcessUseCase.execute(processId)).thenReturn(List.of(enrollment));

        mockMvc.perform(get("/private/processes/{processId}/enrollments", processId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].userId").value("user-123"));
    }

    @Test
    @DisplayName("GET /private/processes/{processId}/enrollments should return empty list")
    void shouldReturnEmptyList() throws Exception {
        var processId = UUID.randomUUID();

        when(listEnrollmentsByProcessUseCase.execute(processId)).thenReturn(List.of());

        mockMvc.perform(get("/private/processes/{processId}/enrollments", processId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // ── GET /private/enrollments/{id} ──

    @Test
    @DisplayName("GET /private/enrollments/{id} should return enrollment")
    void shouldFindEnrollmentById() throws Exception {
        var id = UUID.randomUUID();
        var response = EnrollmentResponse.builder()
                .id(id)
                .electoralProcessId(UUID.randomUUID())
                .userId("found-user")
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .hasVoted(false)
                .build();

        when(findEnrollmentByIdUseCase.execute(id)).thenReturn(response);

        mockMvc.perform(get("/private/enrollments/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("found-user"));
    }

    @Test
    @DisplayName("GET /private/enrollments/{id} should return 404 when not found")
    void shouldReturn404WhenEnrollmentNotFound() throws Exception {
        var id = UUID.randomUUID();

        when(findEnrollmentByIdUseCase.execute(id)).thenThrow(EnrollmentException.notFound(id));

        mockMvc.perform(get("/private/enrollments/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── DELETE /private/enrollments/{id} ──

    @Test
    @DisplayName("DELETE /private/enrollments/{id} should delete enrollment")
    void shouldDeleteEnrollment() throws Exception {
        var id = UUID.randomUUID();

        doNothing().when(deleteEnrollmentUseCase).execute(id);

        mockMvc.perform(delete("/private/enrollments/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /private/enrollments/{id} should return 404 when not found")
    void shouldReturn404WhenDeleteEnrollmentNotFound() throws Exception {
        var id = UUID.randomUUID();

        doThrow(EnrollmentException.notFound(id)).when(deleteEnrollmentUseCase).execute(id);

        mockMvc.perform(delete("/private/enrollments/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
