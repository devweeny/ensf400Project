package com.nhlstats.service;

import com.nhlstats.model.ComparisonResult;
import com.nhlstats.model.Player;
import com.nhlstats.model.PlayerStats;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

/**
 * Service for comparing statistics between NHL players.
 */
@Service
public class StatsComparisonService {
    
    /**
     * Compares multiple players and creates a comprehensive comparison result.
     * 
     * @param players the list of players to compare
     * @return the comparison results
     */
    public ComparisonResult comparePlayerStats(List<Player> players) {
        if (players == null || players.size() < 2) {
            throw new IllegalArgumentException("Need at least two players to compare");
        }
        
        ComparisonResult result = new ComparisonResult();
        result.setPlayers(players);
        
        // Compare raw stats
        Map<String, Map<String, Player>> categoryWinners = new HashMap<>();
        categoryWinners.put("goals", findBestInCategory(players, p -> p.getSeasonStats().getGoals()));
        categoryWinners.put("assists", findBestInCategory(players, p -> p.getSeasonStats().getAssists()));
        categoryWinners.put("points", findBestInCategory(players, p -> p.getSeasonStats().getPoints()));
        categoryWinners.put("plusMinus", findBestInCategory(players, p -> p.getSeasonStats().getPlusMinus()));
        categoryWinners.put("pointsPerGame", findBestInCategory(players, p -> p.getSeasonStats().getPointsPerGame()));
        categoryWinners.put("shotPercentage", findBestInCategory(players, p -> p.getSeasonStats().getShotPercentage()));
        
        result.setCategoryWinners(categoryWinners);
        
        // Calculate similarity scores between players
        Map<String, Double> similarityScores = calculateSimilarityScores(players);
        result.setSimilarityScores(similarityScores);
        
        // Calculate stat differences
        Map<String, Map<String, Double>> statDifferences = calculateStatDifferences(players);
        result.setStatDifferences(statDifferences);
        
        return result;
    }
    
    /**
     * Compares two players and returns a human-readable comparison summary.
     * 
     * @param player1 the first player
     * @param player2 the second player
     * @return a string summarizing the comparison
     */
    public String comparePlayers(Player player1, Player player2) {
        if (player1 == null || player2 == null || 
            player1.getSeasonStats() == null || player2.getSeasonStats() == null) {
            return "Cannot compare players due to missing stats";
        }
        
        PlayerStats stats1 = player1.getSeasonStats();
        PlayerStats stats2 = player2.getSeasonStats();
        
        StringBuilder comparison = new StringBuilder();
        comparison.append(player1.getFullName())
                 .append(" vs ")
                 .append(player2.getFullName())
                 .append(":\n");
        
        // Compare goals
        comparison.append("Goals: ")
                 .append(stats1.getGoals())
                 .append(" vs ")
                 .append(stats2.getGoals());
        if (stats1.getGoals() > stats2.getGoals()) {
            comparison.append(" (").append(player1.getFullName()).append(" leads by ")
                     .append(stats1.getGoals() - stats2.getGoals()).append(")");
        } else if (stats2.getGoals() > stats1.getGoals()) {
            comparison.append(" (").append(player2.getFullName()).append(" leads by ")
                     .append(stats2.getGoals() - stats1.getGoals()).append(")");
        } else {
            comparison.append(" (Tied)");
        }
        comparison.append("\n");
        
        // Compare assists
        comparison.append("Assists: ")
                 .append(stats1.getAssists())
                 .append(" vs ")
                 .append(stats2.getAssists());
        if (stats1.getAssists() > stats2.getAssists()) {
            comparison.append(" (").append(player1.getFullName()).append(" leads by ")
                     .append(stats1.getAssists() - stats2.getAssists()).append(")");
        } else if (stats2.getAssists() > stats1.getAssists()) {
            comparison.append(" (").append(player2.getFullName()).append(" leads by ")
                     .append(stats2.getAssists() - stats1.getAssists()).append(")");
        } else {
            comparison.append(" (Tied)");
        }
        comparison.append("\n");
        
        // Compare points
        comparison.append("Points: ")
                 .append(stats1.getPoints())
                 .append(" vs ")
                 .append(stats2.getPoints());
        if (stats1.getPoints() > stats2.getPoints()) {
            comparison.append(" (").append(player1.getFullName()).append(" leads by ")
                     .append(stats1.getPoints() - stats2.getPoints()).append(")");
        } else if (stats2.getPoints() > stats1.getPoints()) {
            comparison.append(" (").append(player2.getFullName()).append(" leads by ")
                     .append(stats2.getPoints() - stats1.getPoints()).append(")");
        } else {
            comparison.append(" (Tied)");
        }
        
        return comparison.toString();
    }
    
    /**
     * Returns a numeric comparison for various stats between two players.
     * 
     * @param player1 the first player
     * @param player2 the second player
     * @return a map containing the differences for each stat category
     */
    public Map<String, Integer> compareStats(Player player1, Player player2) {
        Map<String, Integer> differences = new HashMap<>();
        
        if (player1 == null || player2 == null || 
            player1.getSeasonStats() == null || player2.getSeasonStats() == null) {
            return differences;
        }
        
        PlayerStats stats1 = player1.getSeasonStats();
        PlayerStats stats2 = player2.getSeasonStats();
        
        differences.put("goals", stats1.getGoals() - stats2.getGoals());
        differences.put("assists", stats1.getAssists() - stats2.getAssists());
        differences.put("points", stats1.getPoints() - stats2.getPoints());
        differences.put("plusMinus", stats1.getPlusMinus() - stats2.getPlusMinus());
        differences.put("powerPlayGoals", stats1.getPowerPlayGoals() - stats2.getPowerPlayGoals());
        differences.put("powerPlayPoints", stats1.getPowerPlayPoints() - stats2.getPowerPlayPoints());
        differences.put("shots", stats1.getShots() - stats2.getShots());
        
        return differences;
    }
    
    /**
     * Ranks players by a specific stat.
     * 
     * @param players the list of players to rank
     * @param statName the stat name to rank by
     * @return a sorted list of players
     */
    public List<Player> rankPlayersByStat(List<Player> players, String statName) {
        if (players == null || players.isEmpty()) {
            return new ArrayList<>();
        }
        
        Comparator<Player> comparator;
        
        switch (statName.toLowerCase()) {
            case "goals":
                comparator = Comparator.comparing(p -> p.getSeasonStats().getGoals());
                break;
            case "assists":
                comparator = Comparator.comparing(p -> p.getSeasonStats().getAssists());
                break;
            case "points":
                comparator = Comparator.comparing(p -> p.getSeasonStats().getPoints());
                break;
            case "plusminus":
                comparator = Comparator.comparing(p -> p.getSeasonStats().getPlusMinus());
                break;
            case "pointspergame":
                comparator = Comparator.comparing(p -> p.getSeasonStats().getPointsPerGame());
                break;
            case "shotpercentage":
                comparator = Comparator.comparing(p -> p.getSeasonStats().getShotPercentage());
                break;
            default:
                comparator = Comparator.comparing(p -> p.getSeasonStats().getPoints());
                break;
        }
        
        // Sort in descending order (highest first)
        return players.stream()
                .filter(p -> p.getSeasonStats() != null)
                .sorted(comparator.reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * Finds the best player in a specific statistical category.
     * 
     * @param players the list of players to compare
     * @param statExtractor a function to extract the specific stat from a player
     * @return a map containing the winner and a flag for ties
     */
    private <T extends Comparable<T>> Map<String, Player> findBestInCategory(
            List<Player> players, 
            Function<Player, T> statExtractor) {
        
        Player bestPlayer = null;
        T bestValue = null;
        List<Player> ties = new ArrayList<>();
        
        for (Player player : players) {
            if (player.getSeasonStats() == null) {
                continue;
            }
            
            T currentValue = statExtractor.apply(player);
            
            if (bestValue == null || currentValue.compareTo(bestValue) > 0) {
                bestValue = currentValue;
                bestPlayer = player;
                ties.clear();
            } else if (currentValue.compareTo(bestValue) == 0) {
                ties.add(player);
            }
        }
        
        Map<String, Player> result = new HashMap<>();
        result.put("winner", bestPlayer);
        if (!ties.isEmpty()) {
            result.put("hasTies", ties.get(0)); // Just indicating there is a tie
        }
        
        return result;
    }
    
    /**
     * Calculates similarity scores between all pairs of players.
     * 
     * @param players the list of players to compare
     * @return a map of similarity scores between pairs of players
     */
    private Map<String, Double> calculateSimilarityScores(List<Player> players) {
        Map<String, Double> similarityScores = new HashMap<>();
        
        for (int i = 0; i < players.size(); i++) {
            for (int j = i + 1; j < players.size(); j++) {
                Player player1 = players.get(i);
                Player player2 = players.get(j);
                
                double similarity = calculatePlayerSimilarity(player1, player2);
                String key = player1.getId() + "-" + player2.getId();
                similarityScores.put(key, similarity);
            }
        }
        
        return similarityScores;
    }
    
    /**
     * Calculates a similarity score between two players.
     * 
     * @param player1 the first player
     * @param player2 the second player
     * @return a similarity score from 0 to 100
     */
    private double calculatePlayerSimilarity(Player player1, Player player2) {
        if (player1.getSeasonStats() == null || player2.getSeasonStats() == null) {
            return 0;
        }
        
        PlayerStats stats1 = player1.getSeasonStats();
        PlayerStats stats2 = player2.getSeasonStats();
        
        // Simple similarity based on normalized Euclidean distance across key stats
        double goalsDiff = normalize(stats1.getGoals(), stats2.getGoals());
        double assistsDiff = normalize(stats1.getAssists(), stats2.getAssists());
        double pointsDiff = normalize(stats1.getPoints(), stats2.getPoints());
        double ppgDiff = normalize(stats1.getPointsPerGame(), stats2.getPointsPerGame());
        double shotPctDiff = normalize(stats1.getShotPercentage(), stats2.getShotPercentage());
        
        // Calculate Euclidean distance
        double distanceSquared = 
            Math.pow(goalsDiff, 2) +
            Math.pow(assistsDiff, 2) +
            Math.pow(pointsDiff, 2) +
            Math.pow(ppgDiff, 2) +
            Math.pow(shotPctDiff, 2);
        
        double distance = Math.sqrt(distanceSquared);
        
        // Convert to similarity score (0-100%, where 100% is identical)
        // Using a sigmoid-like function to map distance to similarity
        return 100 * (1 - Math.tanh(distance / 5));
    }
    
    /**
     * Normalizes the difference between two values.
     */
    private double normalize(double val1, double val2) {
        if (val1 == 0 && val2 == 0) {
            return 0;
        }
        double max = Math.max(Math.abs(val1), Math.abs(val2));
        return Math.abs(val1 - val2) / max;
    }
    
    /**
     * Calculates detailed statistical differences between all pairs of players.
     */
    private Map<String, Map<String, Double>> calculateStatDifferences(List<Player> players) {
        Map<String, Map<String, Double>> allDifferences = new HashMap<>();
        
        for (int i = 0; i < players.size(); i++) {
            for (int j = i + 1; j < players.size(); j++) {
                Player player1 = players.get(i);
                Player player2 = players.get(j);
                
                if (player1.getSeasonStats() == null || player2.getSeasonStats() == null) {
                    continue;
                }
                
                PlayerStats stats1 = player1.getSeasonStats();
                PlayerStats stats2 = player2.getSeasonStats();
                
                Map<String, Double> differences = new HashMap<>();
                differences.put("goals", (double) (stats1.getGoals() - stats2.getGoals()));
                differences.put("assists", (double) (stats1.getAssists() - stats2.getAssists()));
                differences.put("points", (double) (stats1.getPoints() - stats2.getPoints()));
                differences.put("plusMinus", (double) (stats1.getPlusMinus() - stats2.getPlusMinus()));
                differences.put("pointsPerGame", stats1.getPointsPerGame() - stats2.getPointsPerGame());
                differences.put("shotPercentage", stats1.getShotPercentage() - stats2.getShotPercentage());
                
                String key = player1.getFullName() + "-" + player2.getFullName();
                allDifferences.put(key, differences);
            }
        }
        
        return allDifferences;
    }
}