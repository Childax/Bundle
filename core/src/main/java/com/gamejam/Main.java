package com.gamejam;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
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

    // Game state variables
    private enum GameState {
        MENU,
        PLAYING,
        HOW_TO_PLAY,
        GAME_OVER
    }
    private GameState gameState;

    // Menu button variables
    private final GlyphLayout layout = new GlyphLayout();
    private float playButtonX, playButtonY, playButtonWidth, playButtonHeight;
    private float howToPlayButtonX, howToPlayButtonY, howToPlayButtonWidth, howToPlayButtonHeight;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = FontLoader.loadFont("fonts/HelveticaNeue-BlackCond.otf", 48);
        keyboardFont = FontLoader.loadFont("fonts/HelveticaNeue-BlackCond.otf", 32);

        // Start the game in the menu state
        gameState = GameState.MENU;

        Gdx.input.setInputProcessor(this);
    }

    private void startNewGame() {
        gameManager = new GameManager(6);
        board = new Board(gameManager);
        keyboard = new Keyboard();
        solvedWords.clear();
        solvedWordStates.clear();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        switch (gameState) {
            case MENU:
                drawMenu();
                break;
            case PLAYING:
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

                board.render(batch, shapeRenderer, font);
                keyboard.render(batch, shapeRenderer, keyboardFont, 1024);

                // Draw stage number
                drawStageNumber();
                break;
            case HOW_TO_PLAY:
                drawHowToPlayScreen();
                break;
            case GAME_OVER:
                drawGameOverScreen();
                break;
        }
    }

    private void drawMenu() {
        float centerX = Gdx.graphics.getWidth() / 2f;
        float centerY = Gdx.graphics.getHeight() / 2f;

        batch.begin();
        font.setColor(Color.WHITE);

        // Draw title
        layout.setText(font, "WORDLE");
        font.draw(batch, layout, centerX - layout.width / 2, centerY + 150);

        // Draw "Play" button
        layout.setText(font, "Play");
        playButtonWidth = layout.width + 60;
        playButtonHeight = layout.height + 30;
        playButtonX = centerX - playButtonWidth / 2;
        playButtonY = centerY - 50;

        // Draw "How to play" button
        layout.setText(font, "How to play");
        howToPlayButtonWidth = layout.width + 60;
        howToPlayButtonHeight = layout.height + 30;
        howToPlayButtonX = centerX - howToPlayButtonWidth / 2;
        howToPlayButtonY = centerY - 150;

        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(playButtonX, playButtonY, playButtonWidth, playButtonHeight);
        shapeRenderer.rect(howToPlayButtonX, howToPlayButtonY, howToPlayButtonWidth, howToPlayButtonHeight);
        shapeRenderer.end();

        batch.begin();
        font.draw(batch, "Play", playButtonX + 30, playButtonY + playButtonHeight - 15);
        font.draw(batch, "How to play", howToPlayButtonX + 30, howToPlayButtonY + howToPlayButtonHeight - 15);
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

    private void drawHowToPlayScreen() {
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "How to play", Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f + 50);
        font.draw(batch, "Coming soon!", Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f - 50);
        batch.end();
    }

    private void drawGameOverScreen() {
        batch.begin();
        font.setColor(Color.WHITE);
        String message;
        String solution = gameManager.getStageWords().get(gameManager.getCurrentStage());
        if (gameManager.isStageSolved()) {
            message = "You Win!";
        } else {
            message = "You Lose! The word was " + solution.toUpperCase();
        }

        layout.setText(font, message);
        font.draw(batch, layout, Gdx.graphics.getWidth() / 2f - layout.width / 2, Gdx.graphics.getHeight() / 2f + 50);

        layout.setText(font, "Press Enter to return to menu");
        font.draw(batch, layout, Gdx.graphics.getWidth() / 2f - layout.width / 2, Gdx.graphics.getHeight() / 2f - 50);

        batch.end();
    }

    @Override
    public boolean keyDown(int keycode) {
        if (gameState == GameState.PLAYING) {
            if (keycode >= Input.Keys.A && keycode <= Input.Keys.Z) {
                char letter = (char) ('A' + (keycode - Input.Keys.A));
                board.typeLetter(letter);
            } else if (keycode == Input.Keys.ENTER) {
                String submittedWord = board.getSubmittedWord();
                int currentRow = board.getCurrentRow(); // Get current row before submitting
                TileState[] result = board.submitGuess(currentRow); // Pass the row to the board

                if (result != null) {
                    // Update keyboard and advance the board's row
                    for (int c = 0; c < 5; c++) {
                        char letter = submittedWord.charAt(c);
                        keyboard.updateKeyState(letter, result[c]);
                    }

                    // Advance the row regardless of win/loss
                    board.advanceRow();

                    if (gameManager.isStageSolved()) {
                        solvedWords.add(submittedWord);
                        solvedWordStates.add(result);

                        if (!gameManager.advanceStage()) {
                            gameState = GameState.GAME_OVER;
                        } else {
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
                    } else if (gameManager.isGameOver()) {
                        gameState = GameState.GAME_OVER;
                    }
                }
            } else if (keycode == Input.Keys.BACKSPACE) {
                isBackspaceHeld = true;
                board.deleteLetter();
                backspaceHoldTimer = 0.0f;
            }
        } else if (gameState == GameState.GAME_OVER) {
            if (keycode == Input.Keys.ENTER) {
                gameState = GameState.MENU;
            }
        }
        return true;
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        // Invert Y coordinate for screen space
        float screenY = Gdx.graphics.getHeight() - y;

        if (gameState == GameState.MENU) {
            if (x >= playButtonX && x <= playButtonX + playButtonWidth &&
                screenY >= playButtonY && screenY <= playButtonY + playButtonHeight) {
                startNewGame();
                gameState = GameState.PLAYING;
            } else if (x >= howToPlayButtonX && x <= howToPlayButtonX + howToPlayButtonWidth &&
                screenY >= howToPlayButtonY && screenY <= howToPlayButtonY + howToPlayButtonHeight) {
                gameState = GameState.HOW_TO_PLAY;
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

    // unused InputProcessor methods
    @Override public boolean keyTyped(char character) { return false; }
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
