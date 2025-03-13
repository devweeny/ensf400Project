package com.nhlstats.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an NHL player with their basic information and statistics.
 */
public class Player {
    private String id;
    private String fullName;
    private String teamName;
    private String position;
    private String imageUrl;
    private List<GameLog> gameLogs;
    private PlayerStats seasonStats;

    /**
     * Creates a new Player with the specified ID and name.
     * 
     * @param id       the player's ID
     * @param fullName the player's full name
     */
    public Player(String id, String fullName) {
        this.id = id;
        this.fullName = fullName;
        this.gameLogs = new ArrayList<>();
    }

    /**
     * Default constructor.
     */
    public Player() {
        this.gameLogs = new ArrayList<>();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<GameLog> getGameLogs() {
        return gameLogs;
    }

    public void setGameLogs(List<GameLog> gameLogs) {
        this.gameLogs = gameLogs;
    }

    public void addGameLog(GameLog gameLog) {
        this.gameLogs.add(gameLog);
    }

    public PlayerStats getSeasonStats() {
        return seasonStats;
    }

    public void setSeasonStats(PlayerStats seasonStats) {
        this.seasonStats = seasonStats;
    }

    /**
     * Calculates aggregate statistics based on the player's game logs.
     * 
     * @return the calculated season statistics
     */
    public PlayerStats calculateAggregateStats() {
        if (gameLogs.isEmpty()) {
            return new PlayerStats();
        }

        int games = gameLogs.size();
        int goals = 0;
        int assists = 0;
        int points = 0;
        int plusMinus = 0;
        int powerPlayGoals = 0;
        int powerPlayPoints = 0;
        int shots = 0;
        double timeOnIce = 0;

        for (GameLog gameLog : gameLogs) {
            goals += gameLog.getGoals();
            assists += gameLog.getAssists();
            points += gameLog.getPoints();
            plusMinus += gameLog.getPlusMinus();
            powerPlayGoals += gameLog.getPowerPlayGoals();
            powerPlayPoints += gameLog.getPowerPlayPoints();
            shots += gameLog.getShots();
            timeOnIce += gameLog.getTimeOnIceMinutes();
        }

        PlayerStats stats = new PlayerStats();
        stats.setGamesPlayed(games);
        stats.setGoals(goals);
        stats.setAssists(assists);
        stats.setPoints(points);
        stats.setPlusMinus(plusMinus);
        stats.setPowerPlayGoals(powerPlayGoals);
        stats.setPowerPlayPoints(powerPlayPoints);
        stats.setShots(shots);
        stats.setPointsPerGame((double) points / games);
        stats.setGoalsPerGame((double) goals / games);
        stats.setAssistsPerGame((double) assists / games);
        stats.setShotPercentage(shots > 0 ? (double) goals / shots * 100 : 0);
        stats.setAverageTimeOnIce(timeOnIce / games);

        this.seasonStats = stats;
        return stats;
    }
    
    @Override
    public String toString() {
        return fullName + " (" + teamName + ", " + position + ")";
    }
}