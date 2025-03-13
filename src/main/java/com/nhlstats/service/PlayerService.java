package com.nhlstats.service;

import com.nhlstats.model.Player;
import com.nhlstats.model.GameLog;
import com.nhlstats.model.PlayerStats;
import com.nhlstats.api.NhlApiClient;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Service for fetching and processing player data from the NHL API.
 */
public class PlayerService {
    private final NhlApiClient apiClient;
    private final DateTimeFormatter dateFormatter;

    /**
     * Creates a new player service with the NHL API client.
     */
    public PlayerService() {
        this.apiClient = new NhlApiClient();
        this.dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    }

    /**
     * Gets a player with their stats for the specified season and game type.
     * 
     * @param playerId the player's ID
     * @param season the season code (e.g., "20232024")
     * @param gameType the game type code (e.g., "2" for regular season)
     * @return a Player object with populated game logs and season stats
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     */
    public Player getPlayerWithStats(String playerId, String season, String gameType) 
            throws IOException, InterruptedException {
        JsonNode playerInfo = apiClient.getPlayerInfo(playerId);
        JsonNode gameLogData = apiClient.getPlayerGameLog(playerId, season, gameType);
        
        Player player = createPlayerFromInfo(playerId, playerInfo);
        populatePlayerGameLogs(player, gameLogData);
        player.calculateAggregateStats();
        
        return player;
    }

    /**
     * Gets multiple players with their stats for the specified season and game type.
     * Uses parallel requests for efficiency.
     * 
     * @param playerIds the list of player IDs
     * @param season the season code (e.g., "20232024")
     * @param gameType the game type code (e.g., "2" for regular season)
     * @return a list of Player objects with populated game logs and season stats
     */
    public List<Player> getMultiplePlayersWithStats(List<String> playerIds, String season, String gameType) {
        List<CompletableFuture<Player>> playerFutures = playerIds.stream()
            .map(playerId -> getPlayerWithStatsAsync(playerId, season, gameType))
            .collect(Collectors.toList());

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
            playerFutures.toArray(new CompletableFuture[0])
        );

        return allFutures.thenApply(v -> 
            playerFutures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList())
        ).join();
    }

    /**
     * Gets a player by ID.
     * 
     * @param playerId the player's ID
     * @return the Player object, or null if not found
     */
    public Player getPlayerById(String playerId) {
        try {
            JsonNode playerInfo = apiClient.getPlayerInfo(playerId);
            return createPlayerFromInfo(playerId, playerInfo);
        } catch (Exception e) {
            System.err.println("Error fetching player with ID: " + playerId);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Search players by partial name match.
     * 
     * @param query the search query
     * @return a list of matching players
     */
    public List<Player> searchPlayersByName(String query) {
        // In a real implementation, this would call the NHL API's search endpoint
        // For now, return an empty list
        return new ArrayList<>();
    }

    /**
     * Filter players by position.
     * 
     * @param players the list of players to filter
     * @param position the position to filter by
     * @return a filtered list of players
     */
    public List<Player> filterPlayersByPosition(List<Player> players, String position) {
        return players.stream()
                .filter(player -> player.getPosition() != null && 
                        player.getPosition().equalsIgnoreCase(position))
                .collect(Collectors.toList());
    }

    /**
     * Asynchronously gets a player with their stats.
     * 
     * @param playerId the player's ID
     * @param season the season code
     * @param gameType the game type code
     * @return a future that will complete with the player data
     */
    private CompletableFuture<Player> getPlayerWithStatsAsync(String playerId, String season, String gameType) {
        try {
            JsonNode playerInfo = apiClient.getPlayerInfo(playerId);
            Player player = createPlayerFromInfo(playerId, playerInfo);
            
            return apiClient.getPlayerGameLogAsync(playerId, season, gameType)
                .thenApply(gameLogData -> {
                    populatePlayerGameLogs(player, gameLogData);
                    player.calculateAggregateStats();
                    return player;
                });
        } catch (IOException | InterruptedException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Creates a Player object from the API player info.
     * 
     * @param playerId the player's ID
     * @param playerInfo the JSON player info from the API
     * @return a Player object
     */
    private Player createPlayerFromInfo(String playerId, JsonNode playerInfo) {
        String fullName = playerInfo.path("fullName").asText("Unknown Player");
        String teamName = playerInfo.path("currentTeam").path("name").path("default").asText("");
        String position = playerInfo.path("position").asText("");
        
        Player player = new Player(playerId, fullName);
        player.setTeamName(teamName);
        player.setPosition(position);
        
        // Set player image URL
        String imageUrl = "https://cms.nhl.bamgrid.com/images/headshots/current/168x168/" + 
                         playerId + ".jpg";
        player.setImageUrl(imageUrl);
        
        return player;
    }

    /**
     * Populates a player's game logs from the API game log data.
     * 
     * @param player the player to populate game logs for
     * @param gameLogData the JSON game log data from the API
     */
    private void populatePlayerGameLogs(Player player, JsonNode gameLogData) {
        JsonNode gameLogArray = gameLogData.path("gameLog");
        List<GameLog> gameLogs = new ArrayList<>();
        
        for (JsonNode gameLogNode : gameLogArray) {
            GameLog gameLog = new GameLog();
            gameLog.setGameId(gameLogNode.path("gameId").asLong());
            gameLog.setTeamAbbrev(gameLogNode.path("teamAbbrev").asText());
            gameLog.setHomeRoadFlag(gameLogNode.path("homeRoadFlag").asText());
            
            String dateStr = gameLogNode.path("gameDate").asText();
            gameLog.setGameDate(LocalDate.parse(dateStr, dateFormatter));
            
            gameLog.setGoals(gameLogNode.path("goals").asInt());
            gameLog.setAssists(gameLogNode.path("assists").asInt());
            gameLog.setPoints(gameLogNode.path("points").asInt());
            gameLog.setPlusMinus(gameLogNode.path("plusMinus").asInt());
            gameLog.setPowerPlayGoals(gameLogNode.path("powerPlayGoals").asInt());
            gameLog.setPowerPlayPoints(gameLogNode.path("powerPlayPoints").asInt());
            gameLog.setGameWinningGoals(gameLogNode.path("gameWinningGoals").asInt());
            gameLog.setOtGoals(gameLogNode.path("otGoals").asInt());
            gameLog.setShots(gameLogNode.path("shots").asInt());
            gameLog.setShifts(gameLogNode.path("shifts").asInt());
            gameLog.setTimeOnIce(gameLogNode.path("toi").asText());
            gameLog.setOpponentAbbrev(gameLogNode.path("opponentAbbrev").asText());
            
            gameLogs.add(gameLog);
        }
        
        player.setGameLogs(gameLogs);
    }
}