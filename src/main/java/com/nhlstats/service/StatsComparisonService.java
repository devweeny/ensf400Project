package com.nhlstats.service;

import com.nhlstats.model.PlayerStats;

public class StatsComparisonService {

    // Compares two players and returns a comparison object or summary string
    public String comparePlayers(PlayerStats player1, PlayerStats player2) {
        // e.g., "Player A has more goals, Player B has better faceoff win %"
        return "";
    }

    // Returns a numeric comparison (e.g., goals, assists, +/-) as a score difference
    public Map<String, Integer> compareStats(PlayerStats player1, PlayerStats player2) {
        // e.g., {"Goals": +10, "Assists": -5}
        return new HashMap<>();
    }

    // Rank a list of players by a specific stat (e.g., goals, assists, points)
    public List<PlayerStats> rankPlayersByStat(List<PlayerStats> players, String statName) {
        // sort and return
        return new ArrayList<>();
    }
}
