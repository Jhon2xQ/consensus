package com.carmenio.consensus.application.use_case.electoral_process;

import com.carmenio.consensus.application.dto.electoral_process.ProcessStateResponse;
import com.carmenio.consensus.common.constant.ProcessStatus;
import com.carmenio.consensus.domain.entity.ElectoralProcess;
import com.carmenio.consensus.domain.exception.ElectoralProcessException;
import com.carmenio.consensus.domain.repository.ElectoralProcessRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProcessStateUseCaseTest {

    @Mock
    private ElectoralProcessRepository repository;

    @InjectMocks
    private GetProcessStateUseCase useCase;

    private ElectoralProcess createProcess(Instant commitmentStart, Instant commitmentEnd,
                                           Instant votingStart, Instant votingEnd, Instant results) {
        return ElectoralProcess.builder()
                .id(UUID.randomUUID())
                .name("Test Process")
                .scope("test-scope")
                .commitmentStart(commitmentStart)
                .commitmentEnd(commitmentEnd)
                .votingStart(votingStart)
                .votingEnd(votingEnd)
                .results(results)
                .build();
    }

    @Test
    @DisplayName("should return NONE when before commitment period")
    void shouldReturnNoneWhenBeforeCommitmentPeriod() {
        var now = Instant.parse("2026-01-05T00:00:00Z");
        var process = createProcess(
                Instant.parse("2026-01-10T00:00:00Z"),
                Instant.parse("2026-01-20T00:00:00Z"),
                Instant.parse("2026-02-01T00:00:00Z"),
                Instant.parse("2026-02-10T00:00:00Z"),
                Instant.parse("2026-03-01T00:00:00Z")
        );

        when(repository.findById(process.getId())).thenReturn(Optional.of(process));

        var response = useCase.execute(process.getId(), now);

        assertEquals(ProcessStatus.NONE, response.getState());
    }

    @Test
    @DisplayName("should return COMMITMENT when within commitment period")
    void shouldReturnCommitmentWhenWithinCommitmentPeriod() {
        var now = Instant.parse("2026-01-15T00:00:00Z");
        var process = createProcess(
                Instant.parse("2026-01-10T00:00:00Z"),
                Instant.parse("2026-01-20T00:00:00Z"),
                Instant.parse("2026-02-01T00:00:00Z"),
                Instant.parse("2026-02-10T00:00:00Z"),
                Instant.parse("2026-03-01T00:00:00Z")
        );

        when(repository.findById(process.getId())).thenReturn(Optional.of(process));

        var response = useCase.execute(process.getId(), now);

        assertEquals(ProcessStatus.COMMITMENT, response.getState());
    }

    @Test
    @DisplayName("should return VOTING when within voting period")
    void shouldReturnVotingWhenWithinVotingPeriod() {
        var now = Instant.parse("2026-02-05T00:00:00Z");
        var process = createProcess(
                Instant.parse("2026-01-10T00:00:00Z"),
                Instant.parse("2026-01-20T00:00:00Z"),
                Instant.parse("2026-02-01T00:00:00Z"),
                Instant.parse("2026-02-10T00:00:00Z"),
                Instant.parse("2026-03-01T00:00:00Z")
        );

        when(repository.findById(process.getId())).thenReturn(Optional.of(process));

        var response = useCase.execute(process.getId(), now);

        assertEquals(ProcessStatus.VOTING, response.getState());
    }

    @Test
    @DisplayName("should return CLOSED when after results date")
    void shouldReturnClosedWhenAfterResultsDate() {
        var now = Instant.parse("2026-03-05T00:00:00Z");
        var process = createProcess(
                Instant.parse("2026-01-10T00:00:00Z"),
                Instant.parse("2026-01-20T00:00:00Z"),
                Instant.parse("2026-02-01T00:00:00Z"),
                Instant.parse("2026-02-10T00:00:00Z"),
                Instant.parse("2026-03-01T00:00:00Z")
        );

        when(repository.findById(process.getId())).thenReturn(Optional.of(process));

        var response = useCase.execute(process.getId(), now);

        assertEquals(ProcessStatus.CLOSED, response.getState());
    }

    @Test
    @DisplayName("should throw when process not found")
    void shouldThrowWhenProcessNotFound() {
        var id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ElectoralProcessException.class,
                () -> useCase.execute(id, Instant.now()));
    }

    @Test
    @DisplayName("should include processId in response")
    void shouldIncludeProcessIdInResponse() {
        var now = Instant.parse("2026-01-15T00:00:00Z");
        var process = createProcess(
                Instant.parse("2026-01-10T00:00:00Z"),
                Instant.parse("2026-01-20T00:00:00Z"),
                Instant.parse("2026-02-01T00:00:00Z"),
                Instant.parse("2026-02-10T00:00:00Z"),
                Instant.parse("2026-03-01T00:00:00Z")
        );

        when(repository.findById(process.getId())).thenReturn(Optional.of(process));

        var response = useCase.execute(process.getId(), now);

        assertAll("state response",
                () -> assertEquals(process.getId(), response.getProcessId()),
                () -> assertEquals(ProcessStatus.COMMITMENT, response.getState())
        );
    }
}
