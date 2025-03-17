package com.nhlstats.endpoints;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.nhlstats.model.Player;
import com.nhlstats.model.ComparisonResult;
import com.nhlstats.service.PlayerService;
import com.nhlstats.service.StatsComparisonService;

@RestController
@RequestMapping("/api")
public class Endpoints {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private StatsComparisonService comparisonService;

    @GetMapping("/player/{playerId}")
    public Player getPlayer(@PathVariable String playerId) throws IOException, InterruptedException {
        return playerService.getPlayerWithStats(playerId, "20222023", "R");
    }

    @GetMapping("/compare/{playerstr}")
    public ComparisonResult comparePlayers(@PathVariable String playerstr) throws IOException, InterruptedException {
        List<String> players = List.of(playerstr.split(","));
        
        List<Player> playerList = new ArrayList<>();
        for (String player : players) {
            playerList.add(playerService.getPlayerWithStats(player, "20222023", "R"));
        }
        return comparisonService.comparePlayerStats(playerList);

    }

    @GetMapping("/seasons")
    public List<String> getAvailableSeasons() {
        return List.of("20232024", "20222023", "20212022", "20202021");
    }

    @GetMapping("/gametypes")
    public List<String> getGameTypes() {
        return List.of("R", "P"); // Regular season and Playoffs
    }
}
