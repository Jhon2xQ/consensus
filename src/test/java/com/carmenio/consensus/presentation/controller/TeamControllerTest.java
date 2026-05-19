package com.carmenio.consensus.presentation.controller;

import com.carmenio.consensus.application.dto.team.CreateTeamRequest;
import com.carmenio.consensus.application.dto.team.TeamResponse;
import com.carmenio.consensus.application.use_case.team.*;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.domain.exception.TeamException;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TeamController.class)
@AutoConfigureMockMvc(addFilters = false)
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateTeamUseCase createTeamUseCase;

    @MockitoBean
    private ListTeamsByProcessUseCase listTeamsUseCase;

    @MockitoBean
    private FindTeamByIdUseCase findTeamByIdUseCase;

    @MockitoBean
    private UpdateTeamUseCase updateTeamUseCase;

    @MockitoBean
    private DeleteTeamUseCase deleteTeamUseCase;

    @Test
    @DisplayName("POST /api/private/processes/{processId}/teams should create team")
    void shouldCreateTeam() throws Exception {
        var processId = UUID.randomUUID();
        var request = CreateTeamRequest.builder()
                .name("Team Alpha")
                .avatarUrl("https://avatar.example.com/alpha.png")
                .electoralProcessId(processId)
                .build();

        var response = TeamResponse.builder()
                .id(UUID.randomUUID())
                .name("Team Alpha")
                .avatarUrl("https://avatar.example.com/alpha.png")
                .electoralProcessId(processId)
                .build();

        when(createTeamUseCase.execute(any())).thenReturn(response);

        mockMvc.perform(post("/api/private/processes/{processId}/teams", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Team Alpha"))
                .andExpect(jsonPath("$.data.electoralProcessId").value(processId.toString()));
    }

    @Test
    @DisplayName("POST /api/private/processes/{processId}/teams should return 404 when process not found")
    void shouldReturn404WhenProcessNotFoundOnCreate() throws Exception {
        var processId = UUID.randomUUID();
        var request = CreateTeamRequest.builder()
                .name("Team Alpha")
                .electoralProcessId(processId)
                .build();

        when(createTeamUseCase.execute(any()))
                .thenThrow(ElectoralProcessException.notFound(processId));

        mockMvc.perform(post("/api/private/processes/{processId}/teams", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/private/processes/{processId}/teams should list teams")
    void shouldListTeams() throws Exception {
        var processId = UUID.randomUUID();
        var team = TeamResponse.builder()
                .id(UUID.randomUUID())
                .name("Team Alpha")
                .electoralProcessId(processId)
                .build();

        when(listTeamsUseCase.execute(processId)).thenReturn(List.of(team));

        mockMvc.perform(get("/api/private/processes/{processId}/teams", processId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Team Alpha"));
    }

    @Test
    @DisplayName("GET /api/private/teams/{id} should return team")
    void shouldFindTeamById() throws Exception {
        var id = UUID.randomUUID();
        var response = TeamResponse.builder()
                .id(id)
                .name("Found Team")
                .build();

        when(findTeamByIdUseCase.execute(id)).thenReturn(response);

        mockMvc.perform(get("/api/private/teams/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Found Team"));
    }

    @Test
    @DisplayName("GET /api/private/teams/{id} should return 404 when not found")
    void shouldReturn404WhenTeamNotFound() throws Exception {
        var id = UUID.randomUUID();

        when(findTeamByIdUseCase.execute(id)).thenThrow(TeamException.notFound(id));

        mockMvc.perform(get("/api/private/teams/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("PUT /api/private/teams/{id} should update team")
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

        mockMvc.perform(put("/api/private/teams/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Name"));
    }

    @Test
    @DisplayName("DELETE /api/private/teams/{id} should delete team")
    void shouldDeleteTeam() throws Exception {
        var id = UUID.randomUUID();
        doNothing().when(deleteTeamUseCase).execute(id);

        mockMvc.perform(delete("/api/private/teams/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
