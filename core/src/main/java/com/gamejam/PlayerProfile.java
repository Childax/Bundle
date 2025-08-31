package com.gamejam;

import com.badlogic.gdx.Preferences;

/**
 * Stores all player-specific data, including their username and game stats.
 * This class should be a simple data container.
 */
public class PlayerProfile {
    private String username;
    private GameStats gameStats;

    public PlayerProfile(String username) {
        this.username = username;
        this.gameStats = new GameStats();
    }

    public String getUsername() {
        return username;
    }

    public GameStats getGameStats() {
        return gameStats;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Loads the game stats from the preferences file.
     * @param preferences The preferences object to load from.
     */
    public void loadGameStats(Preferences preferences) {
        this.gameStats.setTotalWordsSolved(preferences.getInteger("totalWordsSolved", 0));
        this.gameStats.setBundlesSolved(preferences.getInteger("bundlesSolved", 0));
        this.gameStats.setClassicsSolved(preferences.getInteger("classicsSolved", 0));
        this.gameStats.setBestTimeBundle(preferences.getFloat("bestTimeBundle", Float.MAX_VALUE));
        this.gameStats.setBestTimeClassic(preferences.getFloat("bestTimeClassic", Float.MAX_VALUE));
        this.gameStats.setBestWord(preferences.getString("bestWord", ""));
        this.gameStats.setBestWordGuesses(preferences.getInteger("bestWordGuesses", Integer.MAX_VALUE));
        // Load the new totalGuesses and totalGuessesForSolvedWords fields
        this.gameStats.setTotalGuesses(preferences.getInteger("totalGuesses", 0));
        this.gameStats.setTotalGuessesForSolvedWords(preferences.getInteger("totalGuessesForSolvedWords", 0));
    }

    /**
     * Saves the game stats to the preferences file.
     * @param preferences The preferences object to save to.
     */
    public void saveGameStats(Preferences preferences) {
        preferences.putInteger("totalWordsSolved", this.gameStats.getTotalWordsSolved());
        preferences.putInteger("bundlesSolved", this.gameStats.getBundlesSolved());
        preferences.putInteger("classicsSolved", this.gameStats.getClassicsSolved());
        preferences.putFloat("bestTimeBundle", this.gameStats.getBestTimeBundle());
        preferences.putFloat("bestTimeClassic", this.gameStats.getBestTimeClassic());
        preferences.putString("bestWord", this.gameStats.getBestWord());
        preferences.putInteger("bestWordGuesses", this.gameStats.getBestWordGuesses());
        // Save the new totalGuesses and totalGuessesForSolvedWords fields
        preferences.putInteger("totalGuesses", this.gameStats.getTotalGuesses());
        preferences.putInteger("totalGuessesForSolvedWords", this.gameStats.getTotalGuessesForSolvedWords());
        preferences.flush();
    }

    /**
     * Clears all player data from the preferences and resets the profile.
     * This is useful for a "start fresh" or "reset profile" feature.
     * @param preferences The preferences object to clear.
     */
    public void resetProfile(Preferences preferences) {
        preferences.clear();
        preferences.flush();
        this.gameStats = new GameStats();
        this.username = null;
    }
}
