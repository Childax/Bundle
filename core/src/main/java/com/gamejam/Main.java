package com.gamejam;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
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

    // Game mode flag
    public boolean isClassicMode = false;

    // You can add other screens here as they are created.
    public MenuScreen menuScreen; // Made public for easy access
    private GameScreen gameScreen;
    private StatsScreen statsScreen;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = FontLoader.loadFont("fonts/HelveticaNeue-BlackCond.otf", 48);
        keyboardFont = FontLoader.loadFont("fonts/HelveticaNeue-BlackCond.otf", 32);

        // Initialize the core game objects
        // We'll initialize them here, but they will be re-created in startGame()
        // to ensure a clean state.
        gameManager = new GameManager(6);
        board = new Board(gameManager);
        keyboard = new Keyboard();

        // Instantiate the player profile here. This ensures it only happens once.
        // For now, we'll just give it a default name.
        playerProfile = new PlayerProfile("Childax");

        // Create the initial screens and set the first one
        statsScreen = new StatsScreen(this, playerProfile);
        menuScreen = new MenuScreen(this, statsScreen);
        setScreen(menuScreen);
    }

    /**
     * Resets the game state and transitions to the game screen.
     * The game mode (classic or bundle) is determined by the `isClassicMode` flag.
     *
     * FIX: The bug was that new game objects (like GameManager) were created, but the
     * old instances were still being used by the GameScreen. This fix ensures that a
     * completely new game state is established every time the game starts.
     */
    public void startGame() {
        // Determine the number of stages based on the game mode
        int numStages = isClassicMode ? 1 : 6;

        // Create a new GameManager instance for the selected mode.
        gameManager = new GameManager(numStages);

        // Reset or re-create the board and keyboard for a clean slate.
        // This is crucial to prevent state from previous games from lingering.
        board = new Board(gameManager);
        keyboard = new Keyboard();

        // Create a new GameScreen instance with the updated game objects.
        // This is safer than trying to reset the existing screen.
        gameScreen = new GameScreen(this, gameManager, board, keyboard, playerProfile);

        // Transition to the new game screen.
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
