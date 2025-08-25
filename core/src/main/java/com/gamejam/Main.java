package com.gamejam;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter implements InputProcessor {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private BitmapFont keyboardFont;

    private Board board;
    private Keyboard keyboard;
    private GameManager gameManager;
    private List<String> solvedWords = new ArrayList<>();
    private List<TileState[]> solvedWordStates = new ArrayList<>();

    // Backspace hold variables
    private boolean isBackspaceHeld = false;
    private float backspaceHoldTimer = 0.0f;
    private final float INITIAL_BACKSPACE_DELAY = 0.5f; // Delay before the first repeat
    private final float REPEAT_BACKSPACE_DELAY = 0.05f; // Delay for subsequent repeats

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = FontLoader.loadFont("fonts/HelveticaNeue-BlackCond.otf", 48);
        keyboardFont = FontLoader.loadFont("fonts/HelveticaNeue-BlackCond.otf", 32);

        gameManager = new GameManager(6);
        board = new Board(gameManager);
        keyboard = new Keyboard();

        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        // Check for backspace hold in the main render loop
        if (isBackspaceHeld) {
            backspaceHoldTimer += Gdx.graphics.getDeltaTime();

            // Handle initial deletion after a brief delay
            if (backspaceHoldTimer > INITIAL_BACKSPACE_DELAY) {
                if (backspaceHoldTimer - Gdx.graphics.getDeltaTime() <= INITIAL_BACKSPACE_DELAY) {
                    board.deleteLetter();
                } else {
                    // Handle rapid repeat deletions
                    if (backspaceHoldTimer > INITIAL_BACKSPACE_DELAY + REPEAT_BACKSPACE_DELAY) {
                        board.deleteLetter();
                        backspaceHoldTimer = INITIAL_BACKSPACE_DELAY; // Reset timer to repeat
                    }
                }
            }
        }

        board.render(batch, shapeRenderer, font);
        keyboard.render(batch, shapeRenderer, keyboardFont, 1024);
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode >= Input.Keys.A && keycode <= Input.Keys.Z) {
            char letter = (char) ('A' + (keycode - Input.Keys.A));
            board.typeLetter(letter);
        } else if (keycode == Input.Keys.ENTER) {
            String submittedWord = board.getSubmittedWord();
            TileState[] result = board.submitGuess();

            if (result != null) {
                for (int c = 0; c < 5; c++) {
                    char letter = submittedWord.charAt(c);
                    keyboard.updateKeyState(letter, result[c]);
                }

                if (gameManager.isStageSolved() && !gameManager.isGameOver()) {
                    // Store the solved word and its final, correct state
                    solvedWords.add(submittedWord);
                    solvedWordStates.add(result);

                    if (!gameManager.advanceStage()) return false;

                    board.reset();
                    keyboard.reset();

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
                }
            }
        } else if (keycode == Input.Keys.BACKSPACE) {
            // This handles the initial press
            isBackspaceHeld = true;
            board.deleteLetter();
            backspaceHoldTimer = 0.0f;
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

    // unused InputProcessor methods
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchDown(int x, int y, int pointer, int button) { return false; }
    @Override public boolean touchUp(int x, int y, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int x, int y, int pointer) { return false; }
    @Override public boolean mouseMoved(int x, int y) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
    @Override public boolean touchCancelled(int x, int y, int z, int button) { return false; }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }
}
