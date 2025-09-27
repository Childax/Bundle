package com.gamejam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.*;

/**
 * Manages the core game logic, including word selection, state tracking, and validation.
 */
public class GameManager {
    private List<String> stageWords;
    private Set<String> validWords;
    private List<String> answers;
    private final int numStages;
    private int currentStage;
    private boolean stageSolved;
    private boolean gameOver;
    private boolean isFinalWin = false;

    public GameManager(int numStages) {
        this.numStages = numStages;
        loadWordLists();
        pickNewSolutions();
    }

    /**
     * Resets the game manager's state for a new game.
     * This is the crucial fix to prevent crashes by resetting all state variables.
     */
    public void reset() {
        this.currentStage = 0;
        this.stageSolved = false;
        this.gameOver = false;
        this.isFinalWin = false;
    }

    private void loadWordLists() {
        try {
            // Load solutions
            FileHandle solFile = Gdx.files.internal("words/wordle-answers.txt");
            answers = Arrays.asList(solFile.readString().split("\\r?\\n"));

            // Load valid guesses
            FileHandle guessFile = Gdx.files.internal("words/valid-wordle-words.txt");
            validWords = new HashSet<>(Arrays.asList(guessFile.readString().split("\\r?\\n")));
            validWords.addAll(answers);

        } catch (Exception e) {
            answers = new ArrayList<>();
            validWords = new HashSet<>();
            System.out.println("Failed to load word lists: " + e.getMessage());
        }
    }

    /**
     * Picks new words for the game and resets the game state.
     */
    public void pickNewSolutions() {
        reset();

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
        System.out.println("DEBUG: Words are: ");
        assert stageWords != null;
        for (String word : stageWords) {
            System.out.println(word);
        }
    }

    /**
     * Submits a guess for the current stage.
     * @param guess The word guessed by the player.
     * @param currentRow The current row number on the board (0-indexed).
     * @return The array of TileStates for the guessed word.
     */
    public TileState[] submitGuess(String guess, int currentRow) {
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
        } else if (currentRow == 5) {
            gameOver = true;
            System.out.println("You lose!");
        }

        return result;
    }

    public boolean advanceStage() {
        currentStage++;
        stageSolved = false;
        if (currentStage >= numStages) {
            gameOver = true;
            return false;
        }
        return true;
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

    /**
     * Sets the final win state of the game.
     * @param finalWin The final win state to set.
     */
    public void setFinalWin(boolean finalWin) {
        isFinalWin = finalWin;
    }
}
