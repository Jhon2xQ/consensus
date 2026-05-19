package com.carmenio.consensus.presentation.controller;

import com.carmenio.consensus.application.dto.enrollment.CreateEnrollmentRequest;
import com.carmenio.consensus.application.dto.enrollment.EnrollmentResponse;
import com.carmenio.consensus.application.use_case.enrollment.CreateEnrollmentUseCase;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnrollmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateEnrollmentUseCase createEnrollmentUseCase;

    @MockitoBean
    private FindEnrollmentByIdUseCase findEnrollmentByIdUseCase;

    @MockitoBean
    private ListEnrollmentsByProcessUseCase listEnrollmentsByProcessUseCase;

    @Test
    @DisplayName("POST /api/private/processes/{processId}/enrollments should create enrollment")
    void shouldCreateEnrollment() throws Exception {
        var processId = UUID.randomUUID();
        var request = CreateEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .userId("user-123")
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .build();

        var response = EnrollmentResponse.builder()
                .id(UUID.randomUUID())
                .electoralProcessId(processId)
                .userId("user-123")
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .hasVoted(false)
                .build();

        when(createEnrollmentUseCase.execute(any())).thenReturn(response);

        mockMvc.perform(post("/api/private/processes/{processId}/enrollments", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("user-123"))
                .andExpect(jsonPath("$.data.electoralProcessId").value(processId.toString()))
                .andExpect(jsonPath("$.data.hasVoted").value(false));
    }

    @Test
    @DisplayName("POST should return 404 when process not found")
    void shouldReturn404WhenProcessNotFound() throws Exception {
        var processId = UUID.randomUUID();
        var request = CreateEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .userId("user-123")
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .build();

        when(createEnrollmentUseCase.execute(any()))
                .thenThrow(ElectoralProcessException.notFound(processId));

        mockMvc.perform(post("/api/private/processes/{processId}/enrollments", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST should return 409 when duplicate enrollment")
    void shouldReturn409WhenDuplicate() throws Exception {
        var processId = UUID.randomUUID();
        var request = CreateEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .userId("user-123")
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .build();

        when(createEnrollmentUseCase.execute(any()))
                .thenThrow(EnrollmentException.alreadyExists("userId"));

        mockMvc.perform(post("/api/private/processes/{processId}/enrollments", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/private/processes/{processId}/enrollments should list enrollments")
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

        mockMvc.perform(get("/api/private/processes/{processId}/enrollments", processId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].userId").value("user-123"));
    }

    @Test
    @DisplayName("GET /api/private/processes/{processId}/enrollments should return empty list")
    void shouldReturnEmptyList() throws Exception {
        var processId = UUID.randomUUID();

        when(listEnrollmentsByProcessUseCase.execute(processId)).thenReturn(List.of());

        mockMvc.perform(get("/api/private/processes/{processId}/enrollments", processId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("GET /api/private/enrollments/{id} should return enrollment")
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

        mockMvc.perform(get("/api/private/enrollments/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("found-user"));
    }

    @Test
    @DisplayName("GET /api/private/enrollments/{id} should return 404 when not found")
    void shouldReturn404WhenEnrollmentNotFound() throws Exception {
        var id = UUID.randomUUID();

        when(findEnrollmentByIdUseCase.execute(id)).thenThrow(EnrollmentException.notFound(id));

        mockMvc.perform(get("/api/private/enrollments/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
