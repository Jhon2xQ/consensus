package com.carmenio.consensus.presentation.controller;

import com.carmenio.consensus.application.dto.electoral_process.CreateElectoralProcessRequest;
import com.carmenio.consensus.application.dto.electoral_process.ElectoralProcessResponse;
import com.carmenio.consensus.application.dto.electoral_process.PaginatedResponse;
import com.carmenio.consensus.application.dto.electoral_process.ProcessStateResponse;
import com.carmenio.consensus.application.dto.electoral_process.UpdateElectoralProcessRequest;
import com.carmenio.consensus.application.use_case.electoral_process.*;
import com.carmenio.consensus.common.constant.ProcessStatus;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ElectoralProcessController.class)
class ElectoralProcessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateElectoralProcessUseCase createUseCase;

    @MockitoBean
    private ListElectoralProcessesUseCase listUseCase;

    @MockitoBean
    private FindElectoralProcessByIdUseCase findByIdUseCase;

    @MockitoBean
    private UpdateElectoralProcessUseCase updateUseCase;

    @MockitoBean
    private DeleteElectoralProcessUseCase deleteUseCase;

    @MockitoBean
    private GetProcessStateUseCase getStateUseCase;

    @Test
    @DisplayName("POST /api/private/processes should return 200 with created process")
    void shouldCreateProcess() throws Exception {
        var now = Instant.now();
        var request = CreateElectoralProcessRequest.builder()
                .name("Test Process")
                .scope("test-scope")
                .commitmentStart(now)
                .commitmentEnd(now.plusSeconds(3600))
                .votingStart(now.plusSeconds(7200))
                .votingEnd(now.plusSeconds(10800))
                .results(now.plusSeconds(14400))
                .build();

        var response = ElectoralProcessResponse.builder()
                .id(UUID.randomUUID())
                .name("Test Process")
                .scope("test-scope")
                .build();

        when(createUseCase.execute(any())).thenReturn(response);

        mockMvc.perform(post("/api/private/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test Process"));
    }

    @Test
    @DisplayName("GET /api/private/processes should return paginated list")
    void shouldListProcesses() throws Exception {
        var process = ElectoralProcessResponse.builder()
                .id(UUID.randomUUID())
                .name("Process A")
                .scope("scope-a")
                .build();

        var paginated = PaginatedResponse.<ElectoralProcessResponse>builder()
                .content(List.of(process))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .build();

        when(listUseCase.execute(any(PageRequest.class))).thenReturn(paginated);

        mockMvc.perform(get("/api/private/processes")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Process A"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/private/processes/{id} should return process")
    void shouldFindProcessById() throws Exception {
        var id = UUID.randomUUID();
        var response = ElectoralProcessResponse.builder()
                .id(id)
                .name("Found Process")
                .scope("found-scope")
                .build();

        when(findByIdUseCase.execute(id)).thenReturn(response);

        mockMvc.perform(get("/api/private/processes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Found Process"));
    }

    @Test
    @DisplayName("GET /api/private/processes/{id} should return 404 when not found")
    void shouldReturn404WhenProcessNotFound() throws Exception {
        var id = UUID.randomUUID();
        when(findByIdUseCase.execute(id)).thenThrow(ElectoralProcessException.notFound(id));

        mockMvc.perform(get("/api/private/processes/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("PUT /api/private/processes/{id} should update process")
    void shouldUpdateProcess() throws Exception {
        var id = UUID.randomUUID();
        var request = UpdateElectoralProcessRequest.builder()
                .name("Updated Name")
                .build();

        var response = ElectoralProcessResponse.builder()
                .id(id)
                .name("Updated Name")
                .scope("original-scope")
                .build();

        when(updateUseCase.execute(eq(id), any())).thenReturn(response);

        mockMvc.perform(put("/api/private/processes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Name"));
    }

    @Test
    @DisplayName("DELETE /api/private/processes/{id} should delete process")
    void shouldDeleteProcess() throws Exception {
        var id = UUID.randomUUID();
        doNothing().when(deleteUseCase).execute(id);

        mockMvc.perform(delete("/api/private/processes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /api/private/processes/{id} should return 409 when has dependencies")
    void shouldReturn409WhenProcessHasDependencies() throws Exception {
        var id = UUID.randomUUID();
        doThrow(ElectoralProcessException.hasDependencies())
                .when(deleteUseCase).execute(id);

        mockMvc.perform(delete("/api/private/processes/{id}", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/private/processes/{id}/state should return current state")
    void shouldReturnProcessState() throws Exception {
        var id = UUID.randomUUID();
        var stateResponse = ProcessStateResponse.builder()
                .processId(id)
                .state(ProcessStatus.COMMITMENT)
                .build();

        when(getStateUseCase.execute(eq(id), any())).thenReturn(stateResponse);

        mockMvc.perform(get("/api/private/processes/{id}/state", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.state").value("COMMITMENT"));
    }

    @Test
    @DisplayName("GET /api/private/processes/{id}/state should return PAUSED when process is paused")
    void shouldReturnPausedState() throws Exception {
        var id = UUID.randomUUID();
        var stateResponse = ProcessStateResponse.builder()
                .processId(id)
                .state(ProcessStatus.PAUSED)
                .build();

        when(getStateUseCase.execute(eq(id), any())).thenReturn(stateResponse);

        mockMvc.perform(get("/api/private/processes/{id}/state", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.state").value("PAUSED"));
    }

    @Test
    @DisplayName("GET /api/private/processes/{id}/state should return CANCELLED when process is cancelled")
    void shouldReturnCancelledState() throws Exception {
        var id = UUID.randomUUID();
        var stateResponse = ProcessStateResponse.builder()
                .processId(id)
                .state(ProcessStatus.CANCELLED)
                .build();

        when(getStateUseCase.execute(eq(id), any())).thenReturn(stateResponse);

        mockMvc.perform(get("/api/private/processes/{id}/state", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.state").value("CANCELLED"));
    }
}
