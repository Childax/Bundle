package com.gamejam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.*;

public class GameManager {
    private String solution;
    private Set<String> validWords;
    private List<String> answers;
    private boolean gameOver;

    public GameManager() {
        loadWordLists();
        pickNewSolution();
    }

    private void loadWordLists() {
        try {
            // Load solutions
            FileHandle solFile = Gdx.files.internal("words/wordle-answers.txt");
            answers = Arrays.asList(solFile.readString().split("\\r?\\n"));

            // Load valid guesses
            FileHandle guessFile = Gdx.files.internal("words/valid-wordle-words.txt");
            validWords = new HashSet<>(Arrays.asList(guessFile.readString().split("\\r?\\n")));

            // Solutions should also be valid guesses
            validWords.addAll(answers);

        } catch (Exception e) {
            answers = new ArrayList<>();
            validWords = new HashSet<>();
            System.out.println("Failed to load word lists: " + e.getMessage());
        }
    }

    public void pickNewSolution() {
        Random r = new Random();
        solution = answers.get(r.nextInt(answers.size()));
        gameOver = false;
        System.out.println("DEBUG: New solution is " + solution); // for testing
    }

    public TileState[] submitGuess(String guess) {
        if (gameOver) return null;

        guess = guess.toLowerCase();

        if (!validWords.contains(guess)) {
            System.out.println("Not in word list!");
            return null;
        }

        TileState[] result = WordChecker.checkWord(guess, solution);

        if (guess.equals(solution)) {
            System.out.println("You win!");
            gameOver = true;
        }

        return result;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public String getSolution() {
        return solution;
    }
}

