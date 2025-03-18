package com.nhlstats.ui;

import com.nhlstats.model.Player;
import com.nhlstats.model.PlayerStats;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel for displaying chart visualizations of player statistics.
 */
public class ChartPanel extends JPanel {
    
    public enum ChartType {
        GOALS, ASSISTS, POINTS, PLUS_MINUS, POINTS_PER_GAME, SHOT_PERCENTAGE, GAMES_PLAYED
    }
    
    private Map<String, Integer> data;
    private Color[] barColors;
    private String title;
    private String xAxisLabel;
    private String yAxisLabel;
    private int maxValue;
    private List<Player> players;
    private ChartType currentChartType;
    
    /**
     * Creates a new chart panel with default settings.
     */
    public ChartPanel() {
        this.data = new HashMap<>();
        this.barColors = new Color[] {
            new Color(30, 144, 255),   // Blue
            new Color(220, 20, 60),    // Crimson
            new Color(50, 205, 50),    // Lime Green
            new Color(255, 165, 0),    // Orange
            new Color(128, 0, 128)     // Purple
        };
        this.title = "Sample Bar Chart";
        this.xAxisLabel = "Players";
        this.yAxisLabel = "Points";
        this.currentChartType = ChartType.POINTS;
        
        setBackground(Color.WHITE);
    }
    
    /**
     * Sets the list of players to compare and updates the chart.
     * 
     * @param players the list of players to compare
     * @param chartType the type of statistic to chart
     */
    public void setPlayers(List<Player> players, ChartType chartType) {
        this.players = players;
        this.currentChartType = chartType;
        
        // Generate chart data from players
        updateChartFromPlayers();
    }
    
    /**
     * Updates the chart display based on the current players and chart type.
     */
    public void updateChartFromPlayers() {
        if (players == null || players.isEmpty()) {
            return;
        }
        
        // Set appropriate chart title and axis labels based on chart type
        switch (currentChartType) {
            case GOALS:
                title = "Goals Comparison";
                yAxisLabel = "Goals";
                break;
            case ASSISTS:
                title = "Assists Comparison";
                yAxisLabel = "Assists";
                break;
            case POINTS:
                title = "Points Comparison";
                yAxisLabel = "Points";
                break;
            case PLUS_MINUS:
                title = "Plus/Minus Comparison";
                yAxisLabel = "Plus/Minus";
                break;
            case POINTS_PER_GAME:
                title = "Points Per Game Comparison";
                yAxisLabel = "PPG";
                break;
            case SHOT_PERCENTAGE:
                title = "Shot Percentage Comparison";
                yAxisLabel = "Shot %";
                break;
            case GAMES_PLAYED:
                title = "Games Played Comparison";
                yAxisLabel = "Games";
                break;
        }
        
        // Create data map from player stats
        Map<String, Integer> chartData = new HashMap<>();
        
        for (Player player : players) {
            if (player == null || player.getSeasonStats() == null) {
                continue;
            }
            
            PlayerStats stats = player.getSeasonStats();
            String playerName = player.getFullName();
            int value = 0;
            
            switch (currentChartType) {
                case GOALS:
                    value = stats.getGoals();
                    break;
                case ASSISTS:
                    value = stats.getAssists();
                    break;
                case POINTS:
                    value = stats.getPoints();
                    break;
                case PLUS_MINUS:
                    value = stats.getPlusMinus();
                    break;
                case POINTS_PER_GAME:
                    // Convert to an integer for visualization (multiply by 100 for 2 decimal places)
                    value = (int) (stats.getPointsPerGame() * 100);
                    break;
                case SHOT_PERCENTAGE:
                    // Convert to an integer for visualization (multiply by 100 to get percentage)
                    value = (int) stats.getShotPercentage();
                    break;
                case GAMES_PLAYED:
                    value = stats.getGamesPlayed();
                    break;
            }
            
            chartData.put(playerName, value);
        }
        
        // Update the chart data
        setData(chartData);
    }
    
    /**
     * Sets the chart data.
     * 
     * @param data the data to display
     */
    public void setData(Map<String, Integer> data) {
        this.data = data;
        updateMaxValue();
        repaint();
    }
    
    /**
     * Changes the current chart type and updates the display.
     * 
     * @param chartType the new chart type to display
     */
    public void setChartType(ChartType chartType) {
        this.currentChartType = chartType;
        updateChartFromPlayers();
    }
    
    /**
     * Sets the chart title.
     * 
     * @param title the chart title
     */
    public void setChartTitle(String title) {
        this.title = title;
        repaint();
    }
    
    /**
     * Sets the x-axis label.
     * 
     * @param label the x-axis label
     */
    public void setXAxisLabel(String label) {
        this.xAxisLabel = label;
        repaint();
    }
    
    /**
     * Sets the y-axis label.
     * 
     * @param label the y-axis label
     */
    public void setYAxisLabel(String label) {
        this.yAxisLabel = label;
        repaint();
    }
    
    /**
     * Updates the maximum value for scaling.
     */
    private void updateMaxValue() {
        maxValue = 0;
        for (int value : data.values()) {
            if (value > maxValue) {
                maxValue = value;
            }
        }
        // Add 10% for padding
        maxValue = (int)(maxValue * 1.1);
        if (maxValue == 0) maxValue = 1; // Prevent division by zero
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int padding = 50;
        int labelPadding = 20;
        int barWidth = (getWidth() - (2 * padding)) / (data.size() > 0 ? data.size() : 1);
        
        // Draw title
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics titleFontMetrics = g2d.getFontMetrics();
        int titleWidth = titleFontMetrics.stringWidth(title);
        g2d.drawString(title, (getWidth() - titleWidth) / 2, 30);
        
        // Draw axis labels
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        FontMetrics axisFontMetrics = g2d.getFontMetrics();
        
        // Draw y-axis label (rotated)
        g2d.translate(20, (getHeight() + g2d.getFontMetrics().stringWidth(yAxisLabel)) / 2);
        g2d.rotate(-Math.PI / 2);
        g2d.drawString(yAxisLabel, 0, 0);
        g2d.rotate(Math.PI / 2);
        g2d.translate(-20, -(getHeight() + g2d.getFontMetrics().stringWidth(yAxisLabel)) / 2);
        
        // Draw x-axis label
        int xAxisLabelWidth = axisFontMetrics.stringWidth(xAxisLabel);
        g2d.drawString(xAxisLabel, (getWidth() - xAxisLabelWidth) / 2, getHeight() - 5);
        
        // Draw axes
        g2d.setColor(Color.BLACK);
        g2d.drawLine(padding, getHeight() - padding, padding, padding); // Y-axis
        g2d.drawLine(padding, getHeight() - padding, getWidth() - padding, getHeight() - padding); // X-axis
        
        // Draw y-axis scale
        int yAxisHeight = getHeight() - (2 * padding);
        int numYDivisions = 10;
        for (int i = 0; i <= numYDivisions; i++) {
            int y = getHeight() - padding - ((yAxisHeight * i) / numYDivisions);
            int tickValue = (maxValue * i) / numYDivisions;
            
            // Format the tick value based on chart type
            String yLabel;
            if (currentChartType == ChartType.POINTS_PER_GAME && maxValue > 0) {
                // Show as decimal for points per game
                double decimalValue = tickValue / 100.0;
                yLabel = String.format("%.2f", decimalValue);
            } else if (currentChartType == ChartType.SHOT_PERCENTAGE) {
                // Show as percentage
                yLabel = tickValue + "%";
            } else {
                yLabel = Integer.toString(tickValue);
            }
            
            FontMetrics metrics = g2d.getFontMetrics();
            int labelWidth = metrics.stringWidth(yLabel);
            
            g2d.setColor(Color.BLACK);
            g2d.drawLine(padding, y, padding - 5, y); // Tick mark
            g2d.drawString(yLabel, padding - labelWidth - 5, y + (metrics.getHeight() / 2) - 3);
            
            // Draw light grid line
            g2d.setColor(new Color(200, 200, 200));
            g2d.drawLine(padding, y, getWidth() - padding, y);
        }
        
        // Draw bars
        int barIndex = 0;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            String key = entry.getKey();
            int value = entry.getValue();
            
            int x = padding + (barIndex * barWidth) + 10;
            int barHeight = (int)(((double)value / maxValue) * (getHeight() - (2 * padding)));
            int y = getHeight() - padding - barHeight;
            
            // Draw bar
            g2d.setColor(barColors[barIndex % barColors.length]);
            g2d.fillRect(x, y, barWidth - 20, barHeight);
            
            // Draw bar outline
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x, y, barWidth - 20, barHeight);
            
            // Draw value at top of bar
            String valueStr;
            if (currentChartType == ChartType.POINTS_PER_GAME) {
                valueStr = String.format("%.2f", value / 100.0);
            } else if (currentChartType == ChartType.SHOT_PERCENTAGE) {
                valueStr = value + "%";
            } else {
                valueStr = String.valueOf(value);
            }
            
            FontMetrics metrics = g2d.getFontMetrics();
            int labelWidth = metrics.stringWidth(valueStr);
            g2d.setColor(Color.BLACK);
            g2d.drawString(valueStr, x + (barWidth - 20) / 2 - labelWidth / 2, y - 5);
            
            // Draw x-axis label (player name)
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            metrics = g2d.getFontMetrics();
            
            // Truncate label if needed
            String displayKey = key;
            int maxLabelWidth = barWidth - 10;
            if (metrics.stringWidth(displayKey) > maxLabelWidth) {
                displayKey = displayKey.substring(0, Math.min(8, displayKey.length())) + "...";
            }
            
            labelWidth = metrics.stringWidth(displayKey);
            g2d.drawString(displayKey, x + (barWidth - 20) / 2 - labelWidth / 2, getHeight() - padding + 15);
            
            barIndex++;
        }
    }
    
    /**
     * Updates the chart with numeric data from a generic map.
     * This allows the chart to work with the visualization service which returns Number types.
     * 
     * @param numberData the map with Number values
     */
    public void setNumberData(Map<String, Number> numberData) {
        Map<String, Integer> intData = new HashMap<>();
        
        for (Map.Entry<String, Number> entry : numberData.entrySet()) {
            intData.put(entry.getKey(), entry.getValue().intValue());
        }
        
        setData(intData);
    }
    
    /**
     * Sets the bar colors used in the chart.
     * 
     * @param colors array of colors to use
     */
    public void setBarColors(Color[] colors) {
        this.barColors = colors;
        repaint();
    }
}