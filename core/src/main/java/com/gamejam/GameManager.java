package com.gamejam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.*;

public class GameManager {
    private List<String> stageWords;
    private Set<String> validWords;
    private List<String> answers;
    private final int numStages;
    private int currentStage;
    private boolean stageSolved;
    private boolean gameOver;

    public GameManager(int numStages) {
        this.numStages = numStages;
        this.currentStage = 0;
        loadWordLists();
        pickNewSolutions();
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

    public void pickNewSolutions() {
        this.stageWords = new ArrayList<>();
        Random r = new Random();
        for (int i = 0; i < numStages; i++) {
            String word = answers.get(r.nextInt(answers.size()));
            while (true) {
                assert stageWords != null;
                if (!stageWords.contains(word)) break;
                word = answers.get(r.nextInt(answers.size()));
            }
            stageWords.add(word);
        }
        gameOver = false;
        System.out.println("DEBUG: Words are: ");
        assert stageWords != null;
        for (String word : stageWords) {
            System.out.println(word);
        }
    }

    public TileState[] submitGuess(String guess) {
        if (gameOver) return null;

        String solution = stageWords.get(currentStage);

        guess = guess.toLowerCase();

        if (!validWords.contains(guess)) {
            System.out.println("Not in word list!");
            return null;
        }

        TileState[] result = WordChecker.checkWord(guess, solution);

        if (guess.equals(solution)) {
            System.out.println("Stage solved!");
            stageSolved = true;
            currentStage++;
            if (currentStage >= numStages) {
                gameOver = true;
                System.out.println("You win!");
            }
        }

        return result;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public List<String> getStageWords() {
        return stageWords;
    }

    public boolean isStageSolved() {
        return stageSolved;
    }

    public int getCurrentStage() {
        return currentStage;
    }
}

