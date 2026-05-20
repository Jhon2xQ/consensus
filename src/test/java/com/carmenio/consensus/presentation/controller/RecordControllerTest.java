package com.carmenio.consensus.presentation.controller;

import com.carmenio.consensus.application.dto.record.CreateVoteRecordRequest;
import com.carmenio.consensus.application.dto.record.ProcessResultsResponse;
import com.carmenio.consensus.application.dto.record.TeamResult;
import com.carmenio.consensus.application.dto.record.VoteRecordResponse;
import com.carmenio.consensus.application.use_case.record.CreateVoteRecordUseCase;
import com.carmenio.consensus.application.use_case.record.GetProcessResultsUseCase;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.domain.exception.RecordException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecordController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateVoteRecordUseCase createVoteRecordUseCase;

    @MockitoBean
    private GetProcessResultsUseCase getProcessResultsUseCase;

    @Test
    @DisplayName("POST /public/records should create vote record")
    void shouldCreateVoteRecord() throws Exception {
        var request = CreateVoteRecordRequest.builder()
                .groupId("1")
                .nullifier("nullifier-1")
                .message("Team Alpha")
                .scope("scope-123")
                .transactionHash("0xabc")
                .build();

        var response = VoteRecordResponse.builder()
                .id(UUID.randomUUID())
                .groupId("1")
                .nullifier("nullifier-1")
                .message("Team Alpha")
                .scope("scope-123")
                .transactionHash("0xabc")
                .createdAt(Instant.now())
                .build();

        when(createVoteRecordUseCase.execute(any())).thenReturn(response);

        mockMvc.perform(post("/public/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nullifier").value("nullifier-1"))
                .andExpect(jsonPath("$.data.message").value("Team Alpha"))
                .andExpect(jsonPath("$.data.scope").value("scope-123"));
    }

    @Test
    @DisplayName("POST /public/records should return 400 when scope invalid")
    void shouldReturn400WhenScopeInvalid() throws Exception {
        var request = CreateVoteRecordRequest.builder()
                .groupId("1")
                .nullifier("nullifier-1")
                .message("Team Alpha")
                .scope("invalid-scope")
                .build();

        when(createVoteRecordUseCase.execute(any()))
                .thenThrow(RecordException.invalidScope());

        mockMvc.perform(post("/public/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /public/processes/{id}/results should return results")
    void shouldReturnResults() throws Exception {
        var processId = UUID.randomUUID();
        var teamResults = List.of(
                TeamResult.builder().teamName("Team A").voteCount(2).build(),
                TeamResult.builder().teamName("Team B").voteCount(1).build()
        );

        var response = ProcessResultsResponse.builder()
                .processId(processId)
                .processName("Test Process")
                .teamResults(teamResults)
                .totalVotes(3)
                .status("CLOSED")
                .build();

        when(getProcessResultsUseCase.execute(processId)).thenReturn(response);

        mockMvc.perform(get("/public/processes/{id}/results", processId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.processId").value(processId.toString()))
                .andExpect(jsonPath("$.data.totalVotes").value(3))
                .andExpect(jsonPath("$.data.status").value("CLOSED"))
                .andExpect(jsonPath("$.data.teamResults[0].teamName").value("Team A"))
                .andExpect(jsonPath("$.data.teamResults[0].voteCount").value(2));
    }

    @Test
    @DisplayName("GET /public/processes/{id}/results should return 400 when process not closed")
    void shouldReturn400WhenProcessNotClosed() throws Exception {
        var processId = UUID.randomUUID();

        when(getProcessResultsUseCase.execute(processId))
                .thenThrow(ElectoralProcessException.invalidState("Results only available when process is closed"));

        mockMvc.perform(get("/public/processes/{id}/results", processId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
