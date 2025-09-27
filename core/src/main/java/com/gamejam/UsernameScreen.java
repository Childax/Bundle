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
 * A screen to prompt the user for a username on the first run of the game.
 */
public class UsernameScreen implements Screen, InputProcessor {

    private final Main game;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();
    private String username = "";
    private Rectangle continueButton;
    private final float BUTTON_WIDTH = 250;
    private final float BUTTON_HEIGHT = 75;
    private final float BUTTON_CORNER_RADIUS = 15;
    private final float INPUT_BOX_WIDTH = 400;
    private final float INPUT_BOX_HEIGHT = 70;
    private Rectangle inputBox;

    public UsernameScreen(Main game) {
        this.game = game;
        this.batch = game.getBatch();
        this.shapeRenderer = game.getShapeRenderer();
        this.font = game.getFont();

        float centerX = Gdx.graphics.getWidth() / 2f;
        float centerY = Gdx.graphics.getHeight() / 2f;

        inputBox = new Rectangle(centerX - INPUT_BOX_WIDTH / 2, centerY, INPUT_BOX_WIDTH, INPUT_BOX_HEIGHT);
        continueButton = new Rectangle(centerX - BUTTON_WIDTH / 2, centerY - 150, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        drawRoundedRect(inputBox, 10);
        shapeRenderer.end();

        drawRoundedButton(continueButton, "CONTINUE");

        batch.begin();
        font.setColor(Color.WHITE);
        layout.setText(font, "ENTER YOUR USERNAME");
        font.draw(batch, layout, Gdx.graphics.getWidth() / 2f - layout.width / 2, Gdx.graphics.getHeight() / 2f + 200);

        font.setColor(Color.BLACK);
        layout.setText(font, username.isEmpty() ? "Type here..." : username);
        font.draw(batch, layout, inputBox.x + inputBox.width / 2 - layout.width / 2, inputBox.y + inputBox.height / 2 + layout.height / 2);

        batch.end();
    }

    /**
     * Helper method to draw a rounded rectangle.
     * @param rect The Rectangle defining the bounds.
     * @param radius The radius of the corners.
     */
    private void drawRoundedRect(Rectangle rect, float radius) {
        shapeRenderer.rect(rect.x + radius, rect.y, rect.width - 2 * radius, rect.height);
        shapeRenderer.rect(rect.x, rect.y + radius, radius, rect.height - 2 * radius);
        shapeRenderer.rect(rect.x + rect.width - radius, rect.y + radius, radius, rect.height - 2 * radius);
        shapeRenderer.circle(rect.x + radius, rect.y + radius, radius);
        shapeRenderer.circle(rect.x + rect.width - radius, rect.y + radius, radius);
        shapeRenderer.circle(rect.x + radius, rect.y + rect.height - radius, radius);
        shapeRenderer.circle(rect.x + rect.width - radius, rect.y + rect.height - radius, radius);
    }

    /**
     * Helper method to draw a rounded rectangle button with text.
     * @param button The Rectangle defining the button bounds.
     * @param text The text to display on the button.
     */
    private void drawRoundedButton(Rectangle button, String text) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.valueOf("#4C8BF5"));
        drawRoundedRect(button, BUTTON_CORNER_RADIUS);
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
        float centerX = width / 2f;
        float centerY = height / 2f;
        inputBox.x = centerX - INPUT_BOX_WIDTH / 2;
        inputBox.y = centerY;
        continueButton.x = centerX - BUTTON_WIDTH / 2;
        continueButton.y = centerY - 150;
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() { }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.BACKSPACE) {
            if (!username.isEmpty()) {
                username = username.substring(0, username.length() - 1);
            }
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) { return false; }

    @Override
    public boolean keyTyped(char character) {
        if (Character.isLetterOrDigit(character) || character == ' ') {
            if (username.length() < 15) {
                username += character;
            }
        }
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        int correctedY = Gdx.graphics.getHeight() - screenY;
        if (continueButton.contains(screenX, correctedY)) {
            if (!username.isEmpty()) {
                game.setPlayerUsername(username);
                return true;
            }
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
