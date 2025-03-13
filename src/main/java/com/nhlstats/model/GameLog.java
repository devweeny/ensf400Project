package com.nhlstats.model;

import java.time.LocalDate;

/**
 * Represents a player's statistics for a single game.
 */
public class GameLog {
    private long gameId;
    private String teamAbbrev;
    private String homeRoadFlag;
    private LocalDate gameDate;
    private int goals;
    private int assists;
    private int points;
    private int plusMinus;
    private int powerPlayGoals;
    private int powerPlayPoints;
    private int gameWinningGoals;
    private int otGoals;
    private int shots;
    private int shifts;
    private String timeOnIce;
    private double timeOnIceMinutes;
    private String opponentAbbrev;

    /**
     * Default constructor.
     */
    public GameLog() {
    }

    // Getters and setters
    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public String getTeamAbbrev() {
        return teamAbbrev;
    }

    public void setTeamAbbrev(String teamAbbrev) {
        this.teamAbbrev = teamAbbrev;
    }

    public String getHomeRoadFlag() {
        return homeRoadFlag;
    }

    public void setHomeRoadFlag(String homeRoadFlag) {
        this.homeRoadFlag = homeRoadFlag;
    }

    public LocalDate getGameDate() {
        return gameDate;
    }

    public void setGameDate(LocalDate gameDate) {
        this.gameDate = gameDate;
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

    public int getGameWinningGoals() {
        return gameWinningGoals;
    }

    public void setGameWinningGoals(int gameWinningGoals) {
        this.gameWinningGoals = gameWinningGoals;
    }

    public int getOtGoals() {
        return otGoals;
    }

    public void setOtGoals(int otGoals) {
        this.otGoals = otGoals;
    }

    public int getShots() {
        return shots;
    }

    public void setShots(int shots) {
        this.shots = shots;
    }

    public int getShifts() {
        return shifts;
    }

    public void setShifts(int shifts) {
        this.shifts = shifts;
    }

    public String getTimeOnIce() {
        return timeOnIce;
    }

    public void setTimeOnIce(String timeOnIce) {
        this.timeOnIce = timeOnIce;
        this.timeOnIceMinutes = parseTimeOnIce(timeOnIce);
    }

    public double getTimeOnIceMinutes() {
        return timeOnIceMinutes;
    }

    public String getOpponentAbbrev() {
        return opponentAbbrev;
    }

    public void setOpponentAbbrev(String opponentAbbrev) {
        this.opponentAbbrev = opponentAbbrev;
    }

    /**
     * Parses a time string in format "MM:SS" to minutes (as a decimal number).
     * 
     * @param timeOnIce the time string in format "MM:SS"
     * @return the time in minutes as a decimal number
     */
    private double parseTimeOnIce(String timeOnIce) {
        if (timeOnIce == null || timeOnIce.isEmpty()) {
            return 0;
        }

        String[] parts = timeOnIce.split(":");
        if (parts.length != 2) {
            return 0;
        }

        try {
            int minutes = Integer.parseInt(parts[0]);
            int seconds = Integer.parseInt(parts[1]);
            return minutes + (seconds / 60.0);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    @Override
    public String toString() {
        return "Game " + gameId + " (" + gameDate + "): " + goals + "G " + assists + "A " + points + "P";
    }
}