package com.carmenio.consensus.application.util;

import com.carmenio.consensus.application.dto.record.TeamResult;
import com.carmenio.consensus.domain.entity.Team;
import com.carmenio.consensus.domain.entity.VoteRecord;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility that tallies vote records against candidate teams.
 * <p>
 * Votes are grouped by {@link VoteRecord#getMessage()} (which contains
 * the team name) and matched to existing {@link Team} entities.
 * Results are sorted by vote count in descending order.
 */
public final class VoteCounter {

    private VoteCounter() {
        // Utility class — no instantiation
    }

    /**
     * Counts votes per team, returned as a map of team name → vote count.
     * <p>
     * Only teams present in the {@code teams} list are included in the map.
     * Records whose {@code message} does not match any team name are ignored.
     * The map is sorted by vote count in descending order.
     *
     * @param records the list of validated vote records
     * @param teams   the list of candidate teams
     * @return ordered map of team name to vote count (descending by count)
     */
    public static Map<String, Long> countVotes(List<VoteRecord> records, List<Team> teams) {
        // Initialize all teams with zero votes
        Map<String, Long> tally = teams.stream()
                .collect(Collectors.toMap(
                        Team::getName,
                        team -> 0L,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        // Count votes for each record whose message matches a team name
        for (VoteRecord record : records) {
            tally.computeIfPresent(record.getMessage(), (key, count) -> count + 1);
        }

        // Sort by count descending
        return tally.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    /**
     * Calculates results as a list of {@link TeamResult} sorted by vote count
     * in descending order.
     *
     * @param records the list of validated vote records
     * @param teams   the list of candidate teams
     * @return sorted list of team results (highest votes first)
     */
    public static List<TeamResult> calculateResults(List<VoteRecord> records, List<Team> teams) {
        Map<String, Long> tally = countVotes(records, teams);

        return tally.entrySet().stream()
                .map(entry -> TeamResult.builder()
                        .teamName(entry.getKey())
                        .voteCount(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }
}
