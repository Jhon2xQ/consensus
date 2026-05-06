package com.carmenio.consensus.application.util;

import com.carmenio.consensus.domain.entity.Team;
import com.carmenio.consensus.domain.entity.VoteRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link VoteCounter}.
 * <p>
 * Verifies vote tallying logic: counting by team name, handling
 * empty scenarios, and sorting by vote count.
 */
class VoteCounterTest {

    private static VoteRecord createRecord(String message) {
        return VoteRecord.builder()
                .groupId("1")
                .nullifier(UUID.randomUUID().toString())
                .message(message)
                .scope("scope-1")
                .build();
    }

    private static Team createTeam(String name) {
        return Team.builder()
                .id(UUID.randomUUID())
                .name(name)
                .electoralProcessId(UUID.randomUUID())
                .build();
    }

    @Test
    @DisplayName("Should count votes grouped by team name")
    void shouldCountVotesByTeamName() {
        var teamA = createTeam("Team A");
        var teamB = createTeam("Team B");
        var records = List.of(
                createRecord("Team A"),
                createRecord("Team A"),
                createRecord("Team A"),
                createRecord("Team B"),
                createRecord("Team B")
        );

        var result = VoteCounter.countVotes(records, List.of(teamA, teamB));

        assertEquals(3L, result.get("Team A"));
        assertEquals(2L, result.get("Team B"));
    }

    @Test
    @DisplayName("Should return zero votes for all teams when no records exist")
    void shouldReturnZeroForAllTeamsWhenNoRecords() {
        var teamA = createTeam("Team A");
        var teamB = createTeam("Team B");

        var result = VoteCounter.countVotes(List.of(), List.of(teamA, teamB));

        assertEquals(0L, result.get("Team A"));
        assertEquals(0L, result.get("Team B"));
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should not count votes for teams with no matching records")
    void shouldNotCountVotesForTeamsWithNoMatchingRecords() {
        var teamA = createTeam("Team A");
        var teamB = createTeam("Team B");
        var records = List.of(
                createRecord("Team A"),
                createRecord("Team A")
        );

        var result = VoteCounter.countVotes(records, List.of(teamA, teamB));

        assertEquals(2L, result.get("Team A"));
        assertEquals(0L, result.get("Team B"));
    }

    @Test
    @DisplayName("Should return empty map when no teams exist")
    void shouldReturnEmptyMapWhenNoTeams() {
        var records = List.of(
                createRecord("Team A"),
                createRecord("Team B")
        );

        var result = VoteCounter.countVotes(records, List.of());

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should sort results by vote count descending")
    void shouldSortResultsByVoteCountDescending() {
        var teamA = createTeam("Team A");
        var teamB = createTeam("Team B");
        var teamC = createTeam("Team C");
        var records = List.of(
                createRecord("Team B"),
                createRecord("Team A"),
                createRecord("Team A"),
                createRecord("Team C"),
                createRecord("Team C"),
                createRecord("Team C")
        );

        var results = VoteCounter.calculateResults(records, List.of(teamA, teamB, teamC));

        assertEquals(3, results.size());
        assertEquals("Team C", results.get(0).getTeamName());
        assertEquals(3L, results.get(0).getVoteCount());
        assertEquals("Team A", results.get(1).getTeamName());
        assertEquals(2L, results.get(1).getVoteCount());
        assertEquals("Team B", results.get(2).getTeamName());
        assertEquals(1L, results.get(2).getVoteCount());
    }

    @Test
    @DisplayName("Should return empty list when no teams")
    void shouldReturnEmptyResultsWhenNoTeams() {
        var records = List.of(createRecord("Team A"));
        var results = VoteCounter.calculateResults(records, List.of());
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should return all teams with zero votes when no records")
    void shouldReturnAllTeamsWithZeroVotesWhenNoRecords() {
        var teamA = createTeam("Team A");
        var teamB = createTeam("Team B");

        var results = VoteCounter.calculateResults(List.of(), List.of(teamA, teamB));

        assertEquals(2, results.size());
        assertEquals(0L, results.get(0).getVoteCount());
        assertEquals(0L, results.get(1).getVoteCount());
    }

    @Test
    @DisplayName("Should handle empty records and empty teams")
    void shouldHandleEmptyRecordsAndEmptyTeams() {
        var countResult = VoteCounter.countVotes(List.of(), List.of());
        assertTrue(countResult.isEmpty());

        var resultsResult = VoteCounter.calculateResults(List.of(), List.of());
        assertTrue(resultsResult.isEmpty());
    }

    @Test
    @DisplayName("Should handle records whose message does not match any team")
    void shouldHandleRecordsNotMatchingAnyTeam() {
        var teamA = createTeam("Team A");
        var records = List.of(
                createRecord("Team A"),
                createRecord("Team A"),
                createRecord("NonExistent Team"),
                createRecord("Another Unknown")
        );

        var result = VoteCounter.countVotes(records, List.of(teamA));

        // Only Team A should be counted; non-matching messages are ignored
        assertEquals(2L, result.get("Team A"));
        assertEquals(1, result.size());
    }
}
