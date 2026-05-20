package com.carmenio.consensus.presentation.controller;

import com.carmenio.consensus.application.dto.team.TeamResponse;
import com.carmenio.consensus.application.use_case.team.FindTeamByIdUseCase;
import com.carmenio.consensus.application.use_case.team.ListTeamsByProcessUseCase;
import com.carmenio.consensus.domain.exception.TeamException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TeamPublicController.class)
@AutoConfigureMockMvc(addFilters = false)
class TeamPublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListTeamsByProcessUseCase listTeamsUseCase;

    @MockitoBean
    private FindTeamByIdUseCase findTeamByIdUseCase;

    @Test
    @DisplayName("GET /public/processes/{processId}/teams should list teams")
    void shouldListTeams() throws Exception {
        var processId = UUID.randomUUID();
        var team = TeamResponse.builder()
                .id(UUID.randomUUID())
                .name("Team Alpha")
                .electoralProcessId(processId)
                .build();

        when(listTeamsUseCase.execute(processId)).thenReturn(List.of(team));

        mockMvc.perform(get("/public/processes/{processId}/teams", processId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Team Alpha"));
    }

    @Test
    @DisplayName("GET /public/teams/{id} should return team")
    void shouldFindTeamById() throws Exception {
        var id = UUID.randomUUID();
        var response = TeamResponse.builder()
                .id(id)
                .name("Found Team")
                .build();

        when(findTeamByIdUseCase.execute(id)).thenReturn(response);

        mockMvc.perform(get("/public/teams/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Found Team"));
    }

    @Test
    @DisplayName("GET /public/teams/{id} should return 404 when not found")
    void shouldReturn404WhenTeamNotFound() throws Exception {
        var id = UUID.randomUUID();

        when(findTeamByIdUseCase.execute(id)).thenThrow(TeamException.notFound(id));

        mockMvc.perform(get("/public/teams/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
