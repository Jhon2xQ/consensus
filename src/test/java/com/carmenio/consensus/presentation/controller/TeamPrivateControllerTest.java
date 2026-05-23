package com.carmenio.consensus.presentation.controller;

import com.carmenio.consensus.application.dto.team.CreateTeamRequest;
import com.carmenio.consensus.application.dto.team.TeamResponse;
import com.carmenio.consensus.application.use_case.team.CreateTeamUseCase;
import com.carmenio.consensus.application.use_case.team.DeleteTeamUseCase;
import com.carmenio.consensus.application.use_case.team.UpdateTeamUseCase;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
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
    private CreateTeamUseCase createTeamUseCase;

    @MockitoBean
    private UpdateTeamUseCase updateTeamUseCase;

    @MockitoBean
    private DeleteTeamUseCase deleteTeamUseCase;

    @Test
    @DisplayName("POST /private/processes/{processId}/teams should create team")
    void shouldCreateTeam() throws Exception {
        var processId = UUID.randomUUID();
        var request = CreateTeamRequest.builder()
                .name("Team Alpha")
                .avatarUrl("https://avatar.example.com/alpha.png")
                .build();

        var response = TeamResponse.builder()
                .id(UUID.randomUUID())
                .name("Team Alpha")
                .avatarUrl("https://avatar.example.com/alpha.png")
                .electoralProcessId(processId)
                .build();

        when(createTeamUseCase.execute(any(UUID.class), any(CreateTeamRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/private/processes/{processId}/teams", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Team Alpha"))
                .andExpect(jsonPath("$.data.electoralProcessId").value(processId.toString()));
    }

    @Test
    @DisplayName("POST /private/processes/{processId}/teams should return 404 when process not found")
    void shouldReturn404WhenProcessNotFoundOnCreate() throws Exception {
        var processId = UUID.randomUUID();
        var request = CreateTeamRequest.builder()
                .name("Team Alpha")
                .build();

        when(createTeamUseCase.execute(any(UUID.class), any(CreateTeamRequest.class)))
                .thenThrow(ElectoralProcessException.notFound(processId));

        mockMvc.perform(post("/private/processes/{processId}/teams", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

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
