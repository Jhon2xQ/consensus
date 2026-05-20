package com.carmenio.consensus.presentation.controller;

import com.carmenio.consensus.application.dto.electoral_process.ElectoralProcessResponse;
import com.carmenio.consensus.application.dto.electoral_process.PaginatedResponse;
import com.carmenio.consensus.application.dto.electoral_process.ProcessStateResponse;
import com.carmenio.consensus.application.use_case.electoral_process.FindElectoralProcessByIdUseCase;
import com.carmenio.consensus.application.use_case.electoral_process.GetProcessStateUseCase;
import com.carmenio.consensus.application.use_case.electoral_process.ListElectoralProcessesUseCase;
import com.carmenio.consensus.common.constant.ProcessStatus;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ElectoralProcessPublicController.class)
@AutoConfigureMockMvc(addFilters = false)
class ElectoralProcessPublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListElectoralProcessesUseCase listUseCase;

    @MockitoBean
    private FindElectoralProcessByIdUseCase findByIdUseCase;

    @MockitoBean
    private GetProcessStateUseCase getStateUseCase;

    @Test
    @DisplayName("GET /public/processes should return paginated list")
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

        mockMvc.perform(get("/public/processes")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Process A"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /public/processes/{id} should return process")
    void shouldFindProcessById() throws Exception {
        var id = UUID.randomUUID();
        var response = ElectoralProcessResponse.builder()
                .id(id)
                .name("Found Process")
                .scope("found-scope")
                .build();

        when(findByIdUseCase.execute(id)).thenReturn(response);

        mockMvc.perform(get("/public/processes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Found Process"));
    }

    @Test
    @DisplayName("GET /public/processes/{id} should return 404 when not found")
    void shouldReturn404WhenProcessNotFound() throws Exception {
        var id = UUID.randomUUID();
        when(findByIdUseCase.execute(id)).thenThrow(ElectoralProcessException.notFound(id));

        mockMvc.perform(get("/public/processes/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /public/processes/{id}/state should return current state")
    void shouldReturnProcessState() throws Exception {
        var id = UUID.randomUUID();
        var stateResponse = ProcessStateResponse.builder()
                .processId(id)
                .state(ProcessStatus.COMMITMENT)
                .build();

        when(getStateUseCase.execute(eq(id), any())).thenReturn(stateResponse);

        mockMvc.perform(get("/public/processes/{id}/state", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.state").value("COMMITMENT"));
    }
}
