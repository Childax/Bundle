package com.gamejam;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to represent a player's profile, including their name and game statistics.
 * This class is designed to be easily serialized and deserialized.
 */
public class PlayerProfile {
    private String name;
    private GameStats gameStats;

    public PlayerProfile(String name) {
        this.name = name;
        this.gameStats = new GameStats();
    }

    // Getters and setters for the name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter for the GameStats object
    public GameStats getGameStats() {
        return gameStats;
    }

    // You can also add other profile-related methods here, like updating overall stats
    // or adding an achievement system.
}
