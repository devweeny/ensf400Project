package com.nhlstats.service;

import com.nhlstats.model.Player;
import java.util.*;

public class PlayerService {

    // Get a player by name or ID
    public Player getPlayerById(String playerId) {
        return null;
    }

    // Fetch all players from a data source (API, DB, JSON file)
    public List<Player> getAllPlayers() {
        return new ArrayList<>();
    }

    // Filter players by position (F, D, G)
    public List<Player> filterPlayersByPosition(List<Player> players, String position) {
        return players.stream()
                     .filter(player -> player.getPosition().equalsIgnoreCase(position))
                     .toList();
    }

    // Search players by partial name
    public List<Player> searchPlayersByName(String query) {
        return new ArrayList<>();
    }
}
