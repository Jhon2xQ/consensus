package com.carmenio.consensus.presentation.controller;

import com.carmenio.consensus.application.dto.team.CreateTeamRequest;
import com.carmenio.consensus.application.dto.team.TeamResponse;
import com.carmenio.consensus.application.use_case.team.CreateTeamsBatchUseCase;
import com.carmenio.consensus.application.use_case.team.DeleteTeamUseCase;
import com.carmenio.consensus.application.use_case.team.UpdateTeamUseCase;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.domain.exception.TeamException;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TeamPrivateController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class TeamPrivateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateTeamsBatchUseCase createTeamsBatchUseCase;

    @MockitoBean
    private UpdateTeamUseCase updateTeamUseCase;

    @MockitoBean
    private DeleteTeamUseCase deleteTeamUseCase;

    // ── POST /private/processes/{processId}/teams (batch) ──

    @Test
    @DisplayName("POST /private/processes/{processId}/teams should create single team in batch")
    void shouldCreateSingleTeamInBatch() throws Exception {
        var processId = UUID.randomUUID();
        var request = List.of(CreateTeamRequest.builder()
                .name("Team Alpha")
                .avatarUrl("https://avatar.example.com/alpha.png")
                .build());

        var response = List.of(TeamResponse.builder()
                .id(UUID.randomUUID())
                .name("Team Alpha")
                .avatarUrl("https://avatar.example.com/alpha.png")
                .electoralProcessId(processId)
                .build());

        when(createTeamsBatchUseCase.execute(any(UUID.class), any()))
                .thenReturn(response);

        mockMvc.perform(post("/private/processes/{processId}/teams", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Team Alpha"))
                .andExpect(jsonPath("$.data[0].electoralProcessId").value(processId.toString()));
    }

    @Test
    @DisplayName("POST /private/processes/{processId}/teams should create multiple teams in batch")
    void shouldCreateMultipleTeamsInBatch() throws Exception {
        var processId = UUID.randomUUID();
        var requests = List.of(
                CreateTeamRequest.builder().name("Team Alpha").build(),
                CreateTeamRequest.builder().name("Team Beta").build(),
                CreateTeamRequest.builder().name("Team Gamma").build()
        );

        var responses = List.of(
                TeamResponse.builder().id(UUID.randomUUID()).name("Team Alpha").electoralProcessId(processId).build(),
                TeamResponse.builder().id(UUID.randomUUID()).name("Team Beta").electoralProcessId(processId).build(),
                TeamResponse.builder().id(UUID.randomUUID()).name("Team Gamma").electoralProcessId(processId).build()
        );

        when(createTeamsBatchUseCase.execute(any(UUID.class), any()))
                .thenReturn(responses);

        mockMvc.perform(post("/private/processes/{processId}/teams", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].name").value("Team Alpha"))
                .andExpect(jsonPath("$.data[1].name").value("Team Beta"))
                .andExpect(jsonPath("$.data[2].name").value("Team Gamma"));
    }

    @Test
    @DisplayName("POST /private/processes/{processId}/teams should return 400 for empty array")
    void shouldReturn400WhenEmptyArray() throws Exception {
        var processId = UUID.randomUUID();

        when(createTeamsBatchUseCase.execute(any(UUID.class), any()))
                .thenThrow(TeamException.emptyBatch());

        mockMvc.perform(post("/private/processes/{processId}/teams", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /private/processes/{processId}/teams should return 400 when name is blank")
    void shouldReturn400WhenNameIsBlank() throws Exception {
        var processId = UUID.randomUUID();
        // Simulate Spring validation rejection for @NotBlank on name
        // Note: with @Valid on List, Spring MVC validates each element
        var body = "[{\"name\":\"\",\"avatarUrl\":null}]";

        // Spring MVC validation rejects before the use case is even called
        mockMvc.perform(post("/private/processes/{processId}/teams", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /private/processes/{processId}/teams should return 409 on within-batch duplicate")
    void shouldReturn409WhenWithinBatchDuplicate() throws Exception {
        var processId = UUID.randomUUID();

        when(createTeamsBatchUseCase.execute(any(UUID.class), any()))
                .thenThrow(TeamException.duplicateInBatch("Team Alpha"));

        var body = "[{\"name\":\"Team Alpha\"},{\"name\":\"Team Alpha\"}]";

        mockMvc.perform(post("/private/processes/{processId}/teams", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /private/processes/{processId}/teams should return 404 when process not found")
    void shouldReturn404WhenProcessNotFound() throws Exception {
        var processId = UUID.randomUUID();
        var request = List.of(CreateTeamRequest.builder()
                .name("Team Alpha")
                .build());

        when(createTeamsBatchUseCase.execute(any(UUID.class), any()))
                .thenThrow(ElectoralProcessException.notFound(processId));

        mockMvc.perform(post("/private/processes/{processId}/teams", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /private/processes/{processId}/teams should return 409 on cross-batch DB conflict")
    void shouldReturn409WhenCrossBatchDbConflict() throws Exception {
        var processId = UUID.randomUUID();
        var request = List.of(CreateTeamRequest.builder()
                .name("Existing Team")
                .build());

        when(createTeamsBatchUseCase.execute(any(UUID.class), any()))
                .thenThrow(TeamException.alreadyExists("Existing Team"));

        mockMvc.perform(post("/private/processes/{processId}/teams", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── PUT /private/teams/{id} ──

    @Test
    @DisplayName("PUT /private/teams/{id} should update team")
    void shouldUpdateTeam() throws Exception {
        var id = UUID.randomUUID();
        var processId = UUID.randomUUID();

        var response = TeamResponse.builder()
                .id(id)
                .name("Updated Name")
                .electoralProcessId(processId)
                .build();

        when(updateTeamUseCase.execute(eq(id), eq("Updated Name"), any()))
                .thenReturn(response);

        var body = "{\"name\":\"Updated Name\"}";

        mockMvc.perform(put("/private/teams/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Name"));
    }

    // ── DELETE /private/teams/{id} ──

    @Test
    @DisplayName("DELETE /private/teams/{id} should delete team")
    void shouldDeleteTeam() throws Exception {
        var id = UUID.randomUUID();
        doNothing().when(deleteTeamUseCase).execute(id);

        mockMvc.perform(delete("/private/teams/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
