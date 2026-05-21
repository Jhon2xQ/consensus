package com.carmenio.consensus.application.dto;

import com.carmenio.consensus.application.dto.electoral_process.CreateElectoralProcessRequest;
import com.carmenio.consensus.application.dto.electoral_process.ElectoralProcessResponse;
import com.carmenio.consensus.application.dto.PaginatedResponse;
import com.carmenio.consensus.application.dto.electoral_process.ProcessStateResponse;
import com.carmenio.consensus.application.dto.electoral_process.UpdateElectoralProcessRequest;
import com.carmenio.consensus.common.constant.ProcessStatus;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for request/response DTOs.
 * <p>
 * Verifies construction, field access, and Bean Validation constraints.
 */
class ElectoralProcessDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    // ---- CreateElectoralProcessRequest ----

    @Test
    @DisplayName("should create CreateElectoralProcessRequest with all fields")
    void shouldCreateCreateRequestWithAllFields() {
        var now = Instant.now();
        var request = new CreateElectoralProcessRequest();
        request.setName("Presidential Election");
        request.setScope("presidential-2026");
        request.setCommitmentStart(now);
        request.setCommitmentEnd(now.plusSeconds(3600));
        request.setVotingStart(now.plusSeconds(7200));
        request.setVotingEnd(now.plusSeconds(10800));
        request.setResults(now.plusSeconds(14400));

        assertAll("create request",
                () -> assertEquals("Presidential Election", request.getName()),
                () -> assertEquals("presidential-2026", request.getScope()),
                () -> assertNotNull(request.getCommitmentStart()),
                () -> assertNotNull(request.getCommitmentEnd()),
                () -> assertNotNull(request.getVotingStart()),
                () -> assertNotNull(request.getVotingEnd()),
                () -> assertNotNull(request.getResults())
        );
    }

    @Test
    @DisplayName("should fail validation when name is blank")
    void shouldFailValidationWhenNameIsBlank() {
        var request = new CreateElectoralProcessRequest();
        request.setScope("test-scope");
        request.setCommitmentStart(Instant.now());
        request.setCommitmentEnd(Instant.now().plusSeconds(3600));
        request.setVotingStart(Instant.now().plusSeconds(7200));
        request.setVotingEnd(Instant.now().plusSeconds(10800));
        request.setResults(Instant.now().plusSeconds(14400));

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have validation errors for blank name");
    }

    @Test
    @DisplayName("should fail validation when scope is blank")
    void shouldFailValidationWhenScopeIsBlank() {
        var request = new CreateElectoralProcessRequest();
        request.setName("Test Process");
        request.setCommitmentStart(Instant.now());
        request.setCommitmentEnd(Instant.now().plusSeconds(3600));
        request.setVotingStart(Instant.now().plusSeconds(7200));
        request.setVotingEnd(Instant.now().plusSeconds(10800));
        request.setResults(Instant.now().plusSeconds(14400));

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have validation errors for blank scope");
    }

    // ---- UpdateElectoralProcessRequest ----

    @Test
    @DisplayName("should create UpdateElectoralProcessRequest with all fields")
    void shouldCreateUpdateRequestWithAllFields() {
        var now = Instant.now();
        var request = new UpdateElectoralProcessRequest();
        request.setName("Updated Election");
        request.setCommitmentStart(now);
        request.setCommitmentEnd(now.plusSeconds(3600));
        request.setVotingStart(now.plusSeconds(7200));
        request.setVotingEnd(now.plusSeconds(10800));
        request.setResults(now.plusSeconds(14400));

        assertAll("update request",
                () -> assertEquals("Updated Election", request.getName()),
                () -> assertNotNull(request.getCommitmentStart()),
                () -> assertNotNull(request.getCommitmentEnd()),
                () -> assertNotNull(request.getVotingStart()),
                () -> assertNotNull(request.getVotingEnd()),
                () -> assertNotNull(request.getResults())
        );
    }

    // ---- ElectoralProcessResponse ----

    @Test
    @DisplayName("should create ElectoralProcessResponse with all fields")
    void shouldCreateProcessResponse() {
        var id = UUID.randomUUID();
        var now = Instant.now();

        var response = new ElectoralProcessResponse();
        response.setId(id);
        response.setName("Test Process");
        response.setScope("test-scope");
        response.setCommitmentStart(now);
        response.setCommitmentEnd(now.plusSeconds(3600));
        response.setVotingStart(now.plusSeconds(7200));
        response.setVotingEnd(now.plusSeconds(10800));
        response.setResults(now.plusSeconds(14400));

        assertAll("process response",
                () -> assertEquals(id, response.getId()),
                () -> assertEquals("Test Process", response.getName()),
                () -> assertEquals("test-scope", response.getScope())
        );
    }

    // ---- ProcessStateResponse ----

    @Test
    @DisplayName("should create ProcessStateResponse")
    void shouldCreateProcessStateResponse() {
        var processId = UUID.randomUUID();
        var response = new ProcessStateResponse(processId, ProcessStatus.COMMITMENT);

        assertAll("state response",
                () -> assertEquals(processId, response.getProcessId()),
                () -> assertEquals(ProcessStatus.COMMITMENT, response.getState())
        );
    }

    // ---- PaginatedResponse ----

    @Test
    @DisplayName("should create PaginatedResponse with content and metadata")
    void shouldCreatePaginatedResponse() {
        var items = List.of("item1", "item2", "item3");
        var response = new PaginatedResponse<String>(items, 0, 3, 10L, 4);

        assertAll("paginated response",
                () -> assertEquals(3, response.getContent().size()),
                () -> assertEquals(0, response.getPage()),
                () -> assertEquals(3, response.getSize()),
                () -> assertEquals(10L, response.getTotalElements()),
                () -> assertEquals(4, response.getTotalPages()),
                () -> assertTrue(response.isFirst()),
                () -> assertFalse(response.isLast())
        );
    }

    @Test
    @DisplayName("should detect first page correctly")
    void shouldDetectFirstPage() {
        var response = new PaginatedResponse<>(List.of(), 0, 10, 0L, 0);
        assertTrue(response.isFirst());
    }

    @Test
    @DisplayName("should detect last page correctly")
    void shouldDetectLastPage() {
        var response = new PaginatedResponse<>(List.of(), 1, 10, 10L, 1);
        assertTrue(response.isLast());
    }
}
