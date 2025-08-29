package com.gamejam;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to hold and manage game statistics.
 * This class is serializable, making it easy to save and load.
 */
public class GameStats {
    // Basic game stats
    public int totalWordsSolved;
    public int totalGuesses; // This now counts all guesses, including failed attempts
    public long totalTimeInSeconds; // Total time spent in game
    public int bundlesSolved;
    public double averageGuessPerWord;
    public int totalGuessesForSolvedWords; // New variable for the average calculation

    // Stats for "best word guessed"
    public String bestWord;
    public int bestWordGuesses;

    // Stats for "average guesses per solved word"
    public List<Integer> guessesPerWord;

    public GameStats() {
        this.totalWordsSolved = 0;
        this.totalGuesses = 0;
        this.totalGuessesForSolvedWords = 0;
        this.totalTimeInSeconds = 0;
        this.bestWord = "";
        this.bundlesSolved = 0;
        this.averageGuessPerWord = 0;
        this.bestWordGuesses = Integer.MAX_VALUE; // Initialize with a high value
        this.guessesPerWord = new ArrayList<>();
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
        this.bestWordGuesses = Integer.MAX_VALUE;
        this.guessesPerWord.clear();
    }

    /**
     * Updates the total guess count. This should be called every time a guess is made.
     */
    public void incrementTotalGuesses() {
        this.totalGuesses++;
    }

    /**
     * Updates the stats after a word is successfully solved.
     * @param guesses The number of guesses it took to solve the current word.
     */
    public void onWordSolved(String word, int guesses) {
        totalWordsSolved++;
        totalGuessesForSolvedWords += guesses;
        guessesPerWord.add(guesses);
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
            bestWord = word;
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

    public int getBundlesWon() {
        return this.bundlesSolved;
    }

    public String getBestWord() {
        return this.bestWord;
    }

    public int getBestWordGuesses() {
        return this.bestWordGuesses;
    }

    public int getTotalWordsSolved() {
        return this.totalWordsSolved;
    }
}
