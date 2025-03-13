package com.nhlstats;

import com.nhlstats.model.Player;
import com.nhlstats.model.ComparisonResult;
import com.nhlstats.service.PlayerService;
import com.nhlstats.service.StatsComparisonService;
import com.nhlstats.api.EndpointUrls;
import com.nhlstats.ui.PlayerComparisonApp;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Main entry point for the NHL Player Comparison Tool.
 * Supports both GUI and command line modes.
 */
public class Main {
    
    private static final String SEASON = EndpointUrls.CURRENT_SEASON;
    private static final String GAME_TYPE = EndpointUrls.GAME_TYPE_REGULAR_SEASON;
    
    /**
     * Main method that launches the application.
     * 
     * @param args command line arguments
     *        If no arguments are provided, launches the GUI.
     *        If "--cli" is the first argument, runs in command line mode.
     *        Example: java -jar player-comparison-tool.jar --cli 8478402 8477492
     */
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--cli")) {
            runCommandLine(Arrays.copyOfRange(args, 1, args.length));
        } else {
            launchGUI();
        }
    }
    
    /**
     * Launches the graphical user interface.
     */
    private static void launchGUI() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                System.out.println("Starting NHL Player Comparison Tool GUI...");
                PlayerComparisonApp app = new PlayerComparisonApp();
                app.setVisible(true);
                
            } catch (Exception e) {
                System.err.println("Error launching GUI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Runs the application in command line mode.
     * 
     * @param playerIds array of player IDs to compare
     */
    private static void runCommandLine(String[] playerIds) {
        if (playerIds.length < 2) {
            System.out.println("Please provide at least two player IDs to compare.");
            System.out.println("Example: java -jar player-comparison-tool.jar --cli 8478402 8477492");
            return;
        }
        
        try {
            System.out.println("NHL Player Comparison Tool (CLI Mode)");
            System.out.println("=====================================");
            System.out.println("Season: " + formatSeasonDisplay(SEASON));
            System.out.println("Game Type: Regular Season");
            System.out.println();
            
            System.out.println("Fetching player data for IDs: " + String.join(", ", playerIds));
            
            PlayerService playerService = new PlayerService();
            List<Player> players = playerService.getMultiplePlayersWithStats(Arrays.asList(playerIds), SEASON, GAME_TYPE);
            
            // Display basic player info
            System.out.println("\nPlayers:");
            for (Player player : players) {
                System.out.println("- " + player.getFullName() + " (" + player.getTeamName() + ", " + player.getPosition() + ")");
            }
            
            // Compare players
            System.out.println("\nGenerating comparison...");
            StatsComparisonService comparisonService = new StatsComparisonService();
            ComparisonResult result = comparisonService.comparePlayerStats(players);
            
            // Display comparison results
            displayComparisonResults(result);
            
            // Interactive mode
            offerInteractiveMode(players, comparisonService);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Displays formatted comparison results.
     * 
     * @param result the comparison result to display
     */
    private static void displayComparisonResults(ComparisonResult result) {
        System.out.println("\nComparison Results:");
        System.out.println("==================");
        
        // Display category winners
        System.out.println("\nCategory Leaders:");
        for (String category : result.getCategoryWinners().keySet()) {
            Player winner = result.getCategoryWinners().get(category).get("winner");
            boolean hasTie = result.getCategoryWinners().get(category).containsKey("hasTies");
            
            String formattedCategory = formatCategory(category);
            String value = getValueForCategory(winner, category);
            
            System.out.print("- " + formattedCategory + ": " + winner.getFullName() + " (" + value + ")");
            if (hasTie) {
                System.out.print(" (Tied)");
            }
            System.out.println();
        }
        
        // Display similarity scores
        System.out.println("\nPlayer Similarity Scores:");
        for (String key : result.getSimilarityScores().keySet()) {
            Double score = result.getSimilarityScores().get(key);
            String[] ids = key.split("-");
            
            if (ids.length == 2) {
                String player1Name = findPlayerNameById(result.getPlayers(), ids[0]);
                String player2Name = findPlayerNameById(result.getPlayers(), ids[1]);
                
                System.out.printf("- %s and %s: %.2f%% similarity%n", 
                        player1Name, player2Name, score);
            }
        }
    }
    
    /**
     * Offers an interactive command-line mode for exploring more detailed comparisons.
     * 
     * @param players the list of players being compared
     * @param comparisonService the comparison service to use
     */
    private static void offerInteractiveMode(List<Player> players, StatsComparisonService comparisonService) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\nWould you like to see more detailed comparisons? (y/n)");
        String response = scanner.nextLine().trim().toLowerCase();
        
        if ("y".equals(response) || "yes".equals(response)) {
            boolean exit = false;
            
            while (!exit) {
                System.out.println("\nAvailable commands:");
                System.out.println("1. Compare two players (enter: compare <player_index> <player_index>)");
                System.out.println("2. Show player stats (enter: stats <player_index>)");
                System.out.println("3. Exit (enter: exit)");
                System.out.println("\nPlayer indexes:");
                
                for (int i = 0; i < players.size(); i++) {
                    System.out.println(i + ". " + players.get(i).getFullName());
                }
                
                System.out.print("\nEnter command: ");
                String command = scanner.nextLine().trim();
                
                if (command.startsWith("compare ")) {
                    String[] parts = command.split(" ");
                    if (parts.length == 3) {
                        try {
                            int idx1 = Integer.parseInt(parts[1]);
                            int idx2 = Integer.parseInt(parts[2]);
                            
                            if (idx1 >= 0 && idx1 < players.size() && idx2 >= 0 && idx2 < players.size()) {
                                Player player1 = players.get(idx1);
                                Player player2 = players.get(idx2);
                                
                                String comparison = comparisonService.comparePlayers(player1, player2);
                                System.out.println("\nDetailed Comparison:");
                                System.out.println(comparison);
                            } else {
                                System.out.println("Invalid player indexes. Please try again.");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input. Please enter valid player indexes.");
                        }
                    } else {
                        System.out.println("Invalid command format. Example: compare 0 1");
                    }
                } else if (command.startsWith("stats ")) {
                    String[] parts = command.split(" ");
                    if (parts.length == 2) {
                        try {
                            int idx = Integer.parseInt(parts[1]);
                            
                            if (idx >= 0 && idx < players.size()) {
                                Player player = players.get(idx);
                                System.out.println("\nStats for " + player.getFullName() + ":");
                                System.out.println(player.getSeasonStats().toString());
                            } else {
                                System.out.println("Invalid player index. Please try again.");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input. Please enter a valid player index.");
                        }
                    } else {
                        System.out.println("Invalid command format. Example: stats 0");
                    }
                } else if ("exit".equals(command)) {
                    exit = true;
                } else {
                    System.out.println("Unknown command. Please try again.");
                }
            }
        }
        
        System.out.println("\nThank you for using NHL Player Comparison Tool!");
        scanner.close();
    }
    
    /**
     * Formats a category name to be more readable.
     * 
     * @param category the raw category name
     * @return the formatted category name
     */
    private static String formatCategory(String category) {
        switch (category) {
            case "goals": return "Most Goals";
            case "assists": return "Most Assists";
            case "points": return "Most Points";
            case "plusMinus": return "Best Plus/Minus";
            case "pointsPerGame": return "Best Points Per Game";
            case "shotPercentage": return "Best Shot Percentage";
            default: return category;
        }
    }
    
    /**
     * Gets the value for a specific category from a player.
     * 
     * @param player the player
     * @param category the category name
     * @return the formatted value
     */
    private static String getValueForCategory(Player player, String category) {
        if (player.getSeasonStats() == null) {
            return "N/A";
        }
        
        switch (category) {
            case "goals": return String.valueOf(player.getSeasonStats().getGoals());
            case "assists": return String.valueOf(player.getSeasonStats().getAssists());
            case "points": return String.valueOf(player.getSeasonStats().getPoints());
            case "plusMinus": return String.valueOf(player.getSeasonStats().getPlusMinus());
            case "pointsPerGame": return String.format("%.2f", player.getSeasonStats().getPointsPerGame());
            case "shotPercentage": return String.format("%.2f%%", player.getSeasonStats().getShotPercentage());
            default: return "N/A";
        }
    }
    
    /**
     * Finds a player's name by their ID.
     * 
     * @param players the list of players to search
     * @param id the player ID to find
     * @return the player's name, or "Unknown Player" if not found
     */
    private static String findPlayerNameById(List<Player> players, String id) {
        for (Player player : players) {
            if (player.getId().equals(id)) {
                return player.getFullName();
            }
        }
        return "Unknown Player";
    }
    
    /**
     * Formats a season code for display.
     * 
     * @param season the season code (e.g., "20232024")
     * @return the formatted season (e.g., "2023-2024")
     */
    private static String formatSeasonDisplay(String season) {
        if (season.length() == 8) {
            return season.substring(0, 4) + "-" + season.substring(4);
        }
        return season;
    }
}