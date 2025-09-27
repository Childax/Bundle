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
 */
public class MenuScreen implements Screen, InputProcessor {

    private final Main game;
    private final StatsScreen statsScreen;
    private final HowToPlayScreen howToPlayScreen;
    private final CreditsScreen creditsScreen;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    private Rectangle bundleButton;
    private Rectangle classicButton;
    private Rectangle statsButton;
    private Rectangle howToPlayButton;
    private Rectangle creditsButton;
    private final float BUTTON_WIDTH = 300;
    private final float BUTTON_HEIGHT = 100;
    private final float BUTTON_CORNER_RADIUS = 20;
    private final float BUTTON_SPACING = 30;

    public MenuScreen(Main game, StatsScreen statsScreen, HowToPlayScreen howToPlayScreen, CreditsScreen creditsScreen) {
        this.game = game;
        this.statsScreen = statsScreen;
        this.howToPlayScreen = howToPlayScreen;
        this.creditsScreen = creditsScreen;
        this.batch = game.getBatch();
        this.shapeRenderer = game.getShapeRenderer();
        this.font = game.getFont();

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        batch.begin();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);
        layout.setText(font, "BUNDLE");
        float titleX = 50;
        float titleY = Gdx.graphics.getHeight() / 2 + layout.height / 2;
        font.draw(batch, layout, titleX, titleY);
        batch.end();

        drawRoundedButton(bundleButton, "BUNDLE");
        drawRoundedButton(classicButton, "CLASSIC");
        drawRoundedButton(howToPlayButton, "HOW TO PLAY");
        drawRoundedButton(statsButton, "VIEW STATS");
        drawRoundedButton(creditsButton, "CREDITS");
    }

    /**
     * Helper method to draw a rounded rectangle button with text.
     * @param button The Rectangle defining the button bounds.
     * @param text The text to display on the button.
     */
    private void drawRoundedButton(Rectangle button, String text) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.valueOf("#2A2A2A"));

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
        font.getData().setScale(0.8f);
        layout.setText(font, text);
        float buttonTextX = button.x + (button.width - layout.width) / 2;
        float buttonTextY = button.y + (button.height + layout.height) / 2;
        font.draw(batch, text, buttonTextX, buttonTextY);
        batch.end();
        font.getData().setScale(1.0f);
    }

    @Override
    public void resize(int width, int height) {
        float startX = (width - BUTTON_WIDTH) - 50;
        float totalButtonHeight = (BUTTON_HEIGHT * 5) + (BUTTON_SPACING * 4);
        float startY = (height - totalButtonHeight) / 2;

        bundleButton = new Rectangle(startX, startY + (BUTTON_HEIGHT + BUTTON_SPACING) * 4, BUTTON_WIDTH, BUTTON_HEIGHT);
        classicButton = new Rectangle(startX, startY + (BUTTON_HEIGHT + BUTTON_SPACING) * 3, BUTTON_WIDTH, BUTTON_HEIGHT);
        howToPlayButton = new Rectangle(startX, startY + (BUTTON_HEIGHT + BUTTON_SPACING) * 2, BUTTON_WIDTH, BUTTON_HEIGHT);
        statsButton = new Rectangle(startX, startY + (BUTTON_HEIGHT + BUTTON_SPACING), BUTTON_WIDTH, BUTTON_HEIGHT);
        creditsButton = new Rectangle(startX, startY, BUTTON_WIDTH, BUTTON_HEIGHT);
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

        if (creditsButton.contains(screenX, correctedY)) {
            game.setScreen(creditsScreen);
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
