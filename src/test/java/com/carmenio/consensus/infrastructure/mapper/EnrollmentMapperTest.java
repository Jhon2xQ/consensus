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
 * Verifies entity ↔ DTO conversion mappings including the
 * email field for the two-phase enrollment flow.
 */
class EnrollmentMapperTest {

    private final EnrollmentMapper mapper = new EnrollmentMapper();

    @Test
    @DisplayName("Should map CreateEnrollmentRequest with email to Enrollment entity")
    void shouldMapRequestWithEmailToEntity() {
        var processId = UUID.randomUUID();
        var request = CreateEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .email("voter@example.com")
                .build();

        var entity = mapper.toEntity(request);

        assertNull(entity.getId(), "ID should be null for JPA generation");
        assertEquals(processId, entity.getElectoralProcessId());
        assertEquals("voter@example.com", entity.getEmail());
        assertNull(entity.getUserId(), "userId should be null in creator phase");
        assertNull(entity.getCommitment(), "commitment should be null in creator phase");
        assertFalse(entity.isHasVoted(), "New enrollments should not have voted");
    }

    @Test
    @DisplayName("Should map CreateEnrollmentRequest with all fields to entity")
    void shouldMapRequestWithAllFieldsToEntity() {
        var processId = UUID.randomUUID();
        var request = CreateEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .email("voter@example.com")
                .userId("user-123")
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .build();

        var entity = mapper.toEntity(request);

        assertEquals(processId, entity.getElectoralProcessId());
        assertEquals("voter@example.com", entity.getEmail());
        assertEquals("user-123", entity.getUserId());
        assertEquals("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111",
                entity.getCommitment());
    }

    @Test
    @DisplayName("Should map Enrollment entity to EnrollmentResponse with email")
    void shouldMapEntityToResponseWithEmail() {
        var id = UUID.randomUUID();
        var processId = UUID.randomUUID();
        var entity = Enrollment.builder()
                .id(id)
                .electoralProcessId(processId)
                .email("voter@example.com")
                .userId("user-456")
                .commitment("2222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222")
                .hasVoted(false)
                .build();

        var response = mapper.toResponse(entity);

        assertAll("response with email",
                () -> assertEquals(id, response.getId()),
                () -> assertEquals(processId, response.getElectoralProcessId()),
                () -> assertEquals("voter@example.com", response.getEmail()),
                () -> assertEquals("user-456", response.getUserId()),
                () -> assertEquals("2222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222",
                        response.getCommitment()),
                () -> assertFalse(response.isHasVoted())
        );
    }

    @Test
    @DisplayName("Should map entity with null userId and commitment to response")
    void shouldMapEntityWithNullUserIdAndCommitmentToResponse() {
        var id = UUID.randomUUID();
        var processId = UUID.randomUUID();
        var entity = Enrollment.builder()
                .id(id)
                .electoralProcessId(processId)
                .email("voter@example.com")
                .userId(null)
                .commitment(null)
                .hasVoted(false)
                .build();

        var response = mapper.toResponse(entity);

        assertAll("response with null userId/commitment",
                () -> assertEquals(id, response.getId()),
                () -> assertEquals("voter@example.com", response.getEmail()),
                () -> assertNull(response.getUserId()),
                () -> assertNull(response.getCommitment()),
                () -> assertFalse(response.isHasVoted())
        );
    }

    @Test
    @DisplayName("Should map entity with hasVoted=true to response")
    void shouldMapEntityWithHasVotedTrue() {
        var entity = Enrollment.builder()
                .id(UUID.randomUUID())
                .electoralProcessId(UUID.randomUUID())
                .email("voter@example.com")
                .userId("user-789")
                .commitment("3333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333")
                .hasVoted(true)
                .build();

        var response = mapper.toResponse(entity);

        assertTrue(response.isHasVoted());
    }

    @Test
    @DisplayName("Should create CreateEnrollmentRequest with email via builder")
    void shouldCreateCreateEnrollmentRequestWithEmail() {
        var processId = UUID.randomUUID();
        var request = CreateEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .email("voter@example.com")
                .userId("user-001")
                .commitment("4444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444")
                .build();

        assertNotNull(request);
        assertEquals(processId, request.getElectoralProcessId());
        assertEquals("voter@example.com", request.getEmail());
        assertEquals("user-001", request.getUserId());
        assertEquals("4444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444",
                request.getCommitment());
    }

    @Test
    @DisplayName("Should create EnrollmentResponse with email via builder")
    void shouldCreateEnrollmentResponseWithEmail() {
        var id = UUID.randomUUID();
        var processId = UUID.randomUUID();
        var response = EnrollmentResponse.builder()
                .id(id)
                .electoralProcessId(processId)
                .email("voter@example.com")
                .userId("user-002")
                .commitment("5555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555")
                .hasVoted(true)
                .build();

        assertAll("response with email",
                () -> assertNotNull(response),
                () -> assertEquals(id, response.getId()),
                () -> assertEquals(processId, response.getElectoralProcessId()),
                () -> assertEquals("voter@example.com", response.getEmail()),
                () -> assertEquals("user-002", response.getUserId()),
                () -> assertEquals("5555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555",
                        response.getCommitment()),
                () -> assertTrue(response.isHasVoted())
        );
    }
}
