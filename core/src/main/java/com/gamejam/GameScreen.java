package com.gamejam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
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

    // Variables for the spritesheet and animation
    private Texture rabbitIdleTexture;
    private Texture rabbitDeathTexture;
    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> deathAnimation;

    // Variables for the carrot animation
    private List<Texture> carrotIdleTextures = new ArrayList<>();
    private List<Texture> carrotDeathTextures = new ArrayList<>();
    private Animation<TextureRegion> carrotIdleAnimation;
    private Animation<TextureRegion> carrotDeathAnimation;
    private float stateTime; // Timer to keep track of the animation's state

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
    private String solutionWord;

    private final GlyphLayout layout = new GlyphLayout();

    // Durations for the different animation states
    private static final float WIN_ANIMATION_DURATION = 1.5f; // Duration for the all-green board flash
    private static final float STAGE_COMPLETE_DURATION = 5.0f; // Total duration for the win text display
    private static final float GAME_OVER_DURATION = 5.0f; // Total duration for the game over text display


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

        // --- LOAD RABBIT IDLE ANIMATION ---
        rabbitIdleTexture = new Texture(Gdx.files.internal("bunny/Spritesheets/spritesheet idle.png"));
        TextureRegion[][] idleTmp = TextureRegion.split(rabbitIdleTexture,
            rabbitIdleTexture.getWidth() / 4,
            rabbitIdleTexture.getHeight() / 1);
        TextureRegion[] idleFrames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            idleFrames[i] = idleTmp[0][i];
        }
        idleAnimation = new Animation<TextureRegion>(0.15f, idleFrames);
        stateTime = 0f;

        // --- LOAD RABBIT DEATH ANIMATION ---
        rabbitDeathTexture = new Texture(Gdx.files.internal("bunny/Spritesheets/spritesheet death.png"));
        TextureRegion[][] deathTmp = TextureRegion.split(rabbitDeathTexture,
            rabbitDeathTexture.getWidth() / 4,
            rabbitDeathTexture.getHeight() / 1);
        TextureRegion[] deathFrames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            deathFrames[i] = deathTmp[0][i];
        }
        deathAnimation = new Animation<TextureRegion>(0.15f, deathFrames);


        // --- LOAD CARROT IDLE ANIMATION ---
        TextureRegion[] carrotIdleFrames = new TextureRegion[5];
        for (int i = 0; i < 5; i++) {
            String carrotIdlePath = "carrot/Idle/Idle" + (i+1) + ".png";
            Texture frameTexture = new Texture(Gdx.files.internal(carrotIdlePath));
            carrotIdleTextures.add(frameTexture);
            TextureRegion carrotRegion  = new TextureRegion(frameTexture);
            carrotRegion.flip(true, false);
            carrotIdleFrames[i] = carrotRegion;
        }
        carrotIdleAnimation = new Animation<TextureRegion>(0.15f, carrotIdleFrames);

        // --- LOAD CARROT DEATH ANIMATION ---
        TextureRegion[] carrotDeathFrames = new TextureRegion[8];
        for (int i = 0; i < 8; i++) {
            String carrotDeathPath = "carrot/Death/Death" + (i+1) + ".png";
            Texture frameTexture = new Texture(Gdx.files.internal(carrotDeathPath));
            carrotDeathTextures.add(frameTexture);
            TextureRegion carrotRegion = new TextureRegion(frameTexture);
            carrotRegion.flip(true, false);
            carrotDeathFrames[i] = carrotRegion;
        }
        carrotDeathAnimation = new Animation<TextureRegion>(0.15f, carrotDeathFrames);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new GameInputProcessor());
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        stateTime += delta; // Update animation state time
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
                renderGameOverScene();
                if (stateTimer >= GAME_OVER_DURATION) {
                    game.setScreen(game.menuScreen);
                }
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

        // Use a consistent center point for positioning
        float centerY = Gdx.graphics.getHeight() / 2f - 100;

        // Position the tiles below the center
        float tilesY = centerY - 100;

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
                tile.renderShape(shapeRenderer, startX + (i * (tileSize + tileGap)), tilesY, tileSize, 1.0f, alpha);
            }
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND); // Disable blending after we're done with transparent shapes

        // --- SECOND PASS: Draw Sprites and Text ---
        batch.begin();
        font.setColor(1.0f, 1.0f, 1.0f, alpha);
        String message = "Stage Complete!";
        layout.setText(font, message);
        font.draw(batch, layout, (Gdx.graphics.getWidth() - layout.width) / 2f, Gdx.graphics.getHeight() - 150);

        // Add the "The word was:" text
        String wordMessage = "The word was:";
        layout.setText(font, wordMessage);
        // Position the text slightly above the tiles
        font.draw(batch, layout, (Gdx.graphics.getWidth() - layout.width) / 2f, tilesY + tileSize + 60);

        if (!solvedWords.isEmpty()) {
            int lastRow = solvedWords.size() - 1;
            String word = solvedWords.get(lastRow);
            TileState[] states = solvedWordStates.get(lastRow);
            for (int i = 0; i < 5; i++) {
                // Correctly render the tile letters for the solved word
                Tile tile = new Tile();
                tile.setLetter(word.charAt(i));
                tile.setState(states[i]);
                tile.renderText(batch, font, startX + (i * (tileSize + tileGap)), tilesY, tileSize, alpha);
            }
        }

        // --- DRAW THE BUNNY IDLE ANIMATION ---
        TextureRegion currentBunnyFrame = idleAnimation.getKeyFrame(stateTime, true);
        float scaleFactor = 5.0f;
        float scaledBunnyWidth = currentBunnyFrame.getRegionWidth() * scaleFactor;
        float scaledBunnyHeight = currentBunnyFrame.getRegionHeight() * scaleFactor;
        float screenWidth = Gdx.graphics.getWidth();
        float bunnyX = (screenWidth - scaledBunnyWidth) / 2f + 30;
        float bunnyY = centerY + 20;
        batch.setColor(1.0f, 1.0f, 1.0f, alpha);
        batch.draw(currentBunnyFrame, bunnyX, bunnyY, scaledBunnyWidth, scaledBunnyHeight);
        batch.setColor(Color.WHITE); // Reset color to default

        // --- DRAW THE CARROT IDLE ANIMATION ---
        TextureRegion currentCarrotFrame = carrotIdleAnimation.getKeyFrame(stateTime, true);
        float scaledCarrotWidth = currentCarrotFrame.getRegionWidth() * (scaleFactor - 2);
        float scaledCarrotHeight = currentCarrotFrame.getRegionHeight() * (scaleFactor - 2);
        // Position the carrot to the left of the bunny
        float carrotX = bunnyX - 20;
        float carrotY = bunnyY + 20;
        batch.setColor(1.0f, 1.0f, 1.0f, alpha);
        batch.draw(currentCarrotFrame, carrotX, carrotY, scaledCarrotWidth, scaledCarrotHeight);
        batch.setColor(Color.WHITE); // Reset color to default

        batch.end();
    }

    /**
     * Renders a custom scene for a game over state with a fade-in effect.
     */
    private void renderGameOverScene() {
        // Clear the screen to a solid color
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        // Calculate the current alpha for the fade-in effect
        float alpha = Math.min(1.0f, stateTimer / 1.0f); // Fades in over 1 second

        // Set up the scene elements' positions
        float tileSize = 64f;
        float tileGap = 10f;
        float totalWidth = (5 * tileSize) + (4 * tileGap);
        float startX = (Gdx.graphics.getWidth() - totalWidth) / 2f;

        // Use a consistent center point for positioning
        float centerY = Gdx.graphics.getHeight() / 2f - 100;

        // Position the tiles below the center
        float tilesY = centerY - 100;

        // --- FIRST PASS: Draw Shapes (FOR TILES AND BACKGROUND) ---
        Gdx.gl.glEnable(GL20.GL_BLEND); // Enable blending for transparency
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.15f, alpha);
        shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // RENDER THE TILE SHAPES FOR THE SOLUTION WORD
        String solution = gameManager.getStageWords().get(gameManager.getCurrentStage()).toUpperCase();
        TileState[] states = WordChecker.checkWord(solution, solution);
        for (int i = 0; i < 5; i++) {
            Tile tile = new Tile();
            tile.setLetter(solution.charAt(i));
            tile.setState(states[i]);
            tile.renderShape(shapeRenderer, startX + (i * (tileSize + tileGap)), tilesY, tileSize, 1.0f, alpha);
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND); // Disable blending after we're done with transparent shapes

        // --- SECOND PASS: Draw Sprites and Text ---
        batch.begin();
        font.setColor(1.0f, 1.0f, 1.0f, alpha);
        String message = "GAME OVER!";
        layout.setText(font, message);
        font.draw(batch, layout, (Gdx.graphics.getWidth() - layout.width) / 2f, Gdx.graphics.getHeight() - 150);

        // Add the "The word was:" text
        String wordMessage = "The word was:";
        layout.setText(font, wordMessage);
        // Position the text slightly above the tiles
        font.draw(batch, layout, (Gdx.graphics.getWidth() - layout.width) / 2f, tilesY + tileSize + 60);

        // RENDER THE TILE LETTERS FOR THE SOLUTION WORD
        for (int i = 0; i < 5; i++) {
            Tile tile = new Tile();
            tile.setLetter(solution.charAt(i));
            tile.setState(states[i]);
            tile.renderText(batch, font, startX + (i * (tileSize + tileGap)), tilesY, tileSize, alpha);
        }

        // --- DRAW THE BUNNY DEATH ANIMATION ---
        TextureRegion currentBunnyFrame = deathAnimation.getKeyFrame(stateTime, false); // No looping for death animation
        float scaleFactor = 5.0f;
        float scaledBunnyWidth = currentBunnyFrame.getRegionWidth() * scaleFactor;
        float scaledBunnyHeight = currentBunnyFrame.getRegionHeight() * scaleFactor;
        float screenWidth = Gdx.graphics.getWidth();
        float bunnyX = (screenWidth - scaledBunnyWidth) / 2f + 30;
        float bunnyY = centerY + 20;
        batch.setColor(1.0f, 1.0f, 1.0f, alpha);
        batch.draw(currentBunnyFrame, bunnyX, bunnyY, scaledBunnyWidth, scaledBunnyHeight);
        batch.setColor(Color.WHITE); // Reset color to default

        // --- DRAW THE CARROT DEATH ANIMATION ---
        TextureRegion currentCarrotFrame = carrotDeathAnimation.getKeyFrame(stateTime, false); // No looping for death animation
        float scaledCarrotWidth = currentCarrotFrame.getRegionWidth() * (scaleFactor - 2);
        float scaledCarrotHeight = currentCarrotFrame.getRegionHeight() * (scaleFactor - 2);
        // Position the carrot to the left of the bunny
        float carrotX = bunnyX - 20;
        float carrotY = bunnyY + 20;
        batch.setColor(1.0f, 1.0f, 1.0f, alpha);
        batch.draw(currentCarrotFrame, carrotX, carrotY, scaledCarrotWidth, scaledCarrotHeight);
        batch.setColor(Color.WHITE); // Reset color to default

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

    public void reset() {
        solvedWords.clear();
        solvedWordStates.clear();
        currentState = GameState.PLAYING;
        stateTimer = 0.0f;
        board.reset();
        keyboard.reset();
        // Reset stateTime when the game is reset
        stateTime = 0f;
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
        rabbitIdleTexture.dispose();
        rabbitDeathTexture.dispose();
        // Dispose of each carrot texture in the list
        for (Texture texture : carrotIdleTextures) {
            texture.dispose();
        }
        for (Texture texture : carrotDeathTextures) {
            texture.dispose();
        }
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
                            stateTimer = 0;
                            // Reset the animation timer specifically for the death animation
                            stateTime = 0;
                        } else {
                            board.advanceRow();
                        }
                    }
                } else if (keycode == Input.Keys.BACKSPACE) {
                    isBackspaceHeld = true;
                    board.deleteLetter();
                    backspaceHoldTimer = 0.0f;
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
