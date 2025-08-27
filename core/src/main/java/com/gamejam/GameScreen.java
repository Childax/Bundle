package com.gamejam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the game states and screen transitions.
 * This class handles the logic for a single game screen, including displaying "all green"
 * and moving to the next stage.
 */
public class GameScreen implements Screen {

    private final Main game; // Reference to the main game class
    private final GameManager gameManager;
    private final Board board;
    private final Keyboard keyboard;

    // These resources are passed from the Main class
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private BitmapFont keyboardFont;

    // An enumeration of the different game states within this screen
    public enum GameState {
        PLAYING,
        WIN_ANIMATION, // New state to show the all-green board before transition
        STAGE_COMPLETE,
        GAME_OVER
    }

    private GameState currentState = GameState.PLAYING;
    private float stateTimer = 0.0f;

    // Backspace hold variables
    private boolean isBackspaceHeld = false;
    private float backspaceHoldTimer = 0.0f;
    private final float INITIAL_BACKSPACE_DELAY = 0.5f; // Delay before the first repeat
    private final float REPEAT_BACKSPACE_DELAY = 0.05f; // Delay for subsequent repeats

    // For keeping track of solved words
    private List<String> solvedWords = new ArrayList<>();
    private List<TileState[]> solvedWordStates = new ArrayList<>();

    private final GlyphLayout layout = new GlyphLayout();

    // Durations for the different animation states
    private static final float WIN_ANIMATION_DURATION = 1.5f; // Duration for the all-green board flash
    private static final float STAGE_COMPLETE_DURATION = 2.0f; // Total duration for the win text display

    public GameScreen(Main game, GameManager gameManager, Board board, Keyboard keyboard) {
        this.game = game;
        this.gameManager = gameManager;
        this.board = board;
        this.keyboard = keyboard;

        // Get shared resources from the Main class
        this.batch = game.getBatch();
        this.shapeRenderer = game.getShapeRenderer();
        this.font = game.getFont();
        this.keyboardFont = game.getKeyboardFont();
    }

    @Override
    public void show() {
        // This is the crucial fix! Always set the InputProcessor when the screen becomes active.
        // This ensures the game screen can receive keyboard and touch input after returning from the menu.
        Gdx.input.setInputProcessor(new GameInputProcessor());
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        // Update the state timer
        stateTimer += delta;

        // Check for backspace hold in the main render loop
        if (isBackspaceHeld) {
            backspaceHoldTimer += Gdx.graphics.getDeltaTime();
            if (backspaceHoldTimer > INITIAL_BACKSPACE_DELAY) {
                if (backspaceHoldTimer - Gdx.graphics.getDeltaTime() <= INITIAL_BACKSPACE_DELAY) {
                    board.deleteLetter();
                } else {
                    if (backspaceHoldTimer > INITIAL_BACKSPACE_DELAY + REPEAT_BACKSPACE_DELAY) {
                        board.deleteLetter();
                        backspaceHoldTimer = INITIAL_BACKSPACE_DELAY;
                    }
                }
            }
        }

        // Use a switch statement to handle logic for each state
        switch (currentState) {
            case PLAYING:
                renderPlayingState();
                break;

            case WIN_ANIMATION:
                // Display the board with the final guess all green
                renderPlayingState();
                // Check if the animation duration has passed
                if (stateTimer >= WIN_ANIMATION_DURATION) {
                    stateTimer = 0;
                    currentState = GameState.STAGE_COMPLETE;
                }
                break;

            case STAGE_COMPLETE:
                renderStageCompleteText();
                // Now, check for the transition to the next stage
                if (stateTimer >= STAGE_COMPLETE_DURATION) {
                    stateTimer = 0;
                    loadNextStage();
                }
                break;

            case GAME_OVER:
                drawGameOverScreen();
                break;
        }
    }

    /**
     * Renders the game in the playing state.
     */
    private void renderPlayingState() {
        board.render(batch, shapeRenderer, font);
        keyboard.render(batch, shapeRenderer, keyboardFont, 1024);
        drawStageNumber();
    }

    /**
     * Renders the "ALL GREEN" text with a given alpha value.
     */
    private void renderStageCompleteText() {
        ScreenUtils.clear(Color.GREEN); // Set a green background for the "ALL GREEN" text

        String text = "ALL GREEN";
        layout.setText(font, text); // Set the text for the layout
        float textX = (Gdx.graphics.getWidth() - layout.width) / 2;
        float textY = Gdx.graphics.getHeight() / 2;

        batch.begin();
        font.setColor(Color.WHITE); // Keep font color solid
        font.draw(batch, layout, textX, textY);
        batch.end();
    }

    private void drawStageNumber() {
        // Calculate the top of the board to position the stage number relative to it
        float tileSize = 64f;
        float gap = 10f;
        int rows = 6;
        float boardHeight = rows * tileSize + (rows - 1) * gap;
        float startY = (Gdx.graphics.getHeight() + boardHeight) / 2f + 50;

        batch.begin();
        keyboardFont.setColor(Color.WHITE); // Use the smaller font
        keyboardFont.getData().setScale(0.9f);
        String stageText = "Stage " + (gameManager.getCurrentStage() + 1);
        layout.setText(keyboardFont, stageText);
        float textX = (Gdx.graphics.getWidth() - layout.width) / 2f;
        float textY = startY + 105;
        keyboardFont.draw(batch, stageText, textX, textY);
        keyboardFont.getData().setScale(1f);
        batch.end();
    }

    private void drawGameOverScreen() {
        batch.begin();
        font.setColor(Color.WHITE);
        String message;
        if (gameManager.isFinalWin()) {
            message = "YOU WIN! ALL STAGES COMPLETE!";
        } else {
            String solution = gameManager.getStageWords().get(gameManager.getCurrentStage());
            message = "YOU LOSE! The word was " + solution.toUpperCase();
        }

        layout.setText(font, message);
        font.draw(batch, layout, Gdx.graphics.getWidth() / 2f - layout.width / 2, Gdx.graphics.getHeight() / 2f + 50);

        layout.setText(font, "Press Enter to return to menu");
        font.draw(batch, layout, Gdx.graphics.getWidth() / 2f - layout.width / 2, Gdx.graphics.getHeight() / 2f - 50);

        batch.end();
    }

    /**
     * Resets the game screen for a new game.
     */
    public void reset() {
        solvedWords.clear();
        solvedWordStates.clear();
        currentState = GameState.PLAYING;
        stateTimer = 0.0f;
        board.reset();
        keyboard.reset();
    }

    /**
     * Loads the next stage.
     */
    private void loadNextStage() {
        if (!gameManager.advanceStage()) {
            // All stages complete
            gameManager.setFinalWin(true);
            currentState = GameState.GAME_OVER;
        } else {
            board.reset();
            keyboard.reset();

            // Re-apply solved words for the new stage
            int currentStage = gameManager.getCurrentStage();
            String currentAnswer = gameManager.getStageWords().get(currentStage);

            for (int i = 0; i < solvedWords.size(); i++) {
                String word = solvedWords.get(i);
                TileState[] states = WordChecker.checkWord(word, currentAnswer.toUpperCase());
                board.loadSolvedWord(word, states);
                for (int c = 0; c < 5; c++) {
                    keyboard.updateKeyState(word.charAt(c), states[c]);
                }
            }
            currentState = GameState.PLAYING;
        }
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }

    private class GameInputProcessor implements InputProcessor {
        @Override
        public boolean keyDown(int keycode) {
            if (currentState == GameState.PLAYING) {
                if (keycode >= Input.Keys.A && keycode <= Input.Keys.Z) {
                    char letter = (char) ('A' + (keycode - Input.Keys.A));
                    board.typeLetter(letter);
                } else if (keycode == Input.Keys.ENTER) {
                    String submittedWord = board.getSubmittedWord();
                    int currentRow = board.getCurrentRow(); // Get current row before submitting
                    TileState[] result = board.submitGuess(currentRow); // Pass the row to the board

                    if (result != null) {
                        for (int c = 0; c < 5; c++) {
                            char letter = submittedWord.charAt(c);
                            keyboard.updateKeyState(letter, result[c]);
                        }

                        if (gameManager.isStageSolved()) {
                            // Stage solved, enter the win animation state.
                            solvedWords.add(submittedWord);
                            solvedWordStates.add(result);
                            currentState = GameState.WIN_ANIMATION;
                            stateTimer = 0;
                        } else if (gameManager.isGameOver()) {
                            currentState = GameState.GAME_OVER;
                        } else {
                            board.advanceRow();
                        }
                    }
                } else if (keycode == Input.Keys.BACKSPACE) {
                    isBackspaceHeld = true;
                    board.deleteLetter();
                    backspaceHoldTimer = 0.0f;
                }
            } else if (currentState == GameState.GAME_OVER) {
                if (keycode == Input.Keys.ENTER) {
                    game.setScreen(game.menuScreen);
                }
            }
            return true;
        }

        @Override
        public boolean keyUp(int keycode) {
            if (keycode == Input.Keys.BACKSPACE) {
                isBackspaceHeld = false;
                backspaceHoldTimer = 0.0f;
            }
            return false;
        }

        @Override public boolean keyTyped(char character) { return false; }
        @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
        @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
        @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
        @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
        @Override public boolean scrolled(float amountX, float amountY) { return false; }
        @Override public boolean touchCancelled(int x, int y, int z, int button) { return false; }
    }
}
