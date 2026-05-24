package com.carmenio.consensus.presentation.controller;

import com.carmenio.consensus.application.dto.PaginatedResponse;
import com.carmenio.consensus.application.dto.electoral_process.ElectoralProcessResponse;
import com.carmenio.consensus.application.use_case.electoral_process.CreateElectoralProcessUseCase;
import com.carmenio.consensus.application.use_case.electoral_process.DeleteElectoralProcessUseCase;
import com.carmenio.consensus.application.use_case.electoral_process.FindElectoralProcessByIdUseCase;
import com.carmenio.consensus.application.use_case.electoral_process.ListProcessesByCreatorUseCase;
import com.carmenio.consensus.application.use_case.electoral_process.UpdateElectoralProcessUseCase;
import com.carmenio.consensus.common.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ElectoralProcessPrivateController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("ElectoralProcessPrivateController — Security")
class ElectoralProcessPrivateControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private CreateElectoralProcessUseCase createUseCase;

    @MockitoBean
    private UpdateElectoralProcessUseCase updateUseCase;

    @MockitoBean
    private DeleteElectoralProcessUseCase deleteUseCase;

    @MockitoBean
    private FindElectoralProcessByIdUseCase findByIdUseCase;

    @MockitoBean
    private ListProcessesByCreatorUseCase listUseCase;

    @Test
    @DisplayName("should return processes for authenticated creator")
    void shouldReturnProcessesForAuthenticatedCreator() throws Exception {
        var response = PaginatedResponse.<ElectoralProcessResponse>builder()
                .content(List.of(ElectoralProcessResponse.builder()
                        .id(UUID.randomUUID())
                        .name("My Process")
                        .scope("my-scope")
                        .createdBy("user-1")
                        .build()))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .build();

        when(listUseCase.execute(any(Jwt.class), any())).thenReturn(response);

        mockMvc.perform(get("/private/processes")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_consensus-creator"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].createdBy").value("user-1"));
    }

    @Test
    @DisplayName("should return 401 when no token")
    void shouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get("/private/processes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should return 403 when wrong role")
    void shouldReturn403WhenWrongRole() throws Exception {
        mockMvc.perform(get("/private/processes")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_consensus-user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should return process by ID for consensus-creator")
    void shouldReturnProcessByIdForCreator() throws Exception {
        var id = UUID.randomUUID();
        var response = ElectoralProcessResponse.builder()
                .id(id)
                .name("Private Process")
                .scope("private-scope")
                .createdBy("user-1")
                .build();

        when(findByIdUseCase.execute(id)).thenReturn(response);

        mockMvc.perform(get("/private/processes/{id}", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_consensus-creator"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Private Process"));
    }

    @Test
    @DisplayName("should return 403 for GET /private/processes/{id} with wrong role")
    void shouldReturn403ForGetByIdWithWrongRole() throws Exception {
        mockMvc.perform(get("/private/processes/{id}", UUID.randomUUID())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_consensus-user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should return default pagination")
    void shouldReturnDefaultPagination() throws Exception {
        var response = PaginatedResponse.<ElectoralProcessResponse>builder()
                .content(List.of())
                .page(0)
                .size(20)
                .totalElements(0)
                .totalPages(0)
                .build();

        when(listUseCase.execute(any(Jwt.class), any())).thenReturn(response);

        mockMvc.perform(get("/private/processes")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_consensus-creator"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }
}
