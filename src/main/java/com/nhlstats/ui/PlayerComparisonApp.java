package com.nhlstats.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.nhlstats.model.Player;
import com.nhlstats.model.ComparisonResult;
import com.nhlstats.service.PlayerService;
import com.nhlstats.service.StatsComparisonService;
import com.nhlstats.service.DataVisualizationService;
import com.nhlstats.api.EndpointUrls;

/**
 * Main application window for the NHL Player Comparison Tool.
 */
public class PlayerComparisonApp extends JFrame {
    
    private PlayerService playerService;
    private StatsComparisonService comparisonService;
    private DataVisualizationService visualizationService;
    
    private List<Player> selectedPlayers;
    private ComparisonPanel comparisonPanel;
    private JPanel controlPanel;
    private JPanel statusPanel;
    private JComboBox<String> seasonSelector;
    private JComboBox<String> gameTypeSelector;
    private JTextField playerSearchField;
    private JButton addPlayerButton;
    private JButton compareButton;
    private JLabel statusLabel;
    
    /**
     * Creates a new player comparison application window.
     */
    public PlayerComparisonApp() {
        super("NHL Player Comparison Tool");
        
        // Initialize services
        this.playerService = new PlayerService();
        this.comparisonService = new StatsComparisonService();
        this.visualizationService = new DataVisualizationService();
        this.selectedPlayers = new ArrayList<>();
        
        initializeUI();
    }
    
    /**
     * Sets up the user interface.
     */
    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLayout(new BorderLayout());
        
        // Create UI components
        createControlPanel();
        createComparisonPanel();
        createStatusPanel();
        
        // Add components to frame
        add(controlPanel, BorderLayout.NORTH);
        add(comparisonPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
        
        setLocationRelativeTo(null); // Center on screen
    }
    
    /**
     * Creates the control panel with player selection and comparison options.
     */
    private void createControlPanel() {
        controlPanel = new JPanel();
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Season selector
        JLabel seasonLabel = new JLabel("Season:");
        seasonSelector = new JComboBox<>(new String[] {
                "2023-2024", "2022-2023", "2021-2022", "2020-2021"
        });
        
        // Game type selector
        JLabel gameTypeLabel = new JLabel("Game Type:");
        gameTypeSelector = new JComboBox<>(new String[] {
                "Regular Season", "Playoffs"
        });
        
        // Player search
        JLabel playerSearchLabel = new JLabel("Player ID:");
        playerSearchField = new JTextField(15);
        
        // Buttons
        addPlayerButton = new JButton("Add Player");
        compareButton = new JButton("Compare");
        JButton clearButton = new JButton("Clear All");
        
        // Add action listeners
        addPlayerButton.addActionListener(e -> addPlayer());
        compareButton.addActionListener(e -> compareSelectedPlayers());
        clearButton.addActionListener(e -> clearSelectedPlayers());
        
        // Layout components
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        controlPanel.add(seasonLabel, gbc);
        
        gbc.gridx = 1;
        controlPanel.add(seasonSelector, gbc);
        
        gbc.gridx = 2;
        controlPanel.add(gameTypeLabel, gbc);
        
        gbc.gridx = 3;
        controlPanel.add(gameTypeSelector, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        controlPanel.add(playerSearchLabel, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(playerSearchField, gbc);
        
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        controlPanel.add(addPlayerButton, gbc);
        
        gbc.gridx = 3;
        controlPanel.add(compareButton, gbc);
        
        gbc.gridx = 4;
        controlPanel.add(clearButton, gbc);
    }
    
    /**
     * Creates the main comparison panel where player stats are displayed.
     */
    private void createComparisonPanel() {
        comparisonPanel = new ComparisonPanel(visualizationService);
        comparisonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    
    /**
     * Creates the status panel for showing messages to the user.
     */
    private void createStatusPanel() {
        statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        statusLabel = new JLabel("Ready");
        statusPanel.add(statusLabel, BorderLayout.WEST);
        
        JLabel apiInfoLabel = new JLabel("Using NHL API v1");
        statusPanel.add(apiInfoLabel, BorderLayout.EAST);
    }
    
    /**
     * Adds a player to the comparison panel.
     */
    private void addPlayer() {
        String playerId = playerSearchField.getText().trim();
        
        if (playerId.isEmpty()) {
            setStatusMessage("Please enter a player ID");
            return;
        }
        
        setStatusMessage("Loading player data...");
        playerSearchField.setText("");
        
        // Use SwingWorker to load player data asynchronously
        SwingWorker<Player, Void> worker = new SwingWorker<Player, Void>() {
            @Override
            protected Player doInBackground() throws Exception {
                String season = convertSeason(seasonSelector.getSelectedItem().toString());
                String gameType = getGameType(gameTypeSelector.getSelectedItem().toString());
                
                return playerService.getPlayerWithStats(playerId, season, gameType);
            }
            
            @Override
            protected void done() {
                try {
                    Player player = get();
                    if (player != null) {
                        selectedPlayers.add(player);
                        comparisonPanel.addPlayer(player);
                        setStatusMessage("Added player: " + player.getFullName());
                        
                        // Enable compare button if we have at least 2 players
                        compareButton.setEnabled(selectedPlayers.size() >= 2);
                    } else {
                        setStatusMessage("Could not find player with ID: " + playerId);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    setStatusMessage("Error loading player: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Compares the currently selected players.
     */
    private void compareSelectedPlayers() {
        if (selectedPlayers.size() < 2) {
            setStatusMessage("Need at least two players to compare");
            return;
        }
        
        setStatusMessage("Generating comparison...");
        
        SwingWorker<ComparisonResult, Void> worker = new SwingWorker<ComparisonResult, Void>() {
            @Override
            protected ComparisonResult doInBackground() throws Exception {
                return comparisonService.comparePlayerStats(selectedPlayers);
            }
            
            @Override
            protected void done() {
                try {
                    ComparisonResult result = get();
                    comparisonPanel.showComparisonResult(result);
                    setStatusMessage("Comparison complete");
                } catch (InterruptedException | ExecutionException e) {
                    setStatusMessage("Error comparing players: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Clears all selected players.
     */
    private void clearSelectedPlayers() {
        selectedPlayers.clear();
        comparisonPanel.clearPlayers();
        compareButton.setEnabled(false);
        setStatusMessage("Cleared all players");
    }
    
    /**
     * Updates the status message.
     * 
     * @param message the message to display
     */
    private void setStatusMessage(String message) {
        statusLabel.setText(message);
    }
    
    /**
     * Converts the season display text to the API format.
     * 
     * @param seasonText the season text (e.g. "2023-2024")
     * @return the API season format (e.g. "20232024")
     */
    private String convertSeason(String seasonText) {
        return seasonText.replace("-", "");
    }
    
    /**
     * Gets the game type code from the display text.
     * 
     * @param gameTypeText the game type text
     * @return the game type code
     */
    private String getGameType(String gameTypeText) {
        if (gameTypeText.equals("Playoffs")) {
            return EndpointUrls.GAME_TYPE_PLAYOFFS;
        }
        return EndpointUrls.GAME_TYPE_REGULAR_SEASON;
    }
    
    /**
     * Entry point for launching the application.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                PlayerComparisonApp app = new PlayerComparisonApp();
                app.setVisible(true);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}