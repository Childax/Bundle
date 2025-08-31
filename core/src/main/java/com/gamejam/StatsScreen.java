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

import java.util.Objects;

/**
 * A screen to display the player's game statistics.
 */
public class StatsScreen implements Screen, InputProcessor {

    private final Main game;
    private final PlayerProfile playerProfile;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    // Button properties
    private Rectangle backButton;
    private Rectangle resetButton;
    private Rectangle yesButton;
    private Rectangle noButton;
    private final float BUTTON_WIDTH = 200;
    private final float BUTTON_HEIGHT = 75;
    private final float BUTTON_CORNER_RADIUS = 15;
    private boolean isConfirmingReset = false; // Flag to show the confirmation dialog

    public StatsScreen(Main game, PlayerProfile playerProfile) {
        this.game = game;
        this.playerProfile = playerProfile;
        this.batch = game.getBatch();
        this.shapeRenderer = game.getShapeRenderer();
        this.font = game.getFont();

        // Position the buttons
        float backX = (Gdx.graphics.getWidth() - BUTTON_WIDTH) / 2;
        float backY = 50;

        backButton = new Rectangle(backX, backY, BUTTON_WIDTH, BUTTON_HEIGHT);
        resetButton = new Rectangle(Gdx.graphics.getWidth() - BUTTON_WIDTH - 20, Gdx.graphics.getHeight() - BUTTON_HEIGHT - 20, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        float startX = (Gdx.graphics.getWidth()) / 2f - 400;
        float rightStartX = (Gdx.graphics.getWidth()) / 2f + 50;
        float statsY = Gdx.graphics.getHeight() - 300;

        // Draw the title
        batch.begin();
        font.setColor(Color.WHITE);
        layout.setText(font, "PLAYER STATS");
        float titleX = startX;
        float titleY = Gdx.graphics.getHeight() - 150;
        font.draw(batch, "PLAYER STATS", titleX, titleY);

        // Draw the username
        font.setColor(Color.WHITE);
        font.draw(batch, playerProfile.getUsername(), titleX, titleY - 50);

        // Draw the stats
        this.font.getData().setScale(0.7f);
        GameStats stats = playerProfile.getGameStats();

        // Left side stats
        font.setColor(Color.WHITE);
        font.draw(batch, "Total Words Solved: " + stats.getTotalWordsSolved(), startX, statsY);
        if (!Objects.equals(stats.getBestWord(), "")) {
            font.draw(batch, String.format("Best Word: %s (in %d)", stats.getBestWord(), stats.getBestWordGuesses()), startX, statsY - 50);
        } else {
            font.draw(batch, String.format("Best Word: %s", "N/A"), startX, statsY - 50);
        }
        font.draw(batch, String.format("Avg. Guess/Word Solved: %.2f", stats.getAverageGuesses()), startX, statsY - 100);

        // Right side stats
        font.draw(batch, "BUNDLES Solved: " + stats.getBundlesSolved(), rightStartX, statsY);
        font.draw(batch, "CLASSICs Solved: " + stats.getClassicsSolved(), rightStartX, statsY - 50);

        if (stats.getBestTimeBundle() > 0 && stats.getBundlesSolved() > 0) {
            font.draw(batch, String.format("Best Time (BUNDLE): %.2f s", stats.getBestTimeBundle()), rightStartX, statsY - 100);
        } else {
            font.draw(batch, "Best Time (BUNDLE): N/A", rightStartX, statsY - 100);
        }

        if (stats.getBestTimeClassic() > 0 && stats.getClassicsSolved() > 0) {
            font.draw(batch, String.format("Best Time (CLASSIC): %.2f s", stats.getBestTimeClassic()), rightStartX, statsY - 150);
        } else {
            font.draw(batch, "Best Time (CLASSIC): N/A", rightStartX, statsY - 150);
        }

        batch.end();
        this.font.getData().setScale(1f);

        // Draw the back and reset buttons
        drawRoundedButton(backButton, "BACK", Color.valueOf("#4C8BF5"));
        drawRoundedButton(resetButton, "RESET", Color.RED);

        // Draw the confirmation dialog if the flag is set
        if (isConfirmingReset) {
            drawConfirmationDialog();
        }
    }

    /**
     * Helper method to draw a rounded rectangle button with text.
     * @param button The Rectangle defining the button bounds.
     * @param text The text to display on the button.
     * @param color The color of the button.
     */
    private void drawRoundedButton(Rectangle button, String text, Color color) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
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

    /**
     * Draws a simple confirmation dialog for the reset action.
     */
    private void drawConfirmationDialog() {
        // Draw a dark overlay to make the dialog stand out
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer.end();

        // Draw the dialog box
        float dialogWidth = 400;
        float dialogHeight = 200;
        float dialogX = (Gdx.graphics.getWidth() - dialogWidth) / 2;
        float dialogY = (Gdx.graphics.getHeight() - dialogHeight) / 2;
        Rectangle dialogBox = new Rectangle(dialogX, dialogY, dialogWidth, dialogHeight);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.valueOf("#3A405A"));
        shapeRenderer.rect(dialogBox.x, dialogBox.y, dialogBox.width, dialogBox.height);
        shapeRenderer.end();

        // Position the Yes and No buttons
        float buttonSpacing = 20;
        float buttonsTotalWidth = (BUTTON_WIDTH * 2) + buttonSpacing;
        float buttonsStartX = dialogX + (dialogWidth - buttonsTotalWidth) / 2;
        float buttonsY = dialogY + 20;

        yesButton = new Rectangle(buttonsStartX, buttonsY, BUTTON_WIDTH, BUTTON_HEIGHT);
        noButton = new Rectangle(buttonsStartX + BUTTON_WIDTH + buttonSpacing, buttonsY, BUTTON_WIDTH, BUTTON_HEIGHT);

        drawRoundedButton(yesButton, "YES", Color.valueOf("#4C8BF5"));
        drawRoundedButton(noButton, "NO", Color.RED);

        // Draw the confirmation text
        batch.begin();
        font.setColor(Color.WHITE);
        layout.setText(font, "Are you sure you want to reset your profile?");
        float textX = dialogX + (dialogWidth - layout.width) / 2;
        float textY = dialogY + dialogHeight - 50;
        font.draw(batch, "Are you sure you want to reset your profile?", textX, textY);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        backButton.x = (width - BUTTON_WIDTH) / 2;
        resetButton.x = Gdx.graphics.getWidth() - BUTTON_WIDTH - 20;
        resetButton.y = Gdx.graphics.getHeight() - BUTTON_HEIGHT - 20;
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
    public boolean keyDown(int keycode) { return false; }
    @Override
    public boolean keyUp(int keycode) { return false; }
    @Override
    public boolean keyTyped(char character) { return false; }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        int correctedY = Gdx.graphics.getHeight() - screenY;

        // Handle dialog button presses first
        if (isConfirmingReset) {
            if (yesButton.contains(screenX, correctedY)) {
                // User confirmed reset
                game.resetAndGoToUsernameScreen();
                isConfirmingReset = false; // Reset the flag
                return true;
            } else if (noButton.contains(screenX, correctedY)) {
                // User canceled reset
                isConfirmingReset = false;
                return true;
            }
        }

        // Handle main screen button presses
        if (backButton.contains(screenX, correctedY)) {
            game.setScreen(game.menuScreen);
            return true;
        }

        if (resetButton.contains(screenX, correctedY)) {
            isConfirmingReset = true; // Show confirmation dialog
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
