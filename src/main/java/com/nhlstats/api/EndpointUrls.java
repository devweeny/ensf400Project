package com.nhlstats.api;

/**
 * Contains constant URLs for accessing the NHL API endpoints.
 * This class centralizes all API paths used in the application.
 */
public class EndpointUrls {
    
    /**
     * Base URL for the NHL API web service.
     */
    public static final String API_BASE_URL = "https://api-web.nhle.com/v1/";
    
    /**
     * URL template for fetching player information.
     * Requires player ID to be inserted.
     */
    public static final String PLAYER_INFO = "player/%s";
    
    /**
     * URL template for fetching a player's game log for a specific season and game type.
     * Requires player ID, season, and game type to be inserted.
     */
    public static final String PLAYER_GAME_LOG = "player/%s/game-log/%s/%s";
    
    /**
     * URL template for fetching a player's career statistics.
     * Requires player ID to be inserted.
     */
    public static final String PLAYER_CAREER_STATS = "player/%s/landing";
    
    /**
     * URL for fetching current NHL standings.
     */
    public static final String STANDINGS = "standings/now";
    
    /**
     * URL template for fetching team information.
     * Requires team ID to be inserted.
     */
    public static final String TEAM_INFO = "team/%s/landing";
    
    /**
     * URL template for fetching team roster.
     * Requires team ID and season to be inserted.
     */
    public static final String TEAM_ROSTER = "roster/%s/%s";
    
    /**
     * URL template for fetching game information.
     * Requires game ID to be inserted.
     */
    public static final String GAME_INFO = "gamecenter/%s/boxscore";
    
    /**
     * Game type code for regular season.
     */
    public static final String GAME_TYPE_REGULAR_SEASON = "2";
    
    /**
     * Game type code for playoffs.
     */
    public static final String GAME_TYPE_PLAYOFFS = "3";
    
    /**
     * Current NHL season format (e.g., "20232024" for the 2023-2024 season).
     */
    public static final String CURRENT_SEASON = "20232024";
    
    /**
     * Builds a full URL for a specific API endpoint by combining the base URL with the path.
     * 
     * @param endpoint the endpoint path or template
     * @param params parameters to be inserted into the endpoint template
     * @return the complete URL
     */
    public static String buildUrl(String endpoint, Object... params) {
        String formattedEndpoint = String.format(endpoint, params);
        return API_BASE_URL + formattedEndpoint;
    }
}