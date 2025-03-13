package com.nhlstats.ui;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

import com.nhlstats.model.Player;
import com.nhlstats.model.PlayerStats;
import com.nhlstats.model.ComparisonResult;
import com.nhlstats.service.DataVisualizationService;

/**
 * Panel for displaying player comparison information.
 */
public class ComparisonPanel extends JPanel {
    
    private DataVisualizationService visualizationService;
    private List<Player> players;
    private ComparisonResult comparisonResult;
    
    private JTabbedPane tabbedPane;
    private JPanel playersPanel;
    private JPanel comparisonPane;
    private JPanel chartsPanel;
    
    private DecimalFormat decimalFormat;
    
    /**
     * Creates a new comparison panel.
     * 
     * @param visualizationService the service for preparing visualization data
     */
    public ComparisonPanel(DataVisualizationService visualizationService) {
        this.visualizationService = visualizationService;
        this.players = new ArrayList<>();
        this.decimalFormat = new DecimalFormat("#.##");
        
        setLayout(new BorderLayout());
        
        initializeUI();
    }
    
    /**
     * Sets up the user interface.
     */
    private void initializeUI() {
        tabbedPane = new JTabbedPane();
        
        // Create panels for each tab
        playersPanel = new JPanel();
        playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.Y_AXIS));
        
        comparisonPane = new JPanel(new BorderLayout());
        chartsPanel = new JPanel(new BorderLayout());
        
        // Add tabs
        tabbedPane.addTab("Players", new JScrollPane(playersPanel));
        tabbedPane.addTab("Comparison", new JScrollPane(comparisonPane));
        tabbedPane.addTab("Charts", new JScrollPane(chartsPanel));
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    /**
     * Adds a player to the panel.
     * 
     * @param player the player to add
     */
    public void addPlayer(Player player) {
        if (player == null) {
            return;
        }
        
        players.add(player);
        updatePlayersPanel();
    }
    
    /**
     * Displays the results of a player comparison.
     * 
     * @param result the comparison result
     */
    public void showComparisonResult(ComparisonResult result) {
        this.comparisonResult = result;
        
        updateComparisonPanel();
        updateChartsPanel();
        
        // Switch to comparison tab
        tabbedPane.setSelectedIndex(1);
    }
    
    /**
     * Clears all players from the panel.
     */
    public void clearPlayers() {
        players.clear();
        comparisonResult = null;
        
        updatePlayersPanel();
        comparisonPane.removeAll();
        chartsPanel.removeAll();
        
        revalidate();
        repaint();
    }
    
    /**
     * Updates the players panel with current player information.
     */
    private void updatePlayersPanel() {
        playersPanel.removeAll();
        
        for (Player player : players) {
            JPanel playerCard = createPlayerCard(player);
            playersPanel.add(playerCard);
            playersPanel.add(Box.createVerticalStrut(20)); // Spacing between cards
        }
        
        playersPanel.add(Box.createVerticalGlue()); // Push cards to the top
        
        revalidate();
        repaint();
    }
    
    /**
     * Creates a card displaying player information.
     * 
     * @param player the player
     * @return a panel containing player information
     */
    private JPanel createPlayerCard(Player player) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Header with player name, team, and position
        JPanel headerPanel = new JPanel(new BorderLayout());
        
        JPanel playerInfoPanel = new JPanel(new GridLayout(3, 1));
        JLabel nameLabel = new JLabel(player.getFullName());
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 16f));
        
        JLabel teamLabel = new JLabel("Team: " + player.getTeamName());
        JLabel positionLabel = new JLabel("Position: " + player.getPosition());
        
        playerInfoPanel.add(nameLabel);
        playerInfoPanel.add(teamLabel);
        playerInfoPanel.add(positionLabel);
        
        // Player image
        JLabel imageLabel = new JLabel();
        try {
            URL imageUrl = new URL(player.getImageUrl());
            Image image = ImageIO.read(imageUrl);
            image = image.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(image));
        } catch (IOException e) {
            imageLabel.setText("No Image");
        }
        
        headerPanel.add(playerInfoPanel, BorderLayout.CENTER);
        headerPanel.add(imageLabel, BorderLayout.EAST);
        
        // Stats table
        JPanel statsPanel = new JPanel(new BorderLayout());
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        String[] columnNames = {"Statistic", "Value"};
        
        Object[][] data;
        
        if (player.getSeasonStats() != null) {
            PlayerStats stats = player.getSeasonStats();
            data = new Object[][]{
                {"Games Played", stats.getGamesPlayed()},
                {"Goals", stats.getGoals()},
                {"Assists", stats.getAssists()},
                {"Points", stats.getPoints()},
                {"Plus/Minus", stats.getPlusMinus()},
                {"Points Per Game", decimalFormat.format(stats.getPointsPerGame())},
                {"Shot Percentage", decimalFormat.format(stats.getShotPercentage()) + "%"}
            };
        } else {
            data = new Object[][]{{"No Data Available", ""}};
        }
        
        JTable statsTable = new JTable(data, columnNames);
        statsTable.setEnabled(false);
        
        // Remove button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(e -> {
            players.remove(player);
            updatePlayersPanel();
        });
        buttonPanel.add(removeButton);
        
        statsPanel.add(new JScrollPane(statsTable), BorderLayout.CENTER);
        
        card.add(headerPanel, BorderLayout.NORTH);
        card.add(statsPanel, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.SOUTH);
        
        return card;
    }
    
    /**
     * Updates the comparison panel with the current comparison result.
     */
    private void updateComparisonPanel() {
        comparisonPane.removeAll();
        
        if (comparisonResult == null) {
            JLabel noDataLabel = new JLabel("No comparison data available");
            noDataLabel.setHorizontalAlignment(JLabel.CENTER);
            comparisonPane.add(noDataLabel, BorderLayout.CENTER);
            return;
        }
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        
        // Add category winners section
        JPanel winnersPanel = new JPanel(new GridLayout(0, 1));
        winnersPanel.setBorder(BorderFactory.createTitledBorder("Category Leaders"));
        
        Map<String, Map<String, Player>> categoryWinners = comparisonResult.getCategoryWinners();
        for (Map.Entry<String, Map<String, Player>> entry : categoryWinners.entrySet()) {
            String category = entry.getKey();
            Player winner = entry.getValue().get("winner");
            boolean hasTies = entry.getValue().containsKey("hasTies");
            
            String formattedCategory = formatCategoryName(category);
            
            JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel categoryLabel = new JLabel(formattedCategory + ": ");
            JLabel winnerLabel = new JLabel(winner.getFullName());
            winnerLabel.setFont(winnerLabel.getFont().deriveFont(Font.BOLD));
            
            categoryPanel.add(categoryLabel);
            categoryPanel.add(winnerLabel);
            
            if (hasTies) {
                JLabel tieLabel = new JLabel(" (tied)");
                tieLabel.setFont(tieLabel.getFont().deriveFont(Font.ITALIC));
                categoryPanel.add(tieLabel);
            }
            
            winnersPanel.add(categoryPanel);
        }
        
        // Add similarity scores section
        JPanel similarityPanel = new JPanel(new GridLayout(0, 1));
        similarityPanel.setBorder(BorderFactory.createTitledBorder("Player Similarity Scores"));
        
        Map<String, Double> similarityScores = comparisonResult.getSimilarityScores();
        for (Map.Entry<String, Double> entry : similarityScores.entrySet()) {
            String key = entry.getKey();
            Double score = entry.getValue();
            
            String[] ids = key.split("-");
            if (ids.length == 2) {
                String player1Name = findPlayerNameById(ids[0]);
                String player2Name = findPlayerNameById(ids[1]);
                
                JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JLabel playersLabel = new JLabel(player1Name + " and " + player2Name + ": ");
                JLabel scoreLabel = new JLabel(decimalFormat.format(score) + "% similarity");
                scoreLabel.setFont(scoreLabel.getFont().deriveFont(Font.BOLD));
                
                scorePanel.add(playersLabel);
                scorePanel.add(scoreLabel);
                
                similarityPanel.add(scorePanel);
            }
        }
        
        contentPanel.add(winnersPanel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(similarityPanel);
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        comparisonPane.add(scrollPane, BorderLayout.CENTER);
        
        revalidate();
        repaint();
    }
    
    /**
     * Updates the charts panel with the current player data.
     */
    private void updateChartsPanel() {
        chartsPanel.removeAll();
        
        if (players.isEmpty()) {
            JLabel noDataLabel = new JLabel("No player data available for charts");
            noDataLabel.setHorizontalAlignment(JLabel.CENTER);
            chartsPanel.add(noDataLabel, BorderLayout.CENTER);
            return;
        }
        
        // In a real implementation, this would create charts using a charting library
        // For now, let's just show a placeholder
        JPanel contentPanel = new JPanel(new BorderLayout());
        
        JLabel placeholderLabel = new JLabel("Chart visualization would be displayed here");
        placeholderLabel.setHorizontalAlignment(JLabel.CENTER);
        placeholderLabel.setFont(placeholderLabel.getFont().deriveFont(Font.BOLD, 16f));
        
        JPanel controlPanel = new JPanel();
        String[] chartTypes = {
            "Goals Comparison", "Assists Comparison", "Points Comparison",
            "Points Per Game Comparison", "Shot Percentage Comparison"
        };
        JComboBox<String> chartSelector = new JComboBox<>(chartTypes);
        controlPanel.add(new JLabel("Chart Type:"));
        controlPanel.add(chartSelector);
        
        contentPanel.add(controlPanel, BorderLayout.NORTH);
        contentPanel.add(placeholderLabel, BorderLayout.CENTER);
        
        // For demonstration, add a ChartPanel as a placeholder
        ChartPanel chart = new ChartPanel();
        chart.setPreferredSize(new Dimension(600, 400));
        contentPanel.add(chart, BorderLayout.CENTER);
        
        chartsPanel.add(contentPanel, BorderLayout.CENTER);
        
        revalidate();
        repaint();
    }
    
    /**
     * Formats a category name for display.
     * 
     * @param category the category name
     * @return the formatted name
     */
    private String formatCategoryName(String category) {
        switch (category) {
            case "goals":
                return "Most Goals";
            case "assists":
                return "Most Assists";
            case "points":
                return "Most Points";
            case "plusMinus":
                return "Best Plus/Minus";
            case "pointsPerGame":
                return "Best Points Per Game";
            case "shotPercentage":
                return "Best Shot Percentage";
            default:
                return category;
        }
    }
    
    /**
     * Finds a player's name by their ID.
     * 
     * @param id the player ID
     * @return the player name
     */
    private String findPlayerNameById(String id) {
        for (Player player : players) {
            if (player.getId().equals(id)) {
                return player.getFullName();
            }
        }
        return "Unknown Player";
    }
}