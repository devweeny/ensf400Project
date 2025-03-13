package com.nhlstats.model;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents the results of comparing multiple players' statistics.
 */
public class ComparisonResult {
    private List<Player> players;
    private Map<String, Map<String, Player>> categoryWinners;
    private Map<String, Double> similarityScores;
    private Map<String, Map<String, Double>> statDifferences;
    
    /**
     * Default constructor.
     */
    public ComparisonResult() {
        this.categoryWinners = new HashMap<>();
        this.similarityScores = new HashMap<>();
        this.statDifferences = new HashMap<>();
    }
    
    /**
     * Gets the players being compared.
     * 
     * @return the list of players
     */
    public List<Player> getPlayers() {
        return players;
    }
    
    /**
     * Sets the players being compared.
     * 
     * @param players the list of players
     */
    public void setPlayers(List<Player> players) {
        this.players = players;
    }
    
    /**
     * Gets the category winners, mapping category names to information about the winner.
     * For each category, the map contains at minimum a "winner" key with the Player who
     * leads in that category. If there is a tie, a "hasTies" key is present.
     * 
     * @return the map of category winners
     */
    public Map<String, Map<String, Player>> getCategoryWinners() {
        return categoryWinners;
    }
    
    /**
     * Sets the category winners.
     * 
     * @param categoryWinners the map of category winners
     */
    public void setCategoryWinners(Map<String, Map<String, Player>> categoryWinners) {
        this.categoryWinners = categoryWinners;
    }
    
    /**
     * Gets the similarity scores between pairs of players.
     * The keys are formatted as "playerId1-playerId2".
     * 
     * @return the map of similarity scores
     */
    public Map<String, Double> getSimilarityScores() {
        return similarityScores;
    }
    
    /**
     * Sets the similarity scores between pairs of players.
     * 
     * @param similarityScores the map of similarity scores
     */
    public void setSimilarityScores(Map<String, Double> similarityScores) {
        this.similarityScores = similarityScores;
    }
    
    /**
     * Gets the statistical differences between pairs of players for each stat category.
     * The outer map keys are formatted as "playerId1-playerId2".
     * The inner map keys are stat categories like "goals", "assists", etc.
     * 
     * @return the map of statistical differences
     */
    public Map<String, Map<String, Double>> getStatDifferences() {
        return statDifferences;
    }
    
    /**
     * Sets the statistical differences between pairs of players.
     * 
     * @param statDifferences the map of statistical differences
     */
    public void setStatDifferences(Map<String, Map<String, Double>> statDifferences) {
        this.statDifferences = statDifferences;
    }
    
    /**
     * Adds a winner for a specific category.
     * 
     * @param category the statistical category
     * @param winner the player who leads in that category
     * @param hasTie whether there is a tie for the lead
     */
    public void addCategoryWinner(String category, Player winner, boolean hasTie) {
        Map<String, Player> winnerInfo = new HashMap<>();
        winnerInfo.put("winner", winner);
        if (hasTie) {
            winnerInfo.put("hasTies", winner); // Just a flag
        }
        categoryWinners.put(category, winnerInfo);
    }
    
    /**
     * Adds a similarity score between two players.
     * 
     * @param player1Id the ID of the first player
     * @param player2Id the ID of the second player
     * @param score the similarity score (0-100)
     */
    public void addSimilarityScore(String player1Id, String player2Id, double score) {
        String key = player1Id + "-" + player2Id;
        similarityScores.put(key, score);
    }
    
    /**
     * Adds statistical differences between two players.
     * 
     * @param player1Id the ID of the first player
     * @param player2Id the ID of the second player
     * @param differences the map of differences by category
     */
    public void addStatDifferences(String player1Id, String player2Id, Map<String, Double> differences) {
        String key = player1Id + "-" + player2Id;
        statDifferences.put(key, differences);
    }
}