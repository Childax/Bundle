package com.gamejam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
 * This screen handles the start button and provides a way to initiate the game.
 */
public class MenuScreen implements Screen, InputProcessor {

    private final Main game;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    // Start button properties
    private Rectangle startButton;
    private final float BUTTON_WIDTH = 300;
    private final float BUTTON_HEIGHT = 100;
    private final float BUTTON_CORNER_RADIUS = 20;

    public MenuScreen(Main game) {
        this.game = game;
        this.batch = game.getBatch();
        this.shapeRenderer = game.getShapeRenderer();
        this.font = game.getFont();
        // The start button is centered on the screen.
        float startX = (Gdx.graphics.getWidth() - BUTTON_WIDTH) / 2;
        float startY = (Gdx.graphics.getHeight() - BUTTON_HEIGHT) / 2;
        startButton = new Rectangle(startX, startY, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    @Override
    public void show() {
        // This is the crucial fix: set the input processor to this screen
        // whenever the screen becomes active. This ensures the menu buttons
        // work after returning from the game over screen.
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        // Draw the title
        batch.begin();
        font.setColor(Color.WHITE);
        layout.setText(font, "BUNDLE");
        float titleX = (Gdx.graphics.getWidth() - layout.width) / 2;
        float titleY = Gdx.graphics.getHeight() - 100;
        font.draw(batch, "BUNDLE", titleX, titleY);
        batch.end();

        // Draw the start button
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.valueOf("#4C8BF5")); // A nice blue color

        // Manual drawing of a rounded rectangle
        // Draw the central horizontal rectangle
        shapeRenderer.rect(startButton.x + BUTTON_CORNER_RADIUS, startButton.y,
            startButton.width - 2 * BUTTON_CORNER_RADIUS, startButton.height);

        // Draw the central vertical rectangles
        shapeRenderer.rect(startButton.x, startButton.y + BUTTON_CORNER_RADIUS,
            BUTTON_CORNER_RADIUS, startButton.height - 2 * BUTTON_CORNER_RADIUS);
        shapeRenderer.rect(startButton.x + startButton.width - BUTTON_CORNER_RADIUS,
            startButton.y + BUTTON_CORNER_RADIUS,
            BUTTON_CORNER_RADIUS, startButton.height - 2 * BUTTON_CORNER_RADIUS);

        // Draw the four corner circles
        shapeRenderer.circle(startButton.x + BUTTON_CORNER_RADIUS, startButton.y + BUTTON_CORNER_RADIUS, BUTTON_CORNER_RADIUS);
        shapeRenderer.circle(startButton.x + startButton.width - BUTTON_CORNER_RADIUS, startButton.y + BUTTON_CORNER_RADIUS, BUTTON_CORNER_RADIUS);
        shapeRenderer.circle(startButton.x + BUTTON_CORNER_RADIUS, startButton.y + startButton.height - BUTTON_CORNER_RADIUS, BUTTON_CORNER_RADIUS);
        shapeRenderer.circle(startButton.x + startButton.width - BUTTON_CORNER_RADIUS, startButton.y + startButton.height - BUTTON_CORNER_RADIUS, BUTTON_CORNER_RADIUS);

        shapeRenderer.end();

        // Draw the text on the button
        batch.begin();
        font.setColor(Color.WHITE);
        layout.setText(font, "START GAME");
        float buttonTextX = startButton.x + (startButton.width - layout.width) / 2;
        float buttonTextY = startButton.y + (startButton.height + layout.height) / 2;
        font.draw(batch, "START GAME", buttonTextX, buttonTextY);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // Update button position on resize to keep it centered
        startButton.x = (width - BUTTON_WIDTH) / 2;
        startButton.y = (height - BUTTON_HEIGHT) / 2;
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() { }

    @Override
    public void dispose() { }

    // --- InputProcessor methods ---
    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (startButton.contains(screenX, Gdx.graphics.getHeight() - screenY)) {
            // Start the game when the button is clicked
            game.startGame();
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }
}
