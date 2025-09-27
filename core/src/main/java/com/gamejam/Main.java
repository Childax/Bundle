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
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private BitmapFont keyboardFont;
    private Preferences preferences;

    private GameManager gameManager;
    private Board board;
    private Keyboard keyboard;

    private PlayerProfile playerProfile;

    public boolean isClassicMode = false;

    public MenuScreen menuScreen;
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

        preferences = Gdx.app.getPreferences("BundlePreferences");
        String savedUsername = preferences.getString("username", null);

        if (savedUsername != null && !savedUsername.isEmpty()) {
            playerProfile = new PlayerProfile(savedUsername);
            playerProfile.loadGameStats(preferences);
            howToPlayScreen = new HowToPlayScreen(this);
            creditsScreen = new CreditsScreen(this);
            statsScreen = new StatsScreen(this, playerProfile);
            menuScreen = new MenuScreen(this, statsScreen, howToPlayScreen, creditsScreen);
            setScreen(menuScreen);
        } else {
            usernameScreen = new UsernameScreen(this);
            setScreen(usernameScreen);
        }
    }

    /**
     * Called by the UsernameScreen to save the username and transition to the main menu.
     * @param username The username to save.
     */
    public void setPlayerUsername(String username) {
        preferences.putString("username", username);
        preferences.flush();

        playerProfile = new PlayerProfile(username);
        Gdx.app.log("Main", "Username saved: " + username);

        howToPlayScreen = new HowToPlayScreen(this);
        creditsScreen = new CreditsScreen(this);
        statsScreen = new StatsScreen(this, playerProfile);
        menuScreen = new MenuScreen(this, statsScreen, howToPlayScreen, creditsScreen);
        setScreen(menuScreen);

        usernameScreen.dispose();
    }


    /**
     * Resets the game state and transitions to the game screen.
     */
    public void startGame() {
        // Determine the number of stages based on the game mode
        int numStages = isClassicMode ? 1 : 6;
        gameMode = isClassicMode? GameScreen.GameMode.CLASSIC : GameScreen.GameMode.BUNDLE;
        gameManager = new GameManager(numStages);
        board = new Board(gameManager);
        keyboard = new Keyboard();
        gameScreen = new GameScreen(this, gameManager, board, keyboard, playerProfile);
        gameScreen.setGameMode(gameMode);
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
