package com.carmenio.consensus.infrastructure.mapper;

import com.carmenio.consensus.application.dto.enrollment.CreateEnrollmentRequest;
import com.carmenio.consensus.application.dto.enrollment.EnrollmentResponse;
import com.carmenio.consensus.domain.entity.Enrollment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link EnrollmentMapper}.
 * <p>
 * Verifies entity ↔ DTO conversion mappings.
 */
class EnrollmentMapperTest {

    private final EnrollmentMapper mapper = new EnrollmentMapper();

    @Test
    @DisplayName("Should map CreateEnrollmentRequest to Enrollment entity")
    void shouldMapRequestToEntity() {
        var processId = UUID.randomUUID();
        var request = CreateEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .userId("user-123")
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .build();

        var entity = mapper.toEntity(request);

        assertNull(entity.getId(), "ID should be null for JPA generation");
        assertEquals(processId, entity.getElectoralProcessId());
        assertEquals("user-123", entity.getUserId());
        assertEquals("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111", entity.getCommitment());
        assertFalse(entity.isHasVoted(), "New enrollments should not have voted");
    }

    @Test
    @DisplayName("Should map Enrollment entity to EnrollmentResponse")
    void shouldMapEntityToResponse() {
        var id = UUID.randomUUID();
        var processId = UUID.randomUUID();
        var entity = Enrollment.builder()
                .id(id)
                .electoralProcessId(processId)
                .userId("user-456")
                .commitment("2222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222")
                .hasVoted(false)
                .build();

        var response = mapper.toResponse(entity);

        assertEquals(id, response.getId());
        assertEquals(processId, response.getElectoralProcessId());
        assertEquals("user-456", response.getUserId());
        assertEquals("2222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222", response.getCommitment());
        assertFalse(response.isHasVoted());
    }

    @Test
    @DisplayName("Should map entity with hasVoted=true to response")
    void shouldMapEntityWithHasVotedTrue() {
        var entity = Enrollment.builder()
                .id(UUID.randomUUID())
                .electoralProcessId(UUID.randomUUID())
                .userId("user-789")
                .commitment("3333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333")
                .hasVoted(true)
                .build();

        var response = mapper.toResponse(entity);

        assertTrue(response.isHasVoted());
    }

    @Test
    @DisplayName("Should create CreateEnrollmentRequest with all fields via builder")
    void shouldCreateCreateEnrollmentRequest() {
        var processId = UUID.randomUUID();
        var request = CreateEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .userId("user-001")
                .commitment("4444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444")
                .build();

        assertNotNull(request);
        assertEquals(processId, request.getElectoralProcessId());
        assertEquals("user-001", request.getUserId());
        assertEquals("4444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444", request.getCommitment());
    }

    @Test
    @DisplayName("Should create EnrollmentResponse with all fields via builder")
    void shouldCreateEnrollmentResponse() {
        var id = UUID.randomUUID();
        var processId = UUID.randomUUID();
        var response = EnrollmentResponse.builder()
                .id(id)
                .electoralProcessId(processId)
                .userId("user-002")
                .commitment("5555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555")
                .hasVoted(true)
                .build();

        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals(processId, response.getElectoralProcessId());
        assertEquals("user-002", response.getUserId());
        assertEquals("5555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555", response.getCommitment());
        assertTrue(response.isHasVoted());
    }
}
