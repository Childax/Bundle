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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages the game states and screen transitions.
 * This class handles the logic for a single game screen, including displaying "all green"
 * and moving to the next stage.
 * It also implements a fade-in effect for the stage complete screen.
 */
public class GameScreen implements Screen {

    private final Main game;
    // Reference to the main game class
    private final GameManager gameManager;
    private final Board board;
    private final Keyboard keyboard;
    private final PlayerProfile playerProfile;
    private String sessionBestWord = "";
    private int sessionBestWordGuesses = Integer.MAX_VALUE;
    // New variables for session-specific stats
    private int sessionWordsSolved = 0;
    private int sessionGuesses = 0;
    // Timer variable
    private long startTime;
    // Variable to store the final elapsed time
    private float finalElapsedTime;
    // These resources are passed from the Main class
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private BitmapFont keyboardFont;

    // Variables for the spritesheet and animation
    private Texture rabbitIdleTexture;
    private Texture rabbitDeathTexture;
    private Texture rabbitDashTexture;
    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> deathAnimation;
    private Animation<TextureRegion> dashAnimation;
    // Variables for the carrot animation
    private List<Texture> carrotIdleTextures = new ArrayList<>();
    private List<Texture> carrotDeathTextures = new ArrayList<>();
    private List<Texture> carrotSpinTextures = new ArrayList<>();
    private Animation<TextureRegion> carrotIdleAnimation;
    private Animation<TextureRegion> carrotDeathAnimation;
    private Animation<TextureRegion> carrotSpinAnimation;
    private float stateTime; // Timer to keep track of the animation's state

    // An enumeration of the different game states within this screen
    public enum GameState {
        PLAYING,
        WIN_ANIMATION, // New state to show the all-green board before transition
        STAGE_COMPLETE,
        GAME_OVER,
        WIN_SCREEN, // The new final win screen state
        LOSE_SCREEN // The new lose screen state
    }

    public enum GameMode {
        CLASSIC,
        BUNDLE
    }

    private GameState currentState = GameState.PLAYING;
    private GameMode gameMode;
    private float stateTimer = 0.0f;
    // Backspace hold variables
    private boolean isBackspaceHeld = false;
    private float backspaceHoldTimer = 0.0f;
    private final float INITIAL_BACKSPACE_DELAY = 0.5f;
    // Delay before the first repeat
    private final float REPEAT_BACKSPACE_DELAY = 0.05f;
    // Delay for subsequent repeats

    // For keeping track of solved words
    private List<String> solvedWords = new ArrayList<>();
    private List<TileState[]> solvedWordStates = new ArrayList<>();
    private String solutionWord;

    private final GlyphLayout layout = new GlyphLayout();
    // Durations for the different animation states
    private static final float WIN_ANIMATION_DURATION = 1.5f;
    // Duration for the all-green board flash
    private static final float STAGE_COMPLETE_DURATION = 5.0f;
    // Total duration for the win text display
    private static final float GAME_OVER_DURATION = 5.0f;
    // Total duration for the game over text display

    // Back to Menu button bounds.
    // Using the same name as before for the quit button for consistency.
    private Rectangle menuButtonBounds;
    private Rectangle quitButtonBounds;
    // New texture for the goblin
    private Texture goblinCryingTexture;
    public GameScreen(Main game, GameManager gameManager, Board board, Keyboard keyboard, PlayerProfile playerProfile) {
        this.game = game;
        this.gameManager = gameManager;
        this.board = board;
        this.keyboard = keyboard;
        this.playerProfile = playerProfile;
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
        // --- LOAD RABBIT DASH ANIMATION ---
        rabbitDashTexture = new Texture(Gdx.files.internal("bunny/Spritesheets/spritesheet dash.png"));
        TextureRegion[][] dashTmp = TextureRegion.split(rabbitDashTexture,
            rabbitDashTexture.getWidth() / 4,
            rabbitDashTexture.getHeight() / 1);
        TextureRegion[] dashFrames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            dashFrames[i] = dashTmp[0][i];
        }
        dashAnimation = new Animation<TextureRegion>(0.15f, dashFrames);
        // --- LOAD CARROT IDLE ANIMATION ---
        TextureRegion[] carrotIdleFrames = new TextureRegion[5];
        for (int i = 0; i < 5; i++) {
            String carrotIdlePath = "carrot/Idle/Idle" + (i + 1) + ".png";
            Texture frameTexture = new Texture(Gdx.files.internal(carrotIdlePath));
            carrotIdleTextures.add(frameTexture);
            TextureRegion carrotRegion = new TextureRegion(frameTexture);
            carrotRegion.flip(true, false);
            carrotIdleFrames[i] = carrotRegion;
        }
        carrotIdleAnimation = new Animation<TextureRegion>(0.15f, carrotIdleFrames);
        // --- LOAD CARROT DEATH ANIMATION ---
        TextureRegion[] carrotDeathFrames = new TextureRegion[8];
        for (int i = 0; i < 8; i++) {
            String carrotDeathPath = "carrot/Death/Death" + (i + 1) + ".png";
            Texture frameTexture = new Texture(Gdx.files.internal(carrotDeathPath));
            carrotDeathTextures.add(frameTexture);
            TextureRegion carrotRegion = new TextureRegion(frameTexture);
            carrotRegion.flip(true, false);
            carrotDeathFrames[i] = carrotRegion;
        }
        carrotDeathAnimation = new Animation<TextureRegion>(0.15f, carrotDeathFrames);
        // --- LOAD CARROT SPIN ANIMATION ---
        TextureRegion[] carrotSpinFrames = new TextureRegion[20];
        for (int i = 0; i < 20; i++) {
            String carrotSpinPath = "carrot/Spin/Carrot" + (i + 1) + ".png";
            Texture frameTexture = new Texture(Gdx.files.internal(carrotSpinPath));
            carrotSpinTextures.add(frameTexture);
            TextureRegion carrotRegion = new TextureRegion(frameTexture);
            carrotRegion.flip(true, false);
            carrotSpinFrames[i] = carrotRegion;
        }
        carrotSpinAnimation = new Animation<TextureRegion>(0.15f, carrotSpinFrames);
        // --- LOAD GOBLIN CRYING IMAGE ---
        goblinCryingTexture = new Texture(Gdx.files.internal("goblin/goblin crying.png"));
        // Initialize the quit button bounds at the top right
        float quitButtonWidth = 200;
        float quitButtonHeight = 50;
        float padding = 20;
        quitButtonBounds = new Rectangle(
            Gdx.graphics.getWidth() - quitButtonWidth - padding,
            Gdx.graphics.getHeight() - quitButtonHeight - padding,
            quitButtonWidth,
            quitButtonHeight
        );
        // Initialize the menu button bounds at the center bottom
        menuButtonBounds = new Rectangle(
            (Gdx.graphics.getWidth() - 250) / 2f,
            20,
            250,
            50
        );
    }

    @Override
    public void show() {
        // Correctly reset the game state and timer every time the screen is shown
        reset();
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
                    stateTimer = 0;
                    // Store the final time before transitioning
                    this.finalElapsedTime = Math.max(0, (System.currentTimeMillis() - startTime) / 1000.0f - STAGE_COMPLETE_DURATION);
                    // Transition to the LOSE_SCREEN after the GAME_OVER animation
                    currentState = GameState.LOSE_SCREEN;
                    // Reset animation time for the new scene
                    stateTime = 0;
                }
                break;
            case WIN_SCREEN:
                renderWinScreen();
                break;
            case LOSE_SCREEN:
                renderLoseScreen();
                break;
        }
    }

    /**
     * Renders the game in the playing state.
     */
    private void renderPlayingState() {
        // Correctly call the board's two-pass render method.
        // We handle the begin/end calls at the GameScreen level.
        board.render(batch, shapeRenderer, font, 1.0f);
        // Pass alpha of 1.0 since it's a non-fading state
        keyboard.render(batch, shapeRenderer, keyboardFont, 1024);
        drawStageNumber();
        // Draw the Quit button
        drawQuitButton();
    }

    /**
     * Renders a custom scene for a completed stage with a fade-in effect.
     */
    private void renderStageCompleteScene() {
        // Clear the screen to a solid color
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        // Calculate the current alpha for the fade-in effect
        float alpha = Math.min(1.0f, stateTimer / 1.0f);
        // Fades in over 1 second

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
        Gdx.gl.glEnable(GL20.GL_BLEND);
        // Enable blending for transparency
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
        Gdx.gl.glDisable(GL20.GL_BLEND);
        // Disable blending after we're done with transparent shapes

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
        // Adjust bunnyX to be properly centered
        float bunnyX = (screenWidth - scaledBunnyWidth) / 2f;
        float bunnyY = centerY + 20;
        batch.setColor(1.0f, 1.0f, 1.0f, alpha);
        batch.draw(currentBunnyFrame, bunnyX, bunnyY, scaledBunnyWidth, scaledBunnyHeight);
        batch.setColor(Color.WHITE);
        // Reset color to default

        // --- DRAW THE CARROT IDLE ANIMATION ---
        TextureRegion currentCarrotFrame = carrotIdleAnimation.getKeyFrame(stateTime, true);
        float scaledCarrotWidth = currentCarrotFrame.getRegionWidth() * (scaleFactor - 2);
        float scaledCarrotHeight = currentCarrotFrame.getRegionHeight() * (scaleFactor - 2);
        // Position the carrot to the left of the bunny
        float carrotX = bunnyX - scaledCarrotWidth + 200;
        float carrotY = bunnyY + 20;
        batch.setColor(1.0f, 1.0f, 1.0f, alpha);
        batch.draw(currentCarrotFrame, carrotX, carrotY, scaledCarrotWidth, scaledCarrotHeight);
        batch.setColor(Color.WHITE);
        // Reset color to default

        batch.end();
    }

    /**
     * Renders a custom scene for a game over state with a fade-in effect.
     */
    private void renderGameOverScene() {
        // Clear the screen to a solid color
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        // Calculate the current alpha for the fade-in effect
        float alpha = Math.min(1.0f, stateTimer / 1.0f);
        // Fades in over 1 second

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
        Gdx.gl.glEnable(GL20.GL_BLEND);
        // Enable blending for transparency
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
        Gdx.gl.glDisable(GL20.GL_BLEND);
        // Disable blending after we're done with transparent shapes

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
        TextureRegion currentBunnyFrame = deathAnimation.getKeyFrame(stateTime, false);
        // No looping for death animation
        float scaleFactor = 5.0f;
        float scaledBunnyWidth = currentBunnyFrame.getRegionWidth() * scaleFactor;
        float scaledBunnyHeight = currentBunnyFrame.getRegionHeight() * scaleFactor;
        float screenWidth = Gdx.graphics.getWidth();
        // Adjust bunnyX to be properly centered
        float bunnyX = (screenWidth - scaledBunnyWidth) / 2f;
        float bunnyY = centerY + 20;
        batch.setColor(1.0f, 1.0f, 1.0f, alpha);
        batch.draw(currentBunnyFrame, bunnyX, bunnyY, scaledBunnyWidth, scaledBunnyHeight);
        batch.setColor(Color.WHITE);
        // Reset color to default

        // --- DRAW THE CARROT DEATH ANIMATION ---
        TextureRegion currentCarrotFrame = carrotDeathAnimation.getKeyFrame(stateTime, false);
        // No looping for death animation
        float scaledCarrotWidth = currentCarrotFrame.getRegionWidth() * (scaleFactor - 2);
        float scaledCarrotHeight = currentCarrotFrame.getRegionHeight() * (scaleFactor - 2);
        // Position the carrot to the left of the bunny
        float carrotX = bunnyX - scaledCarrotWidth + 200;
        float carrotY = bunnyY + 20;
        batch.setColor(1.0f, 1.0f, 1.0f, alpha);
        batch.draw(currentCarrotFrame, carrotX, carrotY, scaledCarrotWidth, scaledCarrotHeight);
        batch.setColor(Color.WHITE);
        // Reset color to default

        batch.end();
    }

    private void renderWinScreen() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        // Calculate the current alpha for the fade-in effect
        float alpha = Math.min(1.0f, stateTimer / 1.0f);
        // Fades in over 1 second

        // Define layout variables
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float leftPadding = screenWidth * 0.1f;
        float rightPadding = screenWidth * 0.1f;
        float topPadding = screenHeight * 0.2f;
        float wordSpacing = 20f;
        float tileSize = 64f;
        float tileGap = 10f;
        float totalTileWidth = (5 * tileSize) + (4 * tileGap);
        // --- FIRST PASS: Draw Shapes (FOR TILES AND BACKGROUND) ---
        Gdx.gl.glEnable(GL20.GL_BLEND);
        // Enable blending for transparency
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.15f, alpha);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);

        // Render shapes for all solved words in a vertical list on the right side
        float currentWordY = screenHeight - topPadding - 70;
        for (int i = 0; i < solvedWords.size(); i++) {
            String word = solvedWords.get(i);
            TileState[] states = solvedWordStates.get(i);

            float wordTileStartX = screenWidth - rightPadding - totalTileWidth;
            // Render shapes for each tile in the word
            for (int j = 0; j < word.length(); j++) {
                Tile tile = new Tile();
                tile.setLetter(word.charAt(j));
                tile.setState(states[j]);
                tile.renderShape(shapeRenderer, wordTileStartX + (j * (tileSize + tileGap)), currentWordY, tileSize, 1.0f, alpha);
            }
            currentWordY -= (tileSize + wordSpacing);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        // Disable blending after we're done with transparent shapes

        // --- SECOND PASS: Draw Sprites and Text ---
        batch.begin();
        font.setColor(1.0f, 1.0f, 1.0f, alpha);

        // Draw the "You Win!" text on the left side
        String winMessage = "You Win!";
        layout.setText(font, winMessage);
        font.draw(batch, layout, leftPadding + 80, screenHeight - topPadding + 25);
        // Draw the "Words Solved:" text on the right side, right-aligned
        String solvedTitle = "Words Solved:";
        layout.setText(font, solvedTitle);
        font.draw(batch, layout, screenWidth - rightPadding - layout.width - 40, screenHeight - topPadding + 75);
        // --- DRAW THE BUNNY DASH ANIMATION ---
        TextureRegion currentBunnyFrame = dashAnimation.getKeyFrame(stateTime, true);
        float scaleFactor = 5.0f;
        float scaledBunnyWidth = currentBunnyFrame.getRegionWidth() * scaleFactor;
        float scaledBunnyHeight = currentBunnyFrame.getRegionHeight() * scaleFactor;
        // --- DRAW THE CARROT SPIN ANIMATION ---
        TextureRegion currentCarrotFrame = carrotSpinAnimation.getKeyFrame(stateTime, true);
        float carrotScaleFactor = 0.75f;
        float scaledCarrotWidth = currentCarrotFrame.getRegionWidth() * carrotScaleFactor;
        float scaledCarrotHeight = currentCarrotFrame.getRegionHeight() * carrotScaleFactor;
        // Calculate combined width and position to center both sprites
        float totalSpritesWidth = scaledBunnyWidth + scaledCarrotWidth + 30f;
        // 50f for gap
        float bunnyX = (screenWidth - totalSpritesWidth) / 2f - 100;
        float bunnyY = screenHeight - topPadding - layout.height - scaledBunnyHeight + 100;
        float carrotX = bunnyX + scaledBunnyWidth - 350f;
        float carrotY = bunnyY + 20;

        batch.setColor(1.0f, 1.0f, 1.0f, alpha);
        batch.draw(currentBunnyFrame, bunnyX, bunnyY, scaledBunnyWidth, scaledBunnyHeight);
        batch.draw(currentCarrotFrame, carrotX, carrotY, scaledCarrotWidth, scaledCarrotHeight);
        batch.setColor(Color.WHITE);

        // --- DRAW STATS BELOW ANIMATIONS ---
        float statsY = bunnyY - 50;
        font.getData().setScale(0.75f);
        // Display session-specific stats
        font.draw(batch, "Total Words Solved: " + sessionWordsSolved, leftPadding, statsY);
        statsY -= 40;
        font.draw(batch, "Total Guesses: " + sessionGuesses, leftPadding, statsY);
        statsY -= 40;
        font.draw(batch, String.format("Avg. Guess/Word Solved: %.2f", (float) sessionGuesses / sessionWordsSolved), leftPadding, statsY);
        statsY -= 40;
        if (sessionBestWord.equals("")) {
            font.draw(batch, "Best Word: N/A", leftPadding, statsY);
        } else {
            font.draw(batch, "Best Word: " + sessionBestWord + " (in " + sessionBestWordGuesses + ")", leftPadding, statsY);
        }
        statsY -= 40;
        // New line for time elapsed
        String timeElapsed = String.format("Time Elapsed: %.2f s", finalElapsedTime);
        font.draw(batch, timeElapsed, leftPadding, statsY);
        font.getData().setScale(1.0f);
        currentWordY = screenHeight - topPadding - 70;
        // Render text for all solved words in a vertical list on the right side
        for (int i = 0; i < solvedWords.size(); i++) {
            String word = solvedWords.get(i);
            TileState[] states = solvedWordStates.get(i);

            float wordTileStartX = screenWidth - rightPadding - totalTileWidth;
            for (int j = 0; j < word.length(); j++) {
                Tile tile = new Tile();
                tile.setLetter(word.charAt(j));
                tile.setState(states[j]);
                tile.renderText(batch, font, wordTileStartX + (j * (tileSize + tileGap)), currentWordY, tileSize, alpha);
            }
            currentWordY -= (tileSize + wordSpacing);
        }

        batch.end();

        // Draw the Back to Menu button
        drawMenuButton("Back to Menu");
    }

    private void renderLoseScreen() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        // Calculate the current alpha for the fade-in effect
        float alpha = Math.min(1.0f, stateTimer / 1.0f);
        // Define layout variables
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float leftPadding = screenWidth * 0.1f;
        float rightPadding = screenWidth * 0.1f;
        float topPadding = screenHeight * 0.2f;
        float wordSpacing = 20f;
        float tileSize = 64f;
        float tileGap = 10f;
        float totalTileWidth = (5 * tileSize) + (4 * tileGap);
        // --- FIRST PASS: Draw Shapes ---
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.15f, alpha);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);

        // If there are solved words, render their shapes on the right
        if (!solvedWords.isEmpty()) {
            float currentWordY = screenHeight - topPadding - 70;
            for (int i = 0; i < solvedWords.size(); i++) {
                String word = solvedWords.get(i);
                TileState[] states = solvedWordStates.get(i);

                float wordTileStartX = screenWidth - rightPadding - totalTileWidth;
                for (int j = 0; j < word.length(); j++) {
                    Tile tile = new Tile();
                    tile.setLetter(word.charAt(j));
                    tile.setState(states[j]);
                    tile.renderShape(shapeRenderer, wordTileStartX + (j * (tileSize + tileGap)), currentWordY, tileSize, 1.0f, alpha);
                }
                currentWordY -= (tileSize + wordSpacing);
            }
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        // --- SECOND PASS: Draw Sprites and Text ---
        batch.begin();
        font.setColor(1.0f, 1.0f, 1.0f, alpha);

        String loseMessage = "You Lose!";
        layout.setText(font, loseMessage);
        font.draw(batch, layout, leftPadding + 80, screenHeight - topPadding + 25);
        // Draw the "Words Solved:" text unconditionally
        String solvedTitle = "Words Solved:";
        layout.setText(font, solvedTitle);
        font.draw(batch, layout, screenWidth - rightPadding - layout.width - 40, screenHeight - topPadding + 75);
        // Draw the dead bunny and carrot animations unconditionally on the left
        float scaleFactor = 5.0f;
        float carrotScaleFactor = 0.75f;
        TextureRegion currentBunnyFrame = deathAnimation.getKeyFrame(stateTime, false);
        TextureRegion currentCarrotFrame = carrotDeathAnimation.getKeyFrame(stateTime, false);
        float scaledBunnyWidth = currentBunnyFrame.getRegionWidth() * scaleFactor;
        float scaledBunnyHeight = currentBunnyFrame.getRegionHeight() * scaleFactor;
        float scaledCarrotWidth = currentCarrotFrame.getRegionWidth() * carrotScaleFactor;
        float scaledCarrotHeight = currentCarrotFrame.getRegionHeight() * carrotScaleFactor;
        float totalSpritesWidth = scaledBunnyWidth + scaledCarrotWidth + 30f;
        float bunnyX = (screenWidth - totalSpritesWidth) / 2f - 200;
        float bunnyY = screenHeight - topPadding - layout.height - scaledBunnyHeight + 50;
        float carrotX = bunnyX + scaledBunnyWidth - 250f;
        float carrotY = bunnyY + 50;

        batch.setColor(1.0f, 1.0f, 1.0f, alpha);
        batch.draw(currentBunnyFrame, bunnyX, bunnyY, scaledBunnyWidth, scaledBunnyHeight);
        batch.draw(currentCarrotFrame, carrotX, carrotY, scaledCarrotWidth, scaledCarrotHeight);
        // Draw either the solved words or the goblin image on the right
        if (solvedWords.isEmpty()) {
            // No words solved, show goblin crying
            float imageWidth = goblinCryingTexture.getWidth();
            float imageHeight = goblinCryingTexture.getHeight();
            float imageX = (screenWidth - imageWidth) / 2f + 250;
            float imageY = screenHeight - topPadding - layout.height - imageHeight - 100;
            batch.draw(goblinCryingTexture, imageX, imageY, imageWidth, imageHeight);
        } else {
            // Solved at least one word, render the text for all solved words
            float currentWordY = screenHeight - topPadding - 70;
            for (int i = 0; i < solvedWords.size(); i++) {
                String word = solvedWords.get(i);
                TileState[] states = solvedWordStates.get(i);

                float wordTileStartX = screenWidth - rightPadding - totalTileWidth;
                for (int j = 0; j < word.length(); j++) {
                    Tile tile = new Tile();
                    tile.setLetter(word.charAt(j));
                    tile.setState(states[j]);
                    tile.renderText(batch, font, wordTileStartX + (j * (tileSize + tileGap)), currentWordY, tileSize, alpha);
                }
                currentWordY -= (tileSize + wordSpacing);
            }
        }
        batch.setColor(Color.WHITE);
        // --- DRAW STATS BELOW ANIMATIONS ---
        float statsY = bunnyY;
        font.getData().setScale(0.75f);
        font.draw(batch, "Total Words Solved: " + sessionWordsSolved, leftPadding, statsY);
        statsY -= 40;
        font.draw(batch, "Total Guesses: " + sessionGuesses, leftPadding, statsY);
        statsY -= 40;
        font.draw(batch, String.format("Avg. Guess/Word Solved: %.2f", (float) sessionGuesses / Math.max(1, sessionWordsSolved)), leftPadding, statsY);
        statsY -= 40;
        if (sessionBestWord.equals("")) {
            font.draw(batch, "Best Word: N/A", leftPadding, statsY);
        } else {
            font.draw(batch, "Best Word: " + sessionBestWord + " (in " + sessionBestWordGuesses + ")", leftPadding, statsY);
        }
        statsY -= 40;
        // New line for time elapsed
        String timeElapsed = String.format("Time Elapsed: %.2f s", finalElapsedTime);
        font.draw(batch, timeElapsed, leftPadding, statsY);
        font.getData().setScale(1.0f);

        batch.end();
        // Draw the Back to Menu button
        drawMenuButton("Back to Menu");
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

    /**
     * Draws the quit button on the playing screen.
     */
    private void drawQuitButton() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.5f, 0.1f, 0.1f, 1f);
        shapeRenderer.rect(quitButtonBounds.x, quitButtonBounds.y, quitButtonBounds.width, quitButtonBounds.height);
        shapeRenderer.end();

        batch.begin();
        keyboardFont.setColor(Color.WHITE);
        String quitText = "QUIT";
        layout.setText(keyboardFont, quitText);
        float textX = quitButtonBounds.x + (quitButtonBounds.width - layout.width) / 2;
        float textY = quitButtonBounds.y + (quitButtonBounds.height + layout.height) / 2;
        keyboardFont.draw(batch, quitText, textX, textY);
        batch.end();
    }

    /**
     * Draws the general purpose menu button.
     *
     * @param buttonText The text to display on the button.
     */
    private void drawMenuButton(String buttonText) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.4f, 0.6f, 1f);
        shapeRenderer.rect(menuButtonBounds.x, menuButtonBounds.y, menuButtonBounds.width, menuButtonBounds.height);
        shapeRenderer.end();

        batch.begin();
        keyboardFont.setColor(Color.WHITE);
        layout.setText(keyboardFont, buttonText);
        float textX = menuButtonBounds.x + (menuButtonBounds.width - layout.width) / 2;
        float textY = menuButtonBounds.y + (menuButtonBounds.height + layout.height) / 2;
        keyboardFont.draw(batch, buttonText, textX, textY);
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
        sessionBestWord = "";
        sessionBestWordGuesses = Integer.MAX_VALUE;
        // Reset session-specific stats
        sessionWordsSolved = 0;
        sessionGuesses = 0;
        // Start the timer
        startTime = System.currentTimeMillis();
        finalElapsedTime = 0.0f;
    }

    private void loadNextStage() {
        if (!gameManager.advanceStage()) {
            gameManager.setFinalWin(true);
            // Store the final time before transitioning, subtracting the animation duration
            this.finalElapsedTime = Math.max(0, (System.currentTimeMillis() - startTime) / 1000.0f - STAGE_COMPLETE_DURATION - WIN_ANIMATION_DURATION);

            // Now, check and update best times for both modes here, as finalElapsedTime is correct.
            if (gameMode == GameMode.BUNDLE && (playerProfile.getGameStats().getBestTimeBundle() == 0 || this.finalElapsedTime < playerProfile.getGameStats().getBestTimeBundle())) {
                playerProfile.getGameStats().setBestTimeBundle(this.finalElapsedTime);
            }
            if (gameMode == GameMode.CLASSIC && (playerProfile.getGameStats().getBestTimeClassic() == 0 || this.finalElapsedTime < playerProfile.getGameStats().getBestTimeClassic())) {
                playerProfile.getGameStats().setBestTimeClassic(this.finalElapsedTime);
            }

            // Transition to the new WIN_SCREEN instead of GAME_OVER
            currentState = GameState.WIN_SCREEN;
            stateTimer = 0;
            stateTime = 0; // Reset animation time for the new scene
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
        rabbitDashTexture.dispose();
        goblinCryingTexture.dispose();
        // Dispose of each carrot texture in the list
        for (Texture texture : carrotIdleTextures) {
            texture.dispose();
        }
        for (Texture texture : carrotDeathTextures) {
            texture.dispose();
        }
        for (Texture texture : carrotSpinTextures) {
            texture.dispose();
        }
    }

    // Method to set the game mode
    public void setGameMode(GameMode mode) {
        this.gameMode = mode;
    }

    private class GameInputProcessor implements InputProcessor {
        @Override
        public boolean keyDown(int keycode) {
            // DEBUG: Press 'W' to instantly go to the win screen
            if (keycode == Input.Keys.W) {
                gameManager.setFinalWin(true);
                // Store the final time before transitioning
                GameScreen.this.finalElapsedTime = Math.max(0, (System.currentTimeMillis() - startTime) / 1000.0f - STAGE_COMPLETE_DURATION);
                currentState = GameState.WIN_SCREEN;
                stateTimer = 0;
                stateTime = 0;
                // Iterate through all stage words and add them to the solvedWords list
                for (String word : gameManager.getStageWords()) {
                    solvedWords.add(word.toUpperCase());
                    // CRITICAL FIX: Generate the 'all green' state for each word
                    TileState[] states = WordChecker.checkWord(word.toUpperCase(), word.toUpperCase());
                    solvedWordStates.add(states);
                }
                return true;
            }

            if (currentState == GameState.PLAYING) {
                if (keycode >= Input.Keys.A && keycode <= Input.Keys.Z) {
                    char letter = (char) ('A' + (keycode - Input.Keys.A));
                    board.typeLetter(letter);
                } else if (keycode == Input.Keys.ENTER) {
                    String submittedWord = board.getSubmittedWord();
                    int currentRow = board.getCurrentRow();
                    TileState[] result = board.submitGuess(currentRow);

                    // Increment the total guesses for any submitted word
                    playerProfile.getGameStats().incrementTotalGuesses();
                    sessionGuesses++;

                    if (result != null) {
                        for (int c = 0; c < 5; c++) {
                            char letter = submittedWord.charAt(c);
                            keyboard.updateKeyState(letter, result[c]);
                        }

                        if (gameManager.isStageSolved()) {
                            currentRow = board.getCurrentRow();
                            int guesses = 1;
                            if (currentRow != gameManager.getCurrentStage()) {
                                guesses = board.getCurrentRow() - (gameManager.getCurrentStage() - 1);
                            }
                            System.out.println("Guesses: " + guesses);
                            solvedWords.add(submittedWord);
                            solvedWordStates.add(result);
                            currentState = GameState.WIN_ANIMATION;
                            stateTimer = 0;
                            playerProfile.getGameStats().onWordSolved(submittedWord, guesses);
                            sessionWordsSolved++;
                            updateSessionBestWord(submittedWord, guesses);
                            // NOTE: The redundant time-saving logic was removed from here.
                        } else if (gameManager.isGameOver()) {
                            currentRow = board.getCurrentRow();
                            int guesses = 1;
                            if (currentRow != gameManager.getCurrentStage()) {
                                guesses = board.getCurrentRow() - (gameManager.getCurrentStage() - 1);
                            }
                            // The onWordSolved method now correctly handles updating all stats.
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

        @Override
        public boolean keyTyped(char character) { return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            // Adjust the screenY coordinate for libGDX's inverted Y-axis
            float correctedY = Gdx.graphics.getHeight() - screenY;
            // Handle the QUIT button press
            if (currentState == GameState.PLAYING && quitButtonBounds.contains(screenX, correctedY)) {
                // Store the final time before transitioning
                GameScreen.this.finalElapsedTime = Math.max(0, (System.currentTimeMillis() - startTime) / 1000.0f - STAGE_COMPLETE_DURATION);
                currentState = GameState.GAME_OVER;
                stateTimer = 0;
                stateTime = 0; // Reset animation time for the death animation
                return true;
            }

            // Handle the Back to Menu button press for WIN_SCREEN and LOSE_SCREEN
            if ((currentState == GameState.WIN_SCREEN || currentState == GameState.LOSE_SCREEN) && menuButtonBounds.contains(screenX, correctedY)) {
                game.setScreen(game.menuScreen);
                return true;
            }
            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false;
        }
        public boolean touchDragged(int screenX, int screenY, int pointer) { return false;
        }
        public boolean mouseMoved(int screenX, int screenY) { return false;
        }
        public boolean scrolled(float amountX, float amountY) { return false;
        }
        public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false;
        }
    }

    private void updateSessionBestWord(String word, int guesses) {
        if (guesses < sessionBestWordGuesses) {
            sessionBestWord = word;
            sessionBestWordGuesses = guesses;
        } else if (guesses == sessionBestWordGuesses) {
            // Keep the first word with the best guess count
        }
    }
}
