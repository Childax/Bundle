package com.gamejam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the game states and screen transitions.
 * This class handles the logic for a single game screen, including displaying "all green"
 * and moving to the next stage. It also implements a fade-in effect for the stage complete screen.
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

    // Variables for the sprites
    private Texture rabbitTexture;
    private Sprite rabbitSprite;
    private Sprite carrotSprite; // New sprite for the carrot

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

        // Load the spritesheet and create the sprites
        rabbitTexture = new Texture(Gdx.files.internal("bunny/Spritesheets/spritesheet idle.png"));
        // Assuming the rabbit is at (0, 0) and the carrot is at (64, 0) in the spritesheet
        rabbitSprite = new Sprite(new TextureRegion(rabbitTexture, 0, 0, 32, 32));
        carrotSprite = new Sprite(new TextureRegion(rabbitTexture, 64, 0, 32, 32));
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new GameInputProcessor());
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        stateTimer += delta;

        // Process backspace hold
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

        switch (currentState) {
            case PLAYING:
                renderPlayingState();
                break;
            case WIN_ANIMATION:
                renderPlayingState();
                if (stateTimer >= WIN_ANIMATION_DURATION) {
                    stateTimer = 0;
                    currentState = GameState.STAGE_COMPLETE;
                }
                break;
            case STAGE_COMPLETE:
                renderStageCompleteScene();
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
        // Correctly call the board's two-pass render method.
        // We handle the begin/end calls at the GameScreen level.
        board.render(batch, shapeRenderer, font, 1.0f); // Pass alpha of 1.0 since it's a non-fading state
        keyboard.render(batch, shapeRenderer, keyboardFont, 1024);
        drawStageNumber();
    }

    /**
     * Renders a custom scene for a completed stage with a fade-in effect.
     */
    private void renderStageCompleteScene() {
        // Clear the screen to a solid color
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        // Calculate the current alpha for the fade-in effect
        float alpha = Math.min(1.0f, stateTimer / 1.0f); // Fades in over 1 second

        // Set up the scene elements' positions
        float tileSize = 64f;
        float tileGap = 10f;
        float totalWidth = (5 * tileSize) + (4 * tileGap);
        float startX = (Gdx.graphics.getWidth() - totalWidth) / 2f;
        float startY = (Gdx.graphics.getHeight() / 2f) + 100;

        // --- FIRST PASS: Draw Shapes ---
        Gdx.gl.glEnable(GL20.GL_BLEND); // Enable blending for transparency
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.15f, alpha);
        shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        if (!solvedWords.isEmpty()) {
            int lastRow = solvedWords.size() - 1;
            String word = solvedWords.get(lastRow);
            TileState[] states = solvedWordStates.get(lastRow);
            for (int i = 0; i < 5; i++) {
                // Correctly render the tile shapes for the solved word
                Tile tile = new Tile();
                tile.setLetter(word.charAt(i));
                tile.setState(states[i]);
                tile.renderShape(shapeRenderer, startX + (i * (tileSize + tileGap)), startY, tileSize, 1.0f, alpha);
            }
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND); // Disable blending after we're done with transparent shapes

        // --- SECOND PASS: Draw Sprites and Text ---
        batch.begin();
        font.setColor(1.0f, 1.0f, 1.0f, alpha);
        String message = "Stage Complete!";
        layout.setText(font, message);
        font.draw(batch, layout, (Gdx.graphics.getWidth() - layout.width) / 2f, Gdx.graphics.getHeight() - 50);

        if (!solvedWords.isEmpty()) {
            int lastRow = solvedWords.size() - 1;
            String word = solvedWords.get(lastRow);
            TileState[] states = solvedWordStates.get(lastRow);
            for (int i = 0; i < 5; i++) {
                // Correctly render the tile letters for the solved word
                Tile tile = new Tile();
                tile.setLetter(word.charAt(i));
                tile.setState(states[i]);
                tile.renderText(batch, font, startX + (i * (tileSize + tileGap)), startY, tileSize, alpha);
            }
        }

        // Draw the rabbit and carrot sprites with the fade-in alpha
        float centerX = Gdx.graphics.getWidth() / 2f;
        float spriteY = (Gdx.graphics.getHeight() / 2f) - 100;

        rabbitSprite.setPosition(centerX - 50, spriteY);
        rabbitSprite.setColor(1.0f, 1.0f, 1.0f, alpha);
        rabbitSprite.draw(batch);

        carrotSprite.setPosition(centerX + 10, spriteY);
        carrotSprite.setColor(1.0f, 1.0f, 1.0f, alpha);
        carrotSprite.draw(batch);

        batch.end();
    }

    private void drawStageNumber() {
        // These begin/end calls are correct here because it's a single, self-contained draw action
        batch.begin();
        keyboardFont.setColor(Color.WHITE);
        keyboardFont.getData().setScale(0.9f);
        String stageText = "Stage " + (gameManager.getCurrentStage() + 1);
        layout.setText(keyboardFont, stageText);
        float textX = (Gdx.graphics.getWidth() - layout.width) / 2f;
        float textY = Gdx.graphics.getHeight() - 15;
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

    public void reset() {
        solvedWords.clear();
        solvedWordStates.clear();
        currentState = GameState.PLAYING;
        stateTimer = 0.0f;
        board.reset();
        keyboard.reset();
    }

    private void loadNextStage() {
        if (!gameManager.advanceStage()) {
            gameManager.setFinalWin(true);
            currentState = GameState.GAME_OVER;
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
        rabbitTexture.dispose();
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
                    int currentRow = board.getCurrentRow();
                    TileState[] result = board.submitGuess(currentRow);

                    if (result != null) {
                        for (int c = 0; c < 5; c++) {
                            char letter = submittedWord.charAt(c);
                            keyboard.updateKeyState(letter, result[c]);
                        }

                        if (gameManager.isStageSolved()) {
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
