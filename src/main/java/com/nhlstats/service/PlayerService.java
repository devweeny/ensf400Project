package com.nhlstats.service;

import com.nhlstats.model.Player;
import com.nhlstats.model.GameLog;
import com.nhlstats.model.PlayerStats;
import com.nhlstats.api.NhlApiClient;
import com.nhlstats.api.EndpointUrls;

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
        // First try to get player info from the main endpoint
        JsonNode playerInfo = apiClient.getPlayerInfo(playerId);
        
        System.out.println("Player info structure: " + playerInfo);
        
        // Check if we got an error response
        Player player;
        if (apiClient.isErrorResponse(playerInfo)) {
            System.out.println("Error fetching player info: " + apiClient.getErrorMessage(playerInfo));
            
            // Try alternative API endpoint - the roster endpoint
            try {
                // We need to find which team the player is on
                // For now, create a basic player and try to fetch game logs
                player = createPlayerFromInfo(playerId, playerInfo);
                
                // Try to add more info by using the career stats endpoint
                JsonNode careerNode = apiClient.getPlayerCareerStats(playerId);
                if (!apiClient.isErrorResponse(careerNode)) {
                    enhancePlayerInfo(player, careerNode);
                }
            } catch (Exception e) {
                System.out.println("Failed to get alternative player info: " + e.getMessage());
                player = createPlayerFromInfo(playerId, playerInfo);
            }
        } else {
            player = createPlayerFromInfo(playerId, playerInfo);
        }
        
        JsonNode gameLogData = apiClient.getPlayerGameLog(playerId, season, gameType);
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
        List<CompletableFuture<Player>> futures = playerIds.stream()
                .map(id -> getPlayerWithStatsAsync(id, season, gameType))
                .collect(Collectors.toList());
        
        return futures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        System.err.println("Error getting player: " + e.getMessage());
                        return null;
                    }
                })
                .filter(player -> player != null)
                .collect(Collectors.toList());
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
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getPlayerWithStats(playerId, season, gameType);
            } catch (Exception e) {
                throw new RuntimeException("Error fetching player data: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Creates a Player object from the API player info.
     * 
     * @param playerId the player's ID
     * @param playerInfo the JSON player info from the API
     * @return a Player object
     */
    private Player createPlayerFromInfo(String playerId, JsonNode playerInfo) {
        // Check if we received an error response or empty data
        if (apiClient.isErrorResponse(playerInfo) || playerInfo.isEmpty()) {
            // Try to search for the player ID in the NHL public database
            System.out.println("Creating basic player with ID: " + playerId);
            
            // Create a basic player with just the ID
            String playerName = getPlayerNameFromStaticMapping(playerId);
            if (playerName == null) {
                playerName = "Player " + playerId;
            }
            
            Player player = new Player(playerId, playerName);
            
            // Set defaults 
            String teamName = "NHL";
            String position = "Unknown";
            
            player.setTeamName(teamName);
            player.setPosition(position);
            
            // Set image URL
            player.setImageUrl(buildPlayerImageUrl(playerId));
            
            System.out.println("Created player: " + player.getFullName() + ", Team: " + player.getTeamName() + 
                           ", Position: " + player.getPosition() + ", Image URL: " + player.getImageUrl());
            
            return player;
        }
        
        // First, try to get name from different possible locations in the JSON
        String fullName = null;
        
        // New API structure - check the fullName field directly
        if (playerInfo.has("fullName")) {
            fullName = playerInfo.get("fullName").asText();
        } 
        // Try firstname/lastname combo
        else if (playerInfo.has("firstName") && playerInfo.has("lastName")) {
            fullName = playerInfo.get("firstName").asText() + " " + playerInfo.get("lastName").asText();
        }
        // Check if name is in a nested property (person.fullName in older API)
        else if (playerInfo.has("person") && playerInfo.path("person").has("fullName")) {
            fullName = playerInfo.path("person").get("fullName").asText();
        }
        // Try old API structure with just "name"
        else if (playerInfo.has("name")) {
            fullName = playerInfo.get("name").asText();
        }
        // If all else fails, use the player ID or lookup
        else {
            fullName = getPlayerNameFromStaticMapping(playerId);
            if (fullName == null) {
                fullName = "Player " + playerId;
            }
        }
        
        // Get team name - check multiple possible paths
        String teamName = "";
        if (playerInfo.has("currentTeam") && playerInfo.path("currentTeam").has("name")) {
            teamName = playerInfo.path("currentTeam").get("name").asText();
        } 
        else if (playerInfo.has("currentTeam") && playerInfo.path("currentTeam").has("default")) {
            teamName = playerInfo.path("currentTeam").path("name").path("default").asText();
        }
        else if (playerInfo.has("teamName")) {
            teamName = playerInfo.get("teamName").asText();
        }
        else if (playerInfo.has("team") && playerInfo.path("team").has("name")) {
            teamName = playerInfo.path("team").get("name").asText();
        }
        
        // Get position
        String position = "";
        if (playerInfo.has("position")) {
            // Try different possible structures
            if (playerInfo.path("position").isTextual()) {
                position = playerInfo.path("position").asText();
            } 
            else if (playerInfo.path("position").has("name")) {
                position = playerInfo.path("position").get("name").asText();
            }
            else if (playerInfo.path("position").has("code")) {
                position = playerInfo.path("position").get("code").asText();
            }
        }
        // Try another path
        else if (playerInfo.has("primaryPosition") && playerInfo.path("primaryPosition").has("name")) {
            position = playerInfo.path("primaryPosition").get("name").asText();
        }
        
        // Create the player with the info we found
        Player player = new Player(playerId, fullName);
        player.setTeamName(teamName);
        player.setPosition(position);
        
        // Try different image URL formats since the NHL API changes periodically
        String imageUrl = buildPlayerImageUrl(playerId);
        player.setImageUrl(imageUrl);
        
        System.out.println("Created player: " + player.getFullName() + ", Team: " + player.getTeamName() + 
                           ", Position: " + player.getPosition() + ", Image URL: " + player.getImageUrl());
        
        return player;
    }
    
    /**
     * Try to add more player info from career stats endpoint
     */
    private void enhancePlayerInfo(Player player, JsonNode careerNode) {
        try {
            if (careerNode.has("info") && !careerNode.path("info").isNull()) {
                JsonNode info = careerNode.path("info");
                
                // Try to get full name if we don't have it
                if (player.getFullName().startsWith("Player ") && info.has("fullName")) {
                    player.setFullName(info.get("fullName").asText());
                }
                
                // Try to get position
                if ((player.getPosition() == null || player.getPosition().isEmpty() || 
                    player.getPosition().equals("Unknown")) && info.has("positionCode")) {
                    player.setPosition(info.get("positionCode").asText());
                }
                
                // Try to get team name
                if ((player.getTeamName() == null || player.getTeamName().isEmpty() || 
                    player.getTeamName().equals("NHL")) && info.has("teamName")) {
                    player.setTeamName(info.get("teamName").path("default").asText());
                }
            }
        } catch (Exception e) {
            System.out.println("Error enhancing player info: " + e.getMessage());
        }
    }

    /**
     * Builds an image URL for the player based on their ID.
     * Tries different formats that NHL API has used.
     */
    private String buildPlayerImageUrl(String playerId) {
        // These are possible formats - we'll try the most recent first
        return "https://assets.nhle.com/mugs/nhl/latest/" + playerId + ".png";
        
        // Older format
        // return "https://cms.nhl.bamgrid.com/images/headshots/current/168x168/" + playerId + ".jpg";
    }

    /**
     * Gets a player name from a static mapping for well-known players.
     * This is a fallback for when the API doesn't return clear data.
     */
    private String getPlayerNameFromStaticMapping(String playerId) {
        // Add some common player IDs here for fallback
        switch (playerId) {
            case "8478402": return "Connor McDavid";
            case "8477492": return "Nathan MacKinnon";
            case "8479318": return "Auston Matthews";
            case "8471675": return "Sidney Crosby";
            case "8477934": return "Leon Draisaitl";
            case "8477956": return "David Pastrnak";
            case "8478483": return "Jack Eichel";
            case "8478420": return "Mitchell Marner";
            case "8480801": return "Cale Makar";
            case "8470621": return "Alex Ovechkin";
            case "8471214": return "Nicklas Backstrom";
            case "8471698": return "Evgeni Malkin";
            case "8474141": return "Steven Stamkos";
            case "8471215": return "Patrice Bergeron";
            case "8471701": return "Jonathan Toews";
            case "8471716": return "Anze Kopitar";
            case "8474564": return "John Tavares";
            case "8474578": return "Claude Giroux";
            case "8471695": return "Carey Price";
            case "8479407": return "Matthew Tkachuk";
            case "8478427": return "Mikko Rantanen";
            case "8476883": return "Nikita Kucherov";
            case "8473565": return "Patrick Kane";
            case "8471685": return "Alexander Radulov";
            case "8475745": return "Vladimir Tarasenko";
            case "8475744": return "Mark Stone";
            case "8475726": return "Mark Scheifele";
            case "8471707": return "Brad Marchand";
            case "8475158": return "Roman Josi";
            case "8474600": return "Drew Doughty";
            case "8474715": return "Brent Burns";
            case "8471724": return "Claude Giroux";
            
            default: return null;
        }
    }

    /**
     * Populates a player's game logs from the API game log data.
     * 
     * @param player the player to populate game logs for
     * @param gameLogData the JSON game log data from the API
     */
    private void populatePlayerGameLogs(Player player, JsonNode gameLogData) {
        // Check for errors or empty data
        if (apiClient.isErrorResponse(gameLogData)) {
            System.out.println("Error fetching game logs: " + apiClient.getErrorMessage(gameLogData));
            return;
        }
        
        // The structure has changed - find the game logs in the JSON
        JsonNode gameLogArray = null;
        
        if (gameLogData.has("gameLog") && !gameLogData.path("gameLog").isNull()) {
            gameLogArray = gameLogData.path("gameLog");
        } else if (gameLogData.isArray()) {
            gameLogArray = gameLogData;
        } else {
            System.out.println("Could not find game logs in the response");
            return;
        }
        
        if (gameLogArray == null || gameLogArray.isEmpty()) {
            System.out.println("No game logs found for player: " + player.getFullName());
            return;
        }
        
        List<GameLog> gameLogs = new ArrayList<>();
        
        for (JsonNode gameLogNode : gameLogArray) {
            GameLog gameLog = new GameLog();
            
            try {
                // Try to parse game ID
                if (gameLogNode.has("gameId")) {
                    gameLog.setGameId(gameLogNode.path("gameId").asLong());
                } else if (gameLogNode.has("game") && gameLogNode.path("game").has("id")) {
                    gameLog.setGameId(gameLogNode.path("game").path("id").asLong());
                }
                
                // Team abbreviation
                if (gameLogNode.has("teamAbbrev")) {
                    gameLog.setTeamAbbrev(gameLogNode.path("teamAbbrev").asText());
                } else if (gameLogNode.has("team") && gameLogNode.path("team").has("abbrev")) {
                    gameLog.setTeamAbbrev(gameLogNode.path("team").path("abbrev").asText());
                }
                
                // Home/Road flag
                if (gameLogNode.has("homeRoadFlag")) {
                    gameLog.setHomeRoadFlag(gameLogNode.path("homeRoadFlag").asText());
                } else if (gameLogNode.has("isHome")) {
                    gameLog.setHomeRoadFlag(gameLogNode.path("isHome").asBoolean() ? "H" : "R");
                }
                
                // Game date
                String dateStr = null;
                if (gameLogNode.has("gameDate")) {
                    dateStr = gameLogNode.path("gameDate").asText();
                } else if (gameLogNode.has("date")) {
                    dateStr = gameLogNode.path("date").asText();
                } else if (gameLogNode.has("game") && gameLogNode.path("game").has("date")) {
                    dateStr = gameLogNode.path("game").path("date").asText();
                }
                
                try {
                    if (dateStr != null) {
                        gameLog.setGameDate(LocalDate.parse(dateStr.substring(0, 10), dateFormatter));
                    } else {
                        gameLog.setGameDate(LocalDate.now()); // Default to current date
                    }
                } catch (Exception e) {
                    // Handle date parsing errors
                    System.out.println("Error parsing date: " + e.getMessage());
                    gameLog.setGameDate(LocalDate.now()); // Default to current date
                }
                
                // Stats
                JsonNode statsNode = gameLogNode.has("stat") ? gameLogNode.path("stat") : gameLogNode;
                
                gameLog.setGoals(getIntOrZero(statsNode, "goals"));
                gameLog.setAssists(getIntOrZero(statsNode, "assists"));
                gameLog.setPoints(getIntOrZero(statsNode, "points"));
                gameLog.setPlusMinus(getIntOrZero(statsNode, "plusMinus"));
                gameLog.setPowerPlayGoals(getIntOrZero(statsNode, "powerPlayGoals"));
                gameLog.setPowerPlayPoints(getIntOrZero(statsNode, "powerPlayPoints"));
                gameLog.setGameWinningGoals(getIntOrZero(statsNode, "gameWinningGoals"));
                gameLog.setOtGoals(getIntOrZero(statsNode, "otGoals"));
                gameLog.setShots(getIntOrZero(statsNode, "shots"));
                gameLog.setShifts(getIntOrZero(statsNode, "shifts"));
                
                // Time on ice
                if (statsNode.has("toi")) {
                    gameLog.setTimeOnIce(statsNode.path("toi").asText());
                } else if (statsNode.has("timeOnIce")) {
                    gameLog.setTimeOnIce(statsNode.path("timeOnIce").asText());
                } else {
                    gameLog.setTimeOnIce("00:00");
                }
                
                // Opponent
                if (gameLogNode.has("opponentAbbrev")) {
                    gameLog.setOpponentAbbrev(gameLogNode.path("opponentAbbrev").asText());
                } else if (gameLogNode.has("opponent") && gameLogNode.path("opponent").has("abbrev")) {
                    gameLog.setOpponentAbbrev(gameLogNode.path("opponent").path("abbrev").asText());
                }
                
                gameLogs.add(gameLog);
            } catch (Exception e) {
                System.out.println("Error parsing game log: " + e.getMessage());
            }
        }
        
        player.setGameLogs(gameLogs);
    }
    
    private int getIntOrZero(JsonNode node, String fieldName) {
        if (node.has(fieldName) && !node.path(fieldName).isNull()) {
            try {
                return node.path(fieldName).asInt();
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }
}