package com.nhlstats.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Client for interacting with the NHL API.
 * Provides methods to fetch player information, game logs, and other NHL data.
 */
public class NhlApiClient {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    /**
     * Creates a new NHL API client with default configuration.
     */
    public NhlApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule()); // For handling Java 8 date/time types
    }
    
    /**
     * Gets information about a player.
     * 
     * @param playerId the player's ID
     * @return JSON data containing player information
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     */
    public JsonNode getPlayerInfo(String playerId) throws IOException, InterruptedException {
        String url = EndpointUrls.buildUrl(EndpointUrls.PLAYER_INFO, playerId);
        System.out.println(url);
        return fetchJsonData(url);
    }
    
    /**
     * Gets a player's game log for a specific season and game type.
     * 
     * @param playerId the player's ID
     * @param season the season code (e.g., "20232024" for 2023-2024)
     * @param gameType the game type code (e.g., "2" for regular season)
     * @return JSON data containing the player's game log
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     */
    public JsonNode getPlayerGameLog(String playerId, String season, String gameType) throws IOException, InterruptedException {
        String url = EndpointUrls.buildUrl(EndpointUrls.PLAYER_GAME_LOG, playerId, season, gameType);
        return fetchJsonData(url);
    }
    
    /**
     * Gets a player's career statistics.
     * 
     * @param playerId the player's ID
     * @return JSON data containing the player's career statistics
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     */
    public JsonNode getPlayerCareerStats(String playerId) throws IOException, InterruptedException {
        String url = EndpointUrls.buildUrl(EndpointUrls.PLAYER_CAREER_STATS, playerId);
        return fetchJsonData(url);
    }
    
    /**
     * Gets current NHL standings.
     * 
     * @return JSON data containing current NHL standings
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     */
    public JsonNode getStandings() throws IOException, InterruptedException {
        String url = EndpointUrls.buildUrl(EndpointUrls.STANDINGS);
        return fetchJsonData(url);
    }
    
    /**
     * Gets information about a team.
     * 
     * @param teamId the team's ID
     * @return JSON data containing team information
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     */
    public JsonNode getTeamInfo(String teamId) throws IOException, InterruptedException {
        String url = EndpointUrls.buildUrl(EndpointUrls.TEAM_INFO, teamId);
        return fetchJsonData(url);
    }
    
    /**
     * Gets a team's roster for a specific season.
     * 
     * @param teamId the team's ID
     * @param season the season code (e.g., "20232024" for 2023-2024)
     * @return JSON data containing the team's roster
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     */
    public JsonNode getTeamRoster(String teamId, String season) throws IOException, InterruptedException {
        String url = EndpointUrls.buildUrl(EndpointUrls.TEAM_ROSTER, teamId, season);
        return fetchJsonData(url);
    }
    
    /**
     * Gets asynchronous information about a player.
     * 
     * @param playerId the player's ID
     * @return a future that will complete with the player information
     */
    public CompletableFuture<JsonNode> getPlayerInfoAsync(String playerId) {
        String url = EndpointUrls.buildUrl(EndpointUrls.PLAYER_INFO, playerId);
        return fetchJsonDataAsync(url);
    }
    
    /**
     * Gets asynchronous player's game log for a specific season and game type.
     * 
     * @param playerId the player's ID
     * @param season the season code (e.g., "20232024" for 2023-2024)
     * @param gameType the game type code (e.g., "2" for regular season)
     * @return a future that will complete with the player's game log
     */
    public CompletableFuture<JsonNode> getPlayerGameLogAsync(String playerId, String season, String gameType) {
        String url = EndpointUrls.buildUrl(EndpointUrls.PLAYER_GAME_LOG, playerId, season, gameType);
        return fetchJsonDataAsync(url);
    }
    
    /**
     * Fetches JSON data from the specified URL.
     * 
     * @param url the URL to fetch data from
     * @return the parsed JSON response
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the operation is interrupted
     */
    private JsonNode fetchJsonData(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Check for HTTP errors
        int statusCode = response.statusCode();
        if (statusCode >= 400) {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("error", true);
            errorNode.put("status", statusCode);
            errorNode.put("message", "HTTP error " + statusCode);
            return errorNode;
        }
        
        return objectMapper.readTree(response.body());
    }
    
    /**
     * Asynchronously fetches JSON data from the specified URL.
     * 
     * @param url the URL to fetch data from
     * @return a future that will complete with the parsed JSON response
     */
    private CompletableFuture<JsonNode> fetchJsonDataAsync(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();
        
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        // Check for HTTP errors
                        int statusCode = response.statusCode();
                        if (statusCode >= 400) {
                            ObjectNode errorNode = objectMapper.createObjectNode();
                            errorNode.put("error", true);
                            errorNode.put("status", statusCode);
                            errorNode.put("message", "HTTP error " + statusCode);
                            return errorNode;
                        }
                        
                        return objectMapper.readTree(response.body());
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to parse JSON response", e);
                    }
                });
    }
    
    /**
     * Checks if a response contains an error.
     * 
     * @param response the JSON node to check
     * @return true if the response contains an error, false otherwise
     */
    public boolean isErrorResponse(JsonNode response) {
        return response.has("error") && response.get("error").asBoolean(false);
    }
    
    /**
     * Gets the error message from a response.
     * 
     * @param response the JSON node to extract the error message from
     * @return the error message, or null if there is no error
     */
    public String getErrorMessage(JsonNode response) {
        if (isErrorResponse(response) && response.has("message")) {
            return response.get("message").asText("Unknown error");
        }
        return null;
    }
}