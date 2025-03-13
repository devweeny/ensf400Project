package com.nhlstats.service;

import com.nhlstats.model.Player;
import com.nhlstats.model.PlayerStats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service for preparing player statistics for visualization.
 */
public class DataVisualizationService {

    /**
     * Prepares data for bar chart visualization of a specific stat.
     * 
     * @param players the list of players to include in the chart
     * @param statName the statistic to chart
     * @return a map of player names to their statistic values
     */
    public Map<String, Number> prepareBarChartData(List<Player> players, String statName) {
        Map<String, Number> chartData = new HashMap<>();
        
        for (Player player : players) {
            if (player.getSeasonStats() == null) {
                continue;
            }
            
            Number value = getStatByName(player.getSeasonStats(), statName);
            chartData.put(player.getFullName(), value);
        }
        
        return chartData;
    }
    
    /**
     * Prepares data for radar/spider chart visualization.
     * 
     * @param players the list of players to include in the chart
     * @return a map of player names to their statistics maps
     */
    public Map<String, Map<String, Number>> prepareRadarChartData(List<Player> players) {
        Map<String, Map<String, Number>> radarData = new HashMap<>();
        
        for (Player player : players) {
            if (player.getSeasonStats() == null) {
                continue;
            }
            
            Map<String, Number> stats = getAllStats(player.getSeasonStats());
            radarData.put(player.getFullName(), stats);
        }
        
        return radarData;
    }
    
    /**
     * Prepares data for line chart visualization of game-by-game performance.
     * 
     * @param player the player to chart
     * @param statName the statistic to chart
     * @return a map of game dates to statistic values
     */
    public Map<String, Number> prepareGameByGameLineChartData(Player player, String statName) {
        Map<String, Number> chartData = new HashMap<>();
        
        if (player == null || player.getGameLogs() == null) {
            return chartData;
        }
        
        player.getGameLogs().forEach(gameLog -> {
            String gameDate = gameLog.getGameDate().toString();
            
            switch (statName.toLowerCase()) {
                case "goals":
                    chartData.put(gameDate, gameLog.getGoals());
                    break;
                case "assists":
                    chartData.put(gameDate, gameLog.getAssists());
                    break;
                case "points":
                    chartData.put(gameDate, gameLog.getPoints());
                    break;
                case "plusminus":
                    chartData.put(gameDate, gameLog.getPlusMinus());
                    break;
                case "shots":
                    chartData.put(gameDate, gameLog.getShots());
                    break;
                case "timeonice":
                    chartData.put(gameDate, gameLog.getTimeOnIceMinutes());
                    break;
                default:
                    chartData.put(gameDate, gameLog.getPoints());
                    break;
            }
        });
        
        return chartData;
    }
    
    /**
     * Normalizes stats to 0-100 scale for visual balance.
     * 
     * @param rawStats the raw statistics values
     * @return normalized values on a 0-100 scale
     */
    public Map<String, Double> normalizeStats(Map<String, Number> rawStats) {
        Map<String, Double> normalized = new HashMap<>();
        
        if (rawStats.isEmpty()) {
            return normalized;
        }
        
        double max = rawStats.values().stream()
                .mapToDouble(Number::doubleValue)
                .max()
                .orElse(1.0);
        
        for (Map.Entry<String, Number> entry : rawStats.entrySet()) {
            normalized.put(entry.getKey(), (entry.getValue().doubleValue() * 100.0) / max);
        }
        
        return normalized;
    }
    
    /**
     * Prepares a cumulative performance chart showing progress over the season.
     * 
     * @param player the player to chart
     * @param statName the statistic to accumulate
     * @return a map of game dates to cumulative statistic values
     */
    public Map<String, Number> prepareCumulativeStatChart(Player player, String statName) {
        Map<String, Number> chartData = new HashMap<>();
        
        if (player == null || player.getGameLogs() == null) {
            return chartData;
        }
        
        int cumulative = 0;
        
        for (int i = 0; i < player.getGameLogs().size(); i++) {
            var gameLog = player.getGameLogs().get(i);
            String gameDate = gameLog.getGameDate().toString();
            
            switch (statName.toLowerCase()) {
                case "goals":
                    cumulative += gameLog.getGoals();
                    break;
                case "assists":
                    cumulative += gameLog.getAssists();
                    break;
                case "points":
                    cumulative += gameLog.getPoints();
                    break;
                default:
                    cumulative += gameLog.getPoints();
                    break;
            }
            
            chartData.put(gameDate, cumulative);
        }
        
        return chartData;
    }

    /**
     * Gets a specific statistic value by name from a PlayerStats object.
     * 
     * @param stats the player statistics
     * @param statName the name of the statistic to retrieve
     * @return the statistic value
     */
    private Number getStatByName(PlayerStats stats, String statName) {
        switch (statName.toLowerCase()) {
            case "goals":
                return stats.getGoals();
            case "assists":
                return stats.getAssists();
            case "points":
                return stats.getPoints();
            case "plusminus":
                return stats.getPlusMinus();
            case "pointspergame":
                return stats.getPointsPerGame();
            case "shotpercentage":
                return stats.getShotPercentage();
            case "powerplaygoals":
                return stats.getPowerPlayGoals();
            case "powerplaypoints":
                return stats.getPowerPlayPoints();
            case "shots":
                return stats.getShots();
            case "gamesplayed":
                return stats.getGamesPlayed();
            default:
                return 0;
        }
    }
    
    /**
     * Gets a map of all statistics for a player.
     * 
     * @param stats the player statistics
     * @return a map of statistic names to values
     */
    private Map<String, Number> getAllStats(PlayerStats stats) {
        Map<String, Number> allStats = new HashMap<>();
        
        allStats.put("Goals", stats.getGoals());
        allStats.put("Assists", stats.getAssists());
        allStats.put("Points", stats.getPoints());
        allStats.put("Plus/Minus", stats.getPlusMinus());
        allStats.put("PPG", stats.getPowerPlayGoals());
        allStats.put("PPP", stats.getPowerPlayPoints());
        allStats.put("Shots", stats.getShots());
        allStats.put("Pts/GP", stats.getPointsPerGame());
        allStats.put("Shot%", stats.getShotPercentage());
        
        return allStats;
    }
    
    /**
     * Creates a performance comparison chart between two players for a specific stat.
     * 
     * @param player1 the first player
     * @param player2 the second player
     * @param statName the statistic to compare
     * @return a map with player names and their stat values
     */
    public Map<String, Number> createComparisonChart(Player player1, Player player2, String statName) {
        Map<String, Number> chartData = new HashMap<>();
        
        if (player1 != null && player1.getSeasonStats() != null) {
            chartData.put(player1.getFullName(), getStatByName(player1.getSeasonStats(), statName));
        }
        
        if (player2 != null && player2.getSeasonStats() != null) {
            chartData.put(player2.getFullName(), getStatByName(player2.getSeasonStats(), statName));
        }
        
        return chartData;
    }
    
    /**
     * Creates a performance comparison chart showing multiple stats for two players side by side.
     * 
     * @param player1 the first player
     * @param player2 the second player
     * @return a map with player names to their stats maps
     */
    public Map<String, Map<String, Number>> createMultiStatComparisonChart(Player player1, Player player2) {
        Map<String, Map<String, Number>> comparisonData = new HashMap<>();
        
        if (player1 != null && player1.getSeasonStats() != null) {
            comparisonData.put(player1.getFullName(), getAllStats(player1.getSeasonStats()));
        }
        
        if (player2 != null && player2.getSeasonStats() != null) {
            comparisonData.put(player2.getFullName(), getAllStats(player2.getSeasonStats()));
        }
        
        return comparisonData;
    }
}