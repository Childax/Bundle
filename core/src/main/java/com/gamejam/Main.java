package com.gamejam;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
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
    private Preferences preferences;

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
    private HowToPlayScreen howToPlayScreen;
    private CreditsScreen creditsScreen;
    private UsernameScreen usernameScreen;

    private GameScreen.GameMode gameMode;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = FontLoader.loadFont("fonts/HelveticaNeue-BlackCond.otf", 48);
        keyboardFont = FontLoader.loadFont("fonts/HelveticaNeue-BlackCond.otf", 32);

        // Initialize preferences for persistent data storage
        preferences = Gdx.app.getPreferences("BundlePreferences");

        // Check if a username is already saved
        String savedUsername = preferences.getString("username", null);

        if (savedUsername != null && !savedUsername.isEmpty()) {
            // A username exists, so initialize the player profile and go to the menu
            playerProfile = new PlayerProfile(savedUsername);
            playerProfile.loadGameStats(preferences);
            howToPlayScreen = new HowToPlayScreen(this);
            creditsScreen = new CreditsScreen(this);
            statsScreen = new StatsScreen(this, playerProfile);
            menuScreen = new MenuScreen(this, statsScreen, howToPlayScreen, creditsScreen);
            setScreen(menuScreen);
        } else {
            // No username exists, show the username screen first
            usernameScreen = new UsernameScreen(this);
            setScreen(usernameScreen);
        }
    }

    /**
     * Called by the UsernameScreen to save the username and transition to the main menu.
     * @param username The username to save.
     */
    public void setPlayerUsername(String username) {
        // Save the username to preferences
        preferences.putString("username", username);
        preferences.flush();

        // Initialize the player profile with the new username
        playerProfile = new PlayerProfile(username);
        Gdx.app.log("Main", "Username saved: " + username);

        // Initialize all the game screens and set the menu screen
        howToPlayScreen = new HowToPlayScreen(this);
        creditsScreen = new CreditsScreen(this);
        statsScreen = new StatsScreen(this, playerProfile);
        menuScreen = new MenuScreen(this, statsScreen, howToPlayScreen, creditsScreen);
        setScreen(menuScreen);

        // Dispose of the username screen as it's no longer needed
        usernameScreen.dispose();
    }


    /**
     * Resets the game state and transitions to the game screen.
     * The game mode (classic or bundle) is determined by the `isClassicMode` flag.
     * FIX: The bug was that new game objects (like GameManager) were created, but the
     * old instances were still being used by the GameScreen. This fix ensures that a
     * completely new game state is established every time the game starts.
     */
    public void startGame() {
        // Determine the number of stages based on the game mode
        int numStages = isClassicMode ? 1 : 6;
        gameMode = isClassicMode? GameScreen.GameMode.CLASSIC : GameScreen.GameMode.BUNDLE;
        // Create a new GameManager instance for the selected mode.
        gameManager = new GameManager(numStages);
        // Reset or re-create the board and keyboard for a clean slate.
        // This is crucial to prevent state from previous games from lingering.
        board = new Board(gameManager);
        keyboard = new Keyboard();

        // Create a new GameScreen instance with the updated game objects.
        // This is safer than trying to reset the existing screen.
        gameScreen = new GameScreen(this, gameManager, board, keyboard, playerProfile);
        gameScreen.setGameMode(gameMode);
        // Transition to the new game screen.
        setScreen(gameScreen);
    }

    /**
     * Disposes of all shared resources.
     */
    @Override
    public void dispose() {
        playerProfile.saveGameStats(preferences);
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

    // Add this method to your Main.java class
    public void resetAndGoToUsernameScreen() {
        playerProfile.resetProfile(Gdx.app.getPreferences("BundlePreferences"));
        usernameScreen = new UsernameScreen(this);
        setScreen(usernameScreen);
    }
}
