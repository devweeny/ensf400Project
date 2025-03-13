package com.nhlstats.model;

/**
 * Represents aggregated statistics for a player over a period of time.
 */
public class PlayerStats {
    private int gamesPlayed;
    private int goals;
    private int assists;
    private int points;
    private int plusMinus;
    private int powerPlayGoals;
    private int powerPlayPoints;
    private int shots;
    private double pointsPerGame;
    private double goalsPerGame;
    private double assistsPerGame;
    private double shotPercentage;
    private double averageTimeOnIce;

    /**
     * Default constructor.
     */
    public PlayerStats() {
    }

    /**
     * Constructor with all fields.
     */
    public PlayerStats(int gamesPlayed, int goals, int assists, int points, int plusMinus, 
                      int powerPlayGoals, int powerPlayPoints, int shots, 
                      double pointsPerGame, double goalsPerGame, double assistsPerGame, 
                      double shotPercentage, double averageTimeOnIce) {
        this.gamesPlayed = gamesPlayed;
        this.goals = goals;
        this.assists = assists;
        this.points = points;
        this.plusMinus = plusMinus;
        this.powerPlayGoals = powerPlayGoals;
        this.powerPlayPoints = powerPlayPoints;
        this.shots = shots;
        this.pointsPerGame = pointsPerGame;
        this.goalsPerGame = goalsPerGame;
        this.assistsPerGame = assistsPerGame;
        this.shotPercentage = shotPercentage;
        this.averageTimeOnIce = averageTimeOnIce;
    }

    // Getters and setters
    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getGoals() {
        return goals;
    }

    public void setGoals(int goals) {
        this.goals = goals;
    }

    public int getAssists() {
        return assists;
    }

    public void setAssists(int assists) {
        this.assists = assists;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getPlusMinus() {
        return plusMinus;
    }

    public void setPlusMinus(int plusMinus) {
        this.plusMinus = plusMinus;
    }

    public int getPowerPlayGoals() {
        return powerPlayGoals;
    }

    public void setPowerPlayGoals(int powerPlayGoals) {
        this.powerPlayGoals = powerPlayGoals;
    }

    public int getPowerPlayPoints() {
        return powerPlayPoints;
    }

    public void setPowerPlayPoints(int powerPlayPoints) {
        this.powerPlayPoints = powerPlayPoints;
    }

    public int getShots() {
        return shots;
    }

    public void setShots(int shots) {
        this.shots = shots;
    }

    public double getPointsPerGame() {
        return pointsPerGame;
    }

    public void setPointsPerGame(double pointsPerGame) {
        this.pointsPerGame = pointsPerGame;
    }

    public double getGoalsPerGame() {
        return goalsPerGame;
    }

    public void setGoalsPerGame(double goalsPerGame) {
        this.goalsPerGame = goalsPerGame;
    }

    public double getAssistsPerGame() {
        return assistsPerGame;
    }

    public void setAssistsPerGame(double assistsPerGame) {
        this.assistsPerGame = assistsPerGame;
    }

    public double getShotPercentage() {
        return shotPercentage;
    }

    public void setShotPercentage(double shotPercentage) {
        this.shotPercentage = shotPercentage;
    }

    public double getAverageTimeOnIce() {
        return averageTimeOnIce;
    }

    public void setAverageTimeOnIce(double averageTimeOnIce) {
        this.averageTimeOnIce = averageTimeOnIce;
    }
    
    @Override
    public String toString() {
        return String.format(
            "GP: %d, G: %d, A: %d, P: %d, +/-: %d, PPG: %d, PPP: %d, S: %d, P/GP: %.2f, S%%: %.1f%%",
            gamesPlayed, goals, assists, points, plusMinus, powerPlayGoals, powerPlayPoints, 
            shots, pointsPerGame, shotPercentage
        );
    }
}