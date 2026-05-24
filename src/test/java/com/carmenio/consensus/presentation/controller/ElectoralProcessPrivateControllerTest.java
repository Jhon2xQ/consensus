package com.carmenio.consensus.presentation.controller;

import com.carmenio.consensus.application.dto.electoral_process.CreateElectoralProcessRequest;
import com.carmenio.consensus.application.dto.electoral_process.ElectoralProcessResponse;
import com.carmenio.consensus.application.dto.electoral_process.UpdateElectoralProcessRequest;
import com.carmenio.consensus.application.use_case.electoral_process.CreateElectoralProcessUseCase;
import com.carmenio.consensus.application.use_case.electoral_process.DeleteElectoralProcessUseCase;
import com.carmenio.consensus.application.use_case.electoral_process.ListProcessesByCreatorUseCase;
import com.carmenio.consensus.application.use_case.electoral_process.UpdateElectoralProcessUseCase;
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

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ElectoralProcessPrivateController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class ElectoralProcessPrivateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateElectoralProcessUseCase createUseCase;

    @MockitoBean
    private UpdateElectoralProcessUseCase updateUseCase;

    @MockitoBean
    private DeleteElectoralProcessUseCase deleteUseCase;

    @MockitoBean
    private ListProcessesByCreatorUseCase listUseCase;

    @Test
    @DisplayName("POST /private/processes should return 200 with created process")
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

        when(createUseCase.execute(any(), any())).thenReturn(response);

        mockMvc.perform(post("/private/processes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test Process"));
    }

    @Test
    @DisplayName("PUT /private/processes/{id} should update process")
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

        mockMvc.perform(put("/private/processes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Name"));
    }

    @Test
    @DisplayName("DELETE /private/processes/{id} should delete process")
    void shouldDeleteProcess() throws Exception {
        var id = UUID.randomUUID();
        doNothing().when(deleteUseCase).execute(id);

        mockMvc.perform(delete("/private/processes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

}
