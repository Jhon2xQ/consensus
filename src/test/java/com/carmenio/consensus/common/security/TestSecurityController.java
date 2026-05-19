package com.carmenio.consensus.common.security;

import com.carmenio.consensus.presentation.middleware.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Minimal test controller providing endpoints for all route patterns
 * that the SecurityConfig protects. Used only in SecurityConfigTest.
 */
@RestController
@Profile("security-test")
public class TestSecurityController {

    @GetMapping("/api/private/processes")
    public ResponseEntity<ApiResponse<String>> listProcesses() {
        return ResponseEntity.ok(ApiResponse.success("ok"));
    }

    @GetMapping("/api/private/processes/{id}")
    public ResponseEntity<ApiResponse<String>> getProcess(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("ok"));
    }

    @GetMapping("/api/private/processes/{id}/state")
    public ResponseEntity<ApiResponse<String>> getProcessState(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("ok"));
    }

    @GetMapping("/api/private/processes/{id}/results")
    public ResponseEntity<ApiResponse<String>> getResults(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("ok"));
    }

    @GetMapping("/api/private/processes/{processId}/teams")
    public ResponseEntity<ApiResponse<String>> listTeams(@PathVariable UUID processId) {
        return ResponseEntity.ok(ApiResponse.success("ok"));
    }

    @GetMapping("/api/private/teams/{id}")
    public ResponseEntity<ApiResponse<String>> getTeam(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("ok"));
    }

    @PostMapping("/api/private/processes")
    public ResponseEntity<ApiResponse<String>> createProcess() {
        return ResponseEntity.ok(ApiResponse.success("ok"));
    }

    @PutMapping("/api/private/processes/{id}")
    public ResponseEntity<ApiResponse<String>> updateProcess(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("ok"));
    }

    @DeleteMapping("/api/private/processes/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProcess(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("ok"));
    }

    @PostMapping("/api/private/processes/{processId}/teams")
    public ResponseEntity<ApiResponse<String>> createTeam(@PathVariable UUID processId) {
        return ResponseEntity.ok(ApiResponse.success("ok"));
    }

    @PutMapping("/api/private/teams/{id}")
    public ResponseEntity<ApiResponse<String>> updateTeam(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("ok"));
    }

    @DeleteMapping("/api/private/teams/{id}")
    public ResponseEntity<ApiResponse<String>> deleteTeam(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("ok"));
    }

    @PostMapping("/api/private/records")
    public ResponseEntity<ApiResponse<String>> createRecord() {
        return ResponseEntity.ok(ApiResponse.success("ok"));
    }

    @PostMapping("/api/private/processes/{processId}/enrollments")
    public ResponseEntity<ApiResponse<String>> createEnrollment(@PathVariable UUID processId) {
        return ResponseEntity.ok(ApiResponse.success("ok"));
    }

    @GetMapping("/api/private/processes/{processId}/enrollments")
    public ResponseEntity<ApiResponse<String>> listEnrollments(@PathVariable UUID processId) {
        return ResponseEntity.ok(ApiResponse.success("ok"));
    }

    @GetMapping("/api/private/enrollments/{id}")
    public ResponseEntity<ApiResponse<String>> getEnrollment(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("ok"));
    }
}
