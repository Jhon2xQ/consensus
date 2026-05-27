package com.carmenio.consensus.presentation.controller;

import com.carmenio.consensus.application.dto.enrollment.EnrollmentStatsResponse;
import com.carmenio.consensus.application.use_case.enrollment.GetEnrollmentStatsUseCase;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnrollmentPublicController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class EnrollmentPublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetEnrollmentStatsUseCase getEnrollmentStatsUseCase;

    @Test
    @DisplayName("GET /public/processes/{processId}/enrollments should return stats when process exists")
    void shouldReturnStatsWhenProcessExists() throws Exception {
        var processId = UUID.randomUUID();

        var stats = EnrollmentStatsResponse.builder()
                .totalParticipants(100)
                .totalCommitments(60)
                .totalVoted(40)
                .build();

        when(getEnrollmentStatsUseCase.execute(processId)).thenReturn(stats);

        mockMvc.perform(get("/public/processes/{processId}/enrollments", processId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalParticipants").value(100))
                .andExpect(jsonPath("$.data.totalCommitments").value(60))
                .andExpect(jsonPath("$.data.totalVoted").value(40));
    }

    @Test
    @DisplayName("GET /public/processes/{processId}/enrollments should return 404 when process not found")
    void shouldReturn404WhenProcessNotFound() throws Exception {
        var processId = UUID.randomUUID();

        when(getEnrollmentStatsUseCase.execute(processId))
                .thenThrow(ElectoralProcessException.notFound(processId));

        mockMvc.perform(get("/public/processes/{processId}/enrollments", processId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /public/processes/{processId}/enrollments should return zero stats when no enrollments exist")
    void shouldReturnZeroStatsWhenNoEnrollments() throws Exception {
        var processId = UUID.randomUUID();

        var stats = EnrollmentStatsResponse.builder()
                .totalParticipants(0)
                .totalCommitments(0)
                .totalVoted(0)
                .build();

        when(getEnrollmentStatsUseCase.execute(processId)).thenReturn(stats);

        mockMvc.perform(get("/public/processes/{processId}/enrollments", processId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalParticipants").value(0))
                .andExpect(jsonPath("$.data.totalCommitments").value(0))
                .andExpect(jsonPath("$.data.totalVoted").value(0));
    }
}
