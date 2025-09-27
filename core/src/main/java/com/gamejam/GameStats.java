package com.gamejam;

import java.util.ArrayList;
import java.util.List;

/**
 * A data class to hold the player's game statistics.
 * This is a simple POJO (Plain Old Java Object).
 */
public class GameStats {
    // Basic game stats
    private int totalWordsSolved;
    private int totalGuesses;
    private long totalTimeInSeconds;
    private int bundlesSolved;
    private int classicsSolved;
    private int totalGuessesForSolvedWords;

    private String bestWord;
    private int bestWordGuesses;

    private float bestTimeBundle;
    private float bestTimeClassic;

    public GameStats() {
        this.totalWordsSolved = 0;
        this.totalGuesses = 0;
        this.totalGuessesForSolvedWords = 0;
        this.totalTimeInSeconds = 0;
        this.bestWord = "";
        this.bundlesSolved = 0;
        this.classicsSolved = 0;
        this.bestWordGuesses = Integer.MAX_VALUE;
        this.bestTimeBundle = Float.MAX_VALUE;
        this.bestTimeClassic = Float.MAX_VALUE;
    }

    /**
     * Resets all game statistics to their initial values.
     * This should be called at the start of a new game.
     */
    public void reset() {
        this.totalWordsSolved = 0;
        this.totalGuesses = 0;
        this.totalGuessesForSolvedWords = 0;
        this.totalTimeInSeconds = 0;
        this.bestWord = "";
        this.bundlesSolved = 0;
        this.classicsSolved = 0;
        this.bestWordGuesses = Integer.MAX_VALUE;
        this.bestTimeBundle = Float.MAX_VALUE;
        this.bestTimeClassic = Float.MAX_VALUE;
    }

    /**
     * Updates the total guess count. This should be called every time a guess is made.
     */
    public void incrementTotalGuesses() {
        this.totalGuesses++;
    }

    /**
     * Increments the count for classic games solved.
     */
    public void incrementClassicsSolved() {
        this.classicsSolved++;
    }

    /**
     * Increments the count for bundle games solved.
     */
    public void incrementBundlesSolved() {
        this.bundlesSolved++;
    }

    /**
     * Updates the stats after a word is successfully solved.
     * @param guesses The number of guesses it took to solve the current word.
     */
    public void onWordSolved(String word, int guesses) {
        totalWordsSolved++;
        totalGuessesForSolvedWords += guesses;
        updateBestWord(word, guesses);
    }

    /**
     * Updates the best word guessed stat.
     * @param word The word that was guessed.
     * @param guesses The number of guesses it took.
     */
    public void updateBestWord(String word, int guesses) {
        if (guesses < bestWordGuesses) {
            bestWord = word;
            bestWordGuesses = guesses;
        } else if (guesses == bestWordGuesses) {
            // Keep the first word with the best guess
            if (bestWord.isEmpty()) {
                bestWord = word;
            }
        }
    }

    /**
     * Calculates the average number of guesses per solved word.
     * @return The average guesses, or 0 if no words have been solved.
     */
    public double getAverageGuesses() {
        if (totalWordsSolved == 0) {
            return 0.0;
        }
        return (double) totalGuessesForSolvedWords / totalWordsSolved;
    }

    public int getTotalWordsSolved() { return this.totalWordsSolved; }
    public void setTotalWordsSolved(int totalWordsSolved) { this.totalWordsSolved = totalWordsSolved; }

    public int getTotalGuesses() { return this.totalGuesses; }
    public void setTotalGuesses(int totalGuesses) { this.totalGuesses = totalGuesses; }

    public long getTotalTimeInSeconds() { return this.totalTimeInSeconds; }
    public void setTotalTimeInSeconds(long totalTimeInSeconds) { this.totalTimeInSeconds = totalTimeInSeconds; }

    public int getBundlesSolved() { return this.bundlesSolved; }
    public void setBundlesSolved(int bundlesSolved) { this.bundlesSolved = bundlesSolved; }

    public int getClassicsSolved() { return this.classicsSolved; }
    public void setClassicsSolved(int classicsSolved) { this.classicsSolved = classicsSolved; }

    public int getTotalGuessesForSolvedWords() { return this.totalGuessesForSolvedWords; }
    public void setTotalGuessesForSolvedWords(int totalGuessesForSolvedWords) { this.totalGuessesForSolvedWords = totalGuessesForSolvedWords; }

    public String getBestWord() { return this.bestWord; }
    public void setBestWord(String bestWord) { this.bestWord = bestWord; }

    public int getBestWordGuesses() { return this.bestWordGuesses; }
    public void setBestWordGuesses(int bestWordGuesses) { this.bestWordGuesses = bestWordGuesses; }

    public float getBestTimeBundle() { return bestTimeBundle; }
    public void setBestTimeBundle(float bestTimeBundle) { this.bestTimeBundle = bestTimeBundle; }

    public float getBestTimeClassic() { return bestTimeClassic; }
    public void setBestTimeClassic(float bestTimeClassic) { this.bestTimeClassic = bestTimeClassic; }
}
