package com.carmenio.consensus.presentation.controller;

import com.carmenio.consensus.application.dto.PaginatedResponse;
import com.carmenio.consensus.application.dto.record.CreateVoteRecordRequest;
import com.carmenio.consensus.application.dto.record.VoteRecordResponse;
import com.carmenio.consensus.application.use_case.record.CreateVoteRecordUseCase;
import com.carmenio.consensus.application.use_case.record.ListVoteRecordsUseCase;
import com.carmenio.consensus.common.config.SecurityConfig;
import com.carmenio.consensus.domain.exception.RecordException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecordController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("RecordController")
class RecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateVoteRecordUseCase createVoteRecordUseCase;

    @MockitoBean
    private ListVoteRecordsUseCase listVoteRecordsUseCase;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    // ── POST tests (Semaphore Relayer — exempt from auth) ──

    @Test
    @DisplayName("POST /private/records should create vote record")
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

        mockMvc.perform(post("/private/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nullifier").value("nullifier-1"))
                .andExpect(jsonPath("$.data.message").value("Team Alpha"))
                .andExpect(jsonPath("$.data.scope").value("scope-123"));
    }

    @Test
    @DisplayName("POST /private/records should return 400 when scope invalid")
    void shouldReturn400WhenScopeInvalid() throws Exception {
        var request = CreateVoteRecordRequest.builder()
                .groupId("1")
                .nullifier("nullifier-1")
                .message("Team Alpha")
                .scope("invalid-scope")
                .build();

        when(createVoteRecordUseCase.execute(any()))
                .thenThrow(RecordException.invalidScope());

        mockMvc.perform(post("/private/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── GET functional tests ──

    @Test
    @DisplayName("should return paginated records when no scope")
    void shouldReturnPaginatedRecordsWhenNoScope() throws Exception {
        var now = Instant.now();
        var response = PaginatedResponse.<VoteRecordResponse>builder()
                .content(List.of(
                        VoteRecordResponse.builder()
                                .id(UUID.randomUUID())
                                .groupId("1")
                                .nullifier("nullifier-1")
                                .message("Team Alpha")
                                .scope("scope-1")
                                .transactionHash("0xabc")
                                .createdAt(now)
                                .build(),
                        VoteRecordResponse.builder()
                                .id(UUID.randomUUID())
                                .groupId("1")
                                .nullifier("nullifier-2")
                                .message("Team Beta")
                                .scope("scope-1")
                                .transactionHash("0xdef")
                                .createdAt(now)
                                .build()
                ))
                .page(0)
                .size(20)
                .totalElements(2)
                .totalPages(1)
                .build();

        when(listVoteRecordsUseCase.executePaginated(any())).thenReturn(response);

        mockMvc.perform(get("/private/records")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_consensus-creator"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].message").value("Team Alpha"))
                .andExpect(jsonPath("$.data.content[1].message").value("Team Beta"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalPages").value(1));
    }

    @Test
    @DisplayName("should return all records when scope provided")
    void shouldReturnAllRecordsWhenScopeProvided() throws Exception {
        var now = Instant.now();
        var scope = "scope-1";
        var records = List.of(
                VoteRecordResponse.builder()
                        .id(UUID.randomUUID())
                        .groupId("1")
                        .nullifier("nullifier-1")
                        .message("Team Alpha")
                        .scope(scope)
                        .transactionHash("0xabc")
                        .createdAt(now)
                        .build()
        );

        when(listVoteRecordsUseCase.executeByScope(eq(scope))).thenReturn(records);

        mockMvc.perform(get("/private/records")
                        .param("scope", scope)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_consensus-creator"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].message").value("Team Alpha"))
                .andExpect(jsonPath("$.data[0].scope").value(scope))
                // Verify no pagination metadata in response
                .andExpect(jsonPath("$.data.page").doesNotExist());
    }

    @Test
    @DisplayName("should return empty list for unknown scope")
    void shouldReturnEmptyListForUnknownScope() throws Exception {
        var unknownScope = "unknown-scope";

        when(listVoteRecordsUseCase.executeByScope(eq(unknownScope))).thenReturn(List.of());

        mockMvc.perform(get("/private/records")
                        .param("scope", unknownScope)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_consensus-creator"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // ── GET security tests ──

    @Test
    @DisplayName("should return 401 when no token on GET")
    void shouldReturn401WhenNoTokenOnGet() throws Exception {
        mockMvc.perform(get("/private/records"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should allow any authenticated role on GET")
    void shouldAllowAnyAuthenticatedRoleOnGet() throws Exception {
        var response = PaginatedResponse.<VoteRecordResponse>builder()
                .content(List.of())
                .page(0)
                .size(20)
                .totalElements(0)
                .totalPages(0)
                .build();

        when(listVoteRecordsUseCase.executePaginated(any())).thenReturn(response);

        // consensus-creator should have access
        mockMvc.perform(get("/private/records")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_consensus-creator"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // consensus-user should have access
        mockMvc.perform(get("/private/records")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_consensus-user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
