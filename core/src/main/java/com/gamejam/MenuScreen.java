package com.gamejam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * The main menu screen for the game.
 * This screen handles the buttons for selecting the game mode and viewing stats.
 *
 * MODIFICATION: Added a button to navigate to the new HowToPlayScreen.
 */
public class MenuScreen implements Screen, InputProcessor {

    private final Main game;
    private final StatsScreen statsScreen;
    private final HowToPlayScreen howToPlayScreen;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    // Button properties
    private Rectangle bundleButton; // Button for the 6-stage BUNDLE mode
    private Rectangle classicButton; // Button for the classic 1-stage CLASSIC mode
    private Rectangle statsButton; // Button for viewing stats
    private Rectangle howToPlayButton; // Button for the How To Play screen
    private final float BUTTON_WIDTH = 300;
    private final float BUTTON_HEIGHT = 100;
    private final float BUTTON_CORNER_RADIUS = 20;
    private final float BUTTON_SPACING = 30; // Spacing between buttons

    public MenuScreen(Main game, StatsScreen statsScreen, HowToPlayScreen howToPlayScreen) {
        this.game = game;
        this.statsScreen = statsScreen;
        this.howToPlayScreen = howToPlayScreen;
        this.batch = game.getBatch();
        this.shapeRenderer = game.getShapeRenderer();
        this.font = game.getFont();

        // Position the buttons
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        // Draw the title
        batch.begin();
        font.setColor(Color.WHITE);
        layout.setText(font, "MAIN MENU");
        float titleX = (Gdx.graphics.getWidth() - layout.width) / 2;
        float titleY = Gdx.graphics.getHeight() - 100;
        font.draw(batch, "MAIN MENU", titleX, titleY);
        batch.end();

        // Draw the buttons
        drawRoundedButton(bundleButton, "BUNDLE");
        drawRoundedButton(classicButton, "CLASSIC");
        drawRoundedButton(howToPlayButton, "HOW TO PLAY");
        drawRoundedButton(statsButton, "VIEW STATS");
    }

    /**
     * Helper method to draw a rounded rectangle button with text.
     * @param button The Rectangle defining the button bounds.
     * @param text The text to display on the button.
     */
    private void drawRoundedButton(Rectangle button, String text) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.valueOf("#4C8BF5"));

        shapeRenderer.rect(button.x + BUTTON_CORNER_RADIUS, button.y,
            button.width - 2 * BUTTON_CORNER_RADIUS, button.height);

        shapeRenderer.rect(button.x, button.y + BUTTON_CORNER_RADIUS,
            BUTTON_CORNER_RADIUS, button.height - 2 * BUTTON_CORNER_RADIUS);
        shapeRenderer.rect(button.x + button.width - BUTTON_CORNER_RADIUS,
            button.y + BUTTON_CORNER_RADIUS,
            BUTTON_CORNER_RADIUS, button.height - 2 * BUTTON_CORNER_RADIUS);

        shapeRenderer.circle(button.x + BUTTON_CORNER_RADIUS, button.y + BUTTON_CORNER_RADIUS, BUTTON_CORNER_RADIUS);
        shapeRenderer.circle(button.x + button.width - BUTTON_CORNER_RADIUS, button.y + BUTTON_CORNER_RADIUS, BUTTON_CORNER_RADIUS);
        shapeRenderer.circle(button.x + BUTTON_CORNER_RADIUS, button.y + button.height - BUTTON_CORNER_RADIUS, BUTTON_CORNER_RADIUS);
        shapeRenderer.circle(button.x + button.width - BUTTON_CORNER_RADIUS, button.y + button.height - BUTTON_CORNER_RADIUS, BUTTON_CORNER_RADIUS);

        shapeRenderer.end();

        batch.begin();
        font.setColor(Color.WHITE);
        layout.setText(font, text);
        float buttonTextX = button.x + (button.width - layout.width) / 2;
        float buttonTextY = button.y + (button.height + layout.height) / 2;
        font.draw(batch, text, buttonTextX, buttonTextY);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        float startX = (width - BUTTON_WIDTH) / 2;
        float centerOffset = BUTTON_HEIGHT + BUTTON_SPACING;
        float halfSpacing = BUTTON_SPACING / 2;

        bundleButton = new Rectangle(startX, Gdx.graphics.getHeight() / 2 + centerOffset + halfSpacing, BUTTON_WIDTH, BUTTON_HEIGHT);
        classicButton = new Rectangle(startX, Gdx.graphics.getHeight() / 2, BUTTON_WIDTH, BUTTON_HEIGHT);
        howToPlayButton = new Rectangle(startX, Gdx.graphics.getHeight() / 2 - centerOffset - halfSpacing, BUTTON_WIDTH, BUTTON_HEIGHT);
        statsButton = new Rectangle(startX, Gdx.graphics.getHeight() / 2 - (centerOffset * 2) - BUTTON_SPACING, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() { }

    @Override
    public void dispose() { }

    @Override
    public boolean keyDown(int keycode) { return false; }
    @Override
    public boolean keyUp(int keycode) { return false; }
    @Override
    public boolean keyTyped(char character) { return false; }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Correct the y-coordinate for the LibGDX origin (bottom-left)
        int correctedY = Gdx.graphics.getHeight() - screenY;

        if (bundleButton.contains(screenX, correctedY)) {
            game.isClassicMode = false;
            game.startGame();
            return true;
        }

        if (classicButton.contains(screenX, correctedY)) {
            game.isClassicMode = true;
            game.startGame();
            return true;
        }

        if (howToPlayButton.contains(screenX, correctedY)) {
            game.setScreen(howToPlayScreen);
            return true;
        }

        if (statsButton.contains(screenX, correctedY)) {
            game.setScreen(statsScreen);
            return true;
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override
    public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override
    public boolean scrolled(float amountX, float amountY) { return false; }
    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
}
