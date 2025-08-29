package com.gamejam;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Main game class that manages different screens (e.g., Menu, Gameplay).
 * This class should be a simple controller, delegating all logic to the active screen.
 */
public class Main extends Game {
    // These resources are shared across all screens, so we keep them here.
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private BitmapFont keyboardFont;

    // Game state objects that will be passed to the screens.
    private GameManager gameManager;
    private Board board;
    private Keyboard keyboard;

    // The player profile object that persists across all screens.
    private PlayerProfile playerProfile;

    // You can add other screens here as they are created.
    public MenuScreen menuScreen; // Made public for easy access
    private GameScreen gameScreen;
    private StatsScreen statsScreen;
    // Add other screens here as they are created.

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = FontLoader.loadFont("fonts/HelveticaNeue-BlackCond.otf", 48);
        keyboardFont = FontLoader.loadFont("fonts/HelveticaNeue-BlackCond.otf", 32);

        // Initialize the core game objects
        gameManager = new GameManager(6);
        board = new Board(gameManager);
        keyboard = new Keyboard();

        // Instantiate the player profile here. This ensures it only happens once.
        // For now, we'll just give it a default name.
        playerProfile = new PlayerProfile("Childax");

        // Create the initial screen and set it
        statsScreen = new StatsScreen(this, playerProfile);
        menuScreen = new MenuScreen(this, statsScreen);
        setScreen(menuScreen);
    }

    /**
     * Resets the game state and transitions to the game screen.
     */
    public void startGame() {
        // Reset the game manager for a new set of words
        gameManager.pickNewSolutions();

        // If the GameScreen hasn't been created yet, create it.
        // This avoids creating it until it's needed.
        if (gameScreen == null) {
            // Pass the playerProfile to the GameScreen
            gameScreen = new GameScreen(this, gameManager, board, keyboard, playerProfile);
        } else {
            // If it already exists, reset its state for a new game
            gameScreen.reset();
        }

        // The game objects are already managed by the GameScreen, no need to reset them here.
        setScreen(gameScreen);
    }

    /**
     * Disposes of all shared resources.
     */
    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        keyboardFont.dispose();
        if (getScreen() != null) {
            getScreen().dispose();
        }
    }

    // Getters for shared resources to be used by screens
    public SpriteBatch getBatch() { return batch; }
    public ShapeRenderer getShapeRenderer() { return shapeRenderer; }
    public BitmapFont getFont() { return font; }
    public BitmapFont getKeyboardFont() { return keyboardFont; }
}
