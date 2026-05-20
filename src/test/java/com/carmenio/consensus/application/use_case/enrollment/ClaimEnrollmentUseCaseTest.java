package com.carmenio.consensus.application.use_case.enrollment;

import com.carmenio.consensus.application.dto.enrollment.ClaimEnrollmentRequest;
import com.carmenio.consensus.application.dto.enrollment.EnrollmentResponse;
import com.carmenio.consensus.application.util.JwtClaimExtractor;
import com.carmenio.consensus.common.constant.ProcessStatus;
import com.carmenio.consensus.domain.entity.ElectoralProcess;
import com.carmenio.consensus.domain.entity.Enrollment;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.domain.exception.EnrollmentException;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import com.carmenio.consensus.domain.repository.EnrollmentRepository;
import com.carmenio.consensus.infrastructure.mapper.EnrollmentMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimEnrollmentUseCaseTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private ElectoralProcessRepository electoralProcessRepository;

    @Mock
    private JwtClaimExtractor jwtClaimExtractor;

    @Mock
    private EnrollmentMapper mapper;

    @InjectMocks
    private ClaimEnrollmentUseCase useCase;

    private Jwt createJwt(Map<String, Object> claims) {
        return new Jwt(
                "test-token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                claims
        );
    }

    private ElectoralProcess createProcess(Instant now, ProcessStatus desiredState) {
        return switch (desiredState) {
            case COMMITMENT -> ElectoralProcess.builder()
                    .id(UUID.randomUUID())
                    .commitmentStart(now.minusSeconds(3600))
                    .commitmentEnd(now.plusSeconds(3600))
                    .votingStart(now.plusSeconds(7200))
                    .votingEnd(now.plusSeconds(14400))
                    .results(now.plusSeconds(21600))
                    .build();
            case NONE -> ElectoralProcess.builder()
                    .id(UUID.randomUUID())
                    .commitmentStart(now.plusSeconds(3600))
                    .commitmentEnd(now.plusSeconds(7200))
                    .votingStart(now.plusSeconds(10800))
                    .votingEnd(now.plusSeconds(14400))
                    .results(now.plusSeconds(21600))
                    .build();
            case VOTING -> ElectoralProcess.builder()
                    .id(UUID.randomUUID())
                    .commitmentStart(now.minusSeconds(14400))
                    .commitmentEnd(now.minusSeconds(7200))
                    .votingStart(now.minusSeconds(3600))
                    .votingEnd(now.plusSeconds(3600))
                    .results(now.plusSeconds(7200))
                    .build();
            case CLOSED -> ElectoralProcess.builder()
                    .id(UUID.randomUUID())
                    .commitmentStart(now.minusSeconds(14400))
                    .commitmentEnd(now.minusSeconds(10800))
                    .votingStart(now.minusSeconds(7200))
                    .votingEnd(now.minusSeconds(3600))
                    .results(now.minusSeconds(1))
                    .build();
        };
    }

    private Enrollment createUnclaimedEnrollment(UUID id, UUID processId, String email) {
        var enrollment = new Enrollment();
        enrollment.setId(id);
        enrollment.setElectoralProcessId(processId);
        enrollment.setEmail(email);
        enrollment.setUserId(null);
        enrollment.setCommitment(null);
        enrollment.setHasVoted(false);
        return enrollment;
    }

    @Test
    @DisplayName("Should claim enrollment when JWT email matches and process is in COMMITMENT state")
    void shouldClaimEnrollmentWhenEmailMatchesAndProcessInCommitment() {
        var now = Instant.now();
        var enrollmentId = UUID.randomUUID();
        var process = createProcess(now, ProcessStatus.COMMITMENT);
        var processId = process.getId();
        var email = "voter@example.com";
        var userId = "user-123";
        var commitment = "1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111";

        var enrollment = createUnclaimedEnrollment(enrollmentId, processId, email);

        var jwt = createJwt(Map.of("email", email, "sub", userId));

        var request = ClaimEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .commitment(commitment)
                .build();

        var expectedResponse = EnrollmentResponse.builder()
                .id(enrollmentId)
                .electoralProcessId(processId)
                .email(email)
                .userId(userId)
                .commitment(commitment)
                .hasVoted(false)
                .build();

        when(jwtClaimExtractor.extractEmail(jwt)).thenReturn(email);
        when(jwtClaimExtractor.extractUserId(jwt)).thenReturn(userId);
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
        when(electoralProcessRepository.findById(processId)).thenReturn(Optional.of(process));
        when(enrollmentRepository.existsByElectoralProcessIdAndCommitment(processId, commitment))
                .thenReturn(false);
        when(enrollmentRepository.save(enrollment)).thenReturn(enrollment);
        when(mapper.toResponse(enrollment)).thenReturn(expectedResponse);

        var result = useCase.execute(enrollmentId, request, jwt);

        assertNotNull(result);
        assertEquals(enrollmentId, result.getId());
        assertEquals(processId, result.getElectoralProcessId());
        assertEquals(email, result.getEmail());
        assertEquals(userId, result.getUserId());
        assertEquals(commitment, result.getCommitment());
        assertFalse(result.isHasVoted());

        verify(jwtClaimExtractor).extractEmail(jwt);
        verify(jwtClaimExtractor).extractUserId(jwt);
        verify(enrollmentRepository).findById(enrollmentId);
        verify(electoralProcessRepository).findById(processId);
        verify(enrollmentRepository).existsByElectoralProcessIdAndCommitment(processId, commitment);
        verify(enrollmentRepository).save(enrollment);
        verify(mapper).toResponse(enrollment);
    }

    @Test
    @DisplayName("Should claim enrollment when process is in NONE state")
    void shouldClaimEnrollmentWhenProcessInNone() {
        var now = Instant.now();
        var enrollmentId = UUID.randomUUID();
        var process = createProcess(now, ProcessStatus.NONE);
        var processId = process.getId();
        var email = "voter@example.com";
        var userId = "user-456";
        var commitment = "2222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222";

        var enrollment = createUnclaimedEnrollment(enrollmentId, processId, email);

        var jwt = createJwt(Map.of("email", email, "sub", userId));

        var request = ClaimEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .commitment(commitment)
                .build();

        var expectedResponse = EnrollmentResponse.builder()
                .id(enrollmentId)
                .electoralProcessId(processId)
                .email(email)
                .userId(userId)
                .commitment(commitment)
                .hasVoted(false)
                .build();

        when(jwtClaimExtractor.extractEmail(jwt)).thenReturn(email);
        when(jwtClaimExtractor.extractUserId(jwt)).thenReturn(userId);
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
        when(electoralProcessRepository.findById(processId)).thenReturn(Optional.of(process));
        when(enrollmentRepository.existsByElectoralProcessIdAndCommitment(processId, commitment))
                .thenReturn(false);
        when(enrollmentRepository.save(enrollment)).thenReturn(enrollment);
        when(mapper.toResponse(enrollment)).thenReturn(expectedResponse);

        var result = useCase.execute(enrollmentId, request, jwt);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(userId, result.getUserId());
    }

    @Test
    @DisplayName("Should throw 404 when email does not match enrollment email")
    void shouldThrow404WhenEmailMismatch() {
        var enrollmentId = UUID.randomUUID();
        var processId = UUID.randomUUID();
        var enrollment = createUnclaimedEnrollment(enrollmentId, processId, "enrolled@example.com");

        var jwt = createJwt(Map.of("email", "other@example.com", "sub", "user-123"));

        var request = ClaimEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .build();

        when(jwtClaimExtractor.extractEmail(jwt)).thenReturn("other@example.com");
        when(jwtClaimExtractor.extractUserId(jwt)).thenReturn("user-123");
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));

        var exception = assertThrows(EnrollmentException.class,
                () -> useCase.execute(enrollmentId, request, jwt));

        assertEquals(404, exception.getStatusCode());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw 404 when enrollment not found by ID")
    void shouldThrow404WhenEnrollmentNotFound() {
        var enrollmentId = UUID.randomUUID();
        var processId = UUID.randomUUID();
        var jwt = createJwt(Map.of("email", "voter@example.com", "sub", "user-123"));

        var request = ClaimEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .build();

        when(jwtClaimExtractor.extractEmail(jwt)).thenReturn("voter@example.com");
        when(jwtClaimExtractor.extractUserId(jwt)).thenReturn("user-123");
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.empty());

        var exception = assertThrows(EnrollmentException.class,
                () -> useCase.execute(enrollmentId, request, jwt));

        assertEquals(404, exception.getStatusCode());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw 404 when body processId does not match enrollment processId")
    void shouldThrow404WhenProcessIdMismatch() {
        var enrollmentId = UUID.randomUUID();
        var enrollmentProcessId = UUID.randomUUID();
        var bodyProcessId = UUID.randomUUID();
        var email = "voter@example.com";
        var enrollment = createUnclaimedEnrollment(enrollmentId, enrollmentProcessId, email);

        var jwt = createJwt(Map.of("email", email, "sub", "user-123"));

        var request = ClaimEnrollmentRequest.builder()
                .electoralProcessId(bodyProcessId)
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .build();

        when(jwtClaimExtractor.extractEmail(jwt)).thenReturn(email);
        when(jwtClaimExtractor.extractUserId(jwt)).thenReturn("user-123");
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));

        var exception = assertThrows(EnrollmentException.class,
                () -> useCase.execute(enrollmentId, request, jwt));

        assertEquals(404, exception.getStatusCode());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw 404 when electoral process not found")
    void shouldThrow404WhenProcessNotFound() {
        var enrollmentId = UUID.randomUUID();
        var processId = UUID.randomUUID();
        var email = "voter@example.com";
        var enrollment = createUnclaimedEnrollment(enrollmentId, processId, email);

        var jwt = createJwt(Map.of("email", email, "sub", "user-123"));

        var request = ClaimEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .build();

        when(jwtClaimExtractor.extractEmail(jwt)).thenReturn(email);
        when(jwtClaimExtractor.extractUserId(jwt)).thenReturn("user-123");
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
        when(electoralProcessRepository.findById(processId)).thenReturn(Optional.empty());

        var exception = assertThrows(ElectoralProcessException.class,
                () -> useCase.execute(enrollmentId, request, jwt));

        assertEquals(404, exception.getStatusCode());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw 400 when process is in VOTING state")
    void shouldThrow400WhenProcessInVoting() {
        var now = Instant.now();
        var enrollmentId = UUID.randomUUID();
        var process = createProcess(now, ProcessStatus.VOTING);
        var processId = process.getId();
        var email = "voter@example.com";
        var enrollment = createUnclaimedEnrollment(enrollmentId, processId, email);

        var jwt = createJwt(Map.of("email", email, "sub", "user-123"));

        var request = ClaimEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .build();

        when(jwtClaimExtractor.extractEmail(jwt)).thenReturn(email);
        when(jwtClaimExtractor.extractUserId(jwt)).thenReturn("user-123");
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
        when(electoralProcessRepository.findById(processId)).thenReturn(Optional.of(process));

        var exception = assertThrows(EnrollmentException.class,
                () -> useCase.execute(enrollmentId, request, jwt));

        assertTrue(exception.getMessage().contains("Enrollment not open"));
        assertEquals(400, exception.getStatusCode());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw 400 when process is in CLOSED state")
    void shouldThrow400WhenProcessInClosed() {
        var now = Instant.now();
        var enrollmentId = UUID.randomUUID();
        var process = createProcess(now, ProcessStatus.CLOSED);
        var processId = process.getId();
        var email = "voter@example.com";
        var enrollment = createUnclaimedEnrollment(enrollmentId, processId, email);

        var jwt = createJwt(Map.of("email", email, "sub", "user-123"));

        var request = ClaimEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .build();

        when(jwtClaimExtractor.extractEmail(jwt)).thenReturn(email);
        when(jwtClaimExtractor.extractUserId(jwt)).thenReturn("user-123");
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
        when(electoralProcessRepository.findById(processId)).thenReturn(Optional.of(process));

        var exception = assertThrows(EnrollmentException.class,
                () -> useCase.execute(enrollmentId, request, jwt));

        assertTrue(exception.getMessage().contains("Enrollment not open"));
        assertEquals(400, exception.getStatusCode());
    }

    @Test
    @DisplayName("Should throw 409 when commitment already exists in process")
    void shouldThrow409WhenDuplicateCommitment() {
        var now = Instant.now();
        var enrollmentId = UUID.randomUUID();
        var process = createProcess(now, ProcessStatus.COMMITMENT);
        var processId = process.getId();
        var email = "voter@example.com";
        var commitment = "1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111";
        var enrollment = createUnclaimedEnrollment(enrollmentId, processId, email);

        var jwt = createJwt(Map.of("email", email, "sub", "user-123"));

        var request = ClaimEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .commitment(commitment)
                .build();

        when(jwtClaimExtractor.extractEmail(jwt)).thenReturn(email);
        when(jwtClaimExtractor.extractUserId(jwt)).thenReturn("user-123");
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
        when(electoralProcessRepository.findById(processId)).thenReturn(Optional.of(process));
        when(enrollmentRepository.existsByElectoralProcessIdAndCommitment(processId, commitment))
                .thenReturn(true);

        var exception = assertThrows(EnrollmentException.class,
                () -> useCase.execute(enrollmentId, request, jwt));

        assertEquals(409, exception.getStatusCode());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw 401 when JWT email claim is missing")
    void shouldThrow401WhenJwtEmailMissing() {
        var enrollmentId = UUID.randomUUID();
        var processId = UUID.randomUUID();
        var jwt = createJwt(Map.of("sub", "user-123"));

        var request = ClaimEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .build();

        when(jwtClaimExtractor.extractEmail(jwt))
                .thenThrow(new BadCredentialsException("Missing email claim in JWT"));

        assertThrows(BadCredentialsException.class,
                () -> useCase.execute(enrollmentId, request, jwt));

        verify(enrollmentRepository, never()).findById(any());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw 401 when JWT sub claim is missing")
    void shouldThrow401WhenJwtSubMissing() {
        var enrollmentId = UUID.randomUUID();
        var processId = UUID.randomUUID();
        var jwt = createJwt(Map.of("email", "voter@example.com"));

        var request = ClaimEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .build();

        when(jwtClaimExtractor.extractEmail(jwt)).thenReturn("voter@example.com");
        when(jwtClaimExtractor.extractUserId(jwt))
                .thenThrow(new BadCredentialsException("Missing sub claim in JWT"));

        assertThrows(BadCredentialsException.class,
                () -> useCase.execute(enrollmentId, request, jwt));

        verify(enrollmentRepository, never()).findById(any());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw 409 when enrollment is already claimed (userId is not null)")
    void shouldThrow409WhenAlreadyClaimed() {
        var enrollmentId = UUID.randomUUID();
        var processId = UUID.randomUUID();
        var email = "voter@example.com";

        var enrollment = new Enrollment();
        enrollment.setId(enrollmentId);
        enrollment.setElectoralProcessId(processId);
        enrollment.setEmail(email);
        enrollment.setUserId("already-claimed-user");
        enrollment.setCommitment(null);
        enrollment.setHasVoted(false);

        var jwt = createJwt(Map.of("email", email, "sub", "user-123"));

        var request = ClaimEnrollmentRequest.builder()
                .electoralProcessId(processId)
                .commitment("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .build();

        when(jwtClaimExtractor.extractEmail(jwt)).thenReturn(email);
        when(jwtClaimExtractor.extractUserId(jwt)).thenReturn("user-123");
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));

        var exception = assertThrows(EnrollmentException.class,
                () -> useCase.execute(enrollmentId, request, jwt));

        assertEquals(409, exception.getStatusCode());
        verify(enrollmentRepository, never()).save(any());
    }
}
