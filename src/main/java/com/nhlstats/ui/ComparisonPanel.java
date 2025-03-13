package com.nhlstats.ui;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
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
        
        // Player info section
        JPanel playerInfoPanel = new JPanel(new GridLayout(3, 1));
        
        // Name label - create only one instance
        JLabel nameLabel = new JLabel(player.getFullName());
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 16f));
        
        // Debug output to see if the name is actually available
        System.out.println("Creating card for player: ID=" + player.getId() + 
                          ", Name='" + player.getFullName() + "'" +
                          ", Team='" + player.getTeamName() + "'" +
                          ", Position='" + player.getPosition() + "'" +
                          ", Image URL='" + player.getImageUrl() + "'");
        
        JLabel teamLabel = new JLabel("Team: " + player.getTeamName());
        JLabel positionLabel = new JLabel("Position: " + player.getPosition());
        
        playerInfoPanel.add(nameLabel);
        playerInfoPanel.add(teamLabel);
        playerInfoPanel.add(positionLabel);
                
        // Player image
        JLabel imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(100, 100));

        if (player.getImageUrl() != null && !player.getImageUrl().isEmpty()) {
            try {
                // Try our primary image source
                URL imageUrl = new URL(player.getImageUrl());
                System.out.println("Loading image from: " + imageUrl);
                
                // Set a timeout for URL connection
                URLConnection connection = imageUrl.openConnection();
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(5000);
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                
                Image image = ImageIO.read(connection.getInputStream());
                
                if (image != null) {
                    image = image.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(image));
                } else {
                    System.out.println("Image is null for player: " + player.getFullName());
                    createPlaceholderImage(imageLabel, player);
                }
            } catch (Exception e) {
                System.out.println("Failed to load primary image for " + player.getFullName() + ": " + e.getMessage());
                
                // Try alternate URL if primary fails
                try {
                    String altImageUrl = String.format("https://assets.nhle.com/mugs/nhl/latest/%s.png", player.getId());
                    URL url = new URL(altImageUrl);
                    
                    URLConnection connection = url.openConnection();
                    connection.setConnectTimeout(3000);
                    connection.setReadTimeout(5000);
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                    
                    Image image = ImageIO.read(connection.getInputStream());
                    if (image != null) {
                        image = image.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                        imageLabel.setIcon(new ImageIcon(image));
                    } else {
                        createPlaceholderImage(imageLabel, player);
                    }
                } catch (Exception e2) {
                    System.out.println("Failed to load alternate image: " + e2.getMessage());
                    createPlaceholderImage(imageLabel, player);
                }
            }
        } else {
            System.out.println("No image URL for player: " + player.getFullName());
            createPlaceholderImage(imageLabel, player);
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

    private void createPlaceholderImage(JLabel label, Player player) {
        // Create a colored placeholder with player's initials
        int width = 100;
        int height = 100;
        
        BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = placeholder.createGraphics();
        
        // Use player's team colors if available, otherwise use a default color
        g2d.setColor(new Color(30, 144, 255)); // Default blue
        g2d.fillRect(0, 0, width, height);
        
        // Add initials
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        
        String initials = getInitials(player.getFullName());
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(initials);
        int textHeight = fm.getHeight();
        
        g2d.drawString(initials, (width - textWidth) / 2, height / 2 + textHeight / 4);
        
        g2d.dispose();
        
        label.setIcon(new ImageIcon(placeholder));
    }
    
    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        
        StringBuilder initials = new StringBuilder();
        String[] parts = name.split(" ");
        
        for (String part : parts) {
            if (!part.isEmpty()) {
                initials.append(part.charAt(0));
            }
        }
        
        return initials.toString().toUpperCase();
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