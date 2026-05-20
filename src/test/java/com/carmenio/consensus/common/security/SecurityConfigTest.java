package com.carmenio.consensus.common.security;

import com.carmenio.consensus.common.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TestSecurityController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("security-test")
@DisplayName("SecurityConfig — Route Matrix")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Mock JwtDecoder required by {@code @WebMvcTest} — Spring Boot auto-config
     * won't create the real decoder in a sliced test context because the JWKS URI
     * is not reachable. This mock satisfies the OAuth2 Resource Server dependency.
     */
    @MockitoBean
    private JwtDecoder jwtDecoder;

    // ──────────────────────────────────────────────
    // Public endpoints (no token required)
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("GET /public/processes should return 200 without token")
    void publicGetProcesses() throws Exception {
        mockMvc.perform(get("/public/processes"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /public/processes/{id} should return 200 without token")
    void publicGetProcessById() throws Exception {
        mockMvc.perform(get("/public/processes/{id}", UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /public/processes/{processId}/teams should return 200 without token")
    void publicGetTeams() throws Exception {
        mockMvc.perform(get("/public/processes/{processId}/teams", UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /public/teams/{id} should return 200 without token")
    void publicGetTeamById() throws Exception {
        mockMvc.perform(get("/public/teams/{id}", UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /public/processes/{id}/results should return 200 without token")
    void publicGetResults() throws Exception {
        mockMvc.perform(get("/public/processes/{id}/results", UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /public/processes/{id}/state should return 200 without token")
    void publicGetState() throws Exception {
        mockMvc.perform(get("/public/processes/{id}/state", UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    // ──────────────────────────────────────────────
    // Exempt endpoint (POST /public/records)
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("POST /public/records should return 200 without token (Semaphore exempt)")
    void exemptPostRecords() throws Exception {
        mockMvc.perform(post("/public/records")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isOk());
    }

    // ──────────────────────────────────────────────
    // Protected POST/PUT/DELETE (requires auth)
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("POST /private/processes should return 401 without token")
    void protectedPostProcessesWithoutToken() throws Exception {
        mockMvc.perform(post("/private/processes")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /private/processes should return 403 with user role")
    void protectedPostProcessesWithUserRole() throws Exception {
        mockMvc.perform(post("/private/processes")
                        .contentType("application/json")
                        .content("{}")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /private/processes should return 200 with creator role")
    void protectedPostProcessesWithCreatorRole() throws Exception {
        mockMvc.perform(post("/private/processes")
                        .contentType("application/json")
                        .content("{}")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_creator"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /private/processes/{id} should return 200 with creator role")
    void protectedPutProcessesWithCreatorRole() throws Exception {
        mockMvc.perform(put("/private/processes/{id}", UUID.randomUUID())
                        .contentType("application/json")
                        .content("{}")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_creator"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /private/processes/{id} should return 200 with creator role")
    void protectedDeleteProcessesWithCreatorRole() throws Exception {
        mockMvc.perform(delete("/private/processes/{id}", UUID.randomUUID())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_creator"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /private/processes/{processId}/teams should return 200 with creator role")
    void protectedPostTeamsWithCreatorRole() throws Exception {
        mockMvc.perform(post("/private/processes/{processId}/teams", UUID.randomUUID())
                        .contentType("application/json")
                        .content("{}")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_creator"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /private/teams/{id} should return 200 with creator role")
    void protectedPutTeamsWithCreatorRole() throws Exception {
        mockMvc.perform(put("/private/teams/{id}", UUID.randomUUID())
                        .contentType("application/json")
                        .content("{}")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_creator"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /private/teams/{id} should return 200 with creator role")
    void protectedDeleteTeamsWithCreatorRole() throws Exception {
        mockMvc.perform(delete("/private/teams/{id}", UUID.randomUUID())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_creator"))))
                .andExpect(status().isOk());
    }

    // ──────────────────────────────────────────────
    // Enrollment endpoints (requires user role)
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("POST /private/processes/{processId}/enrollments should return 401 without token")
    void protectedPostEnrollmentsWithoutToken() throws Exception {
        mockMvc.perform(post("/private/processes/{processId}/enrollments", UUID.randomUUID())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /private/processes/{processId}/enrollments should return 200 with user role")
    void protectedPostEnrollmentsWithUserRole() throws Exception {
        mockMvc.perform(post("/private/processes/{processId}/enrollments", UUID.randomUUID())
                        .contentType("application/json")
                        .content("{}")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /private/processes/{processId}/enrollments should return 403 with creator role")
    void protectedPostEnrollmentsWithCreatorRole() throws Exception {
        mockMvc.perform(post("/private/processes/{processId}/enrollments", UUID.randomUUID())
                        .contentType("application/json")
                        .content("{}")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_creator"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /private/processes/{processId}/enrollments should return 401 without token")
    void protectedGetEnrollmentsWithoutToken() throws Exception {
        mockMvc.perform(get("/private/processes/{processId}/enrollments", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /private/processes/{processId}/enrollments should return 200 with user role")
    void protectedGetEnrollmentsWithUserRole() throws Exception {
        mockMvc.perform(get("/private/processes/{processId}/enrollments", UUID.randomUUID())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /private/enrollments/{id} should return 401 without token")
    void protectedGetEnrollmentByIdWithoutToken() throws Exception {
        mockMvc.perform(get("/private/enrollments/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /private/enrollments/{id} should return 200 with user role")
    void protectedGetEnrollmentByIdWithUserRole() throws Exception {
        mockMvc.perform(get("/private/enrollments/{id}", UUID.randomUUID())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isOk());
    }
}
