package com.nhlstats.service;

import com.nhlstats.model.PlayerStats;
import java.util.*;

public class DataVisualizationService {

    // Prepare data for bar chart (e.g., goals comparison)
    public Map<String, Integer> prepareBarChartData(List<PlayerStats> players, String statName) {
        Map<String, Integer> chartData = new HashMap<>();
        for (PlayerStats ps : players) {
            chartData.put(ps.getPlayerName(), ps.getStatByName(statName));
        }
        return chartData;
    }

    // Prepare data for radar/spider chart (e.g., overall performance categories)
    public Map<String, Map<String, Integer>> prepareRadarChartData(List<PlayerStats> players) {
        Map<String, Map<String, Integer>> radarData = new HashMap<>();
        for (PlayerStats ps : players) {
            radarData.put(ps.getPlayerName(), ps.getAllStats());
        }
        return radarData;
    }

    // Normalize stats to 0-100 scale for visual balance
    public Map<String, Double> normalizeStats(Map<String, Integer> rawStats) {
        Map<String, Double> normalized = new HashMap<>();
        int max = Collections.max(rawStats.values());
        for (Map.Entry<String, Integer> entry : rawStats.entrySet()) {
            normalized.put(entry.getKey(), (entry.getValue() * 100.0) / max);
        }
        return normalized;
    }
}
