package com.gamejam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Align;

/**
 * A screen that displays game credits.
 * This screen displays all content on a single screen without scrolling.
 */
public class CreditsScreen implements Screen {

    private final Main game;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    // Back button properties
    private Rectangle backButton;
    private final float BUTTON_WIDTH = 300;
    private final float BUTTON_HEIGHT = 100;
    private final float BUTTON_CORNER_RADIUS = 20;

    public CreditsScreen(Main game) {
        this.game = game;
        this.batch = game.getBatch();
        this.shapeRenderer = game.getShapeRenderer();
        this.font = game.getFont();

        // Position the back button
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void show() {
        // Handle input for button clicks
        Gdx.input.setInputProcessor(new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                int correctedY = Gdx.graphics.getHeight() - screenY;
                if (backButton.contains(screenX, correctedY)) {
                    game.setScreen(game.menuScreen);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        // All drawing uses a fixed, non-scrolling camera
        batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // --- Draw all shapes first ---
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawRoundedButtonShape(backButton);
        shapeRenderer.end();

        // --- Then draw all text on top ---
        batch.begin();
        drawRoundedButtonText(backButton, "BACK TO MENU");
        drawCreditsContent();
        batch.end();
    }

    /**
     * Helper method to draw all the text for the credits content in a window-type layout.
     */
    private void drawCreditsContent() {
        float padding = 50;
        float halfWidth = Gdx.graphics.getWidth() / 2;
        float halfHeight = Gdx.graphics.getHeight() / 2;

        font.setColor(Color.WHITE);

        // Main Title
        font.getData().setScale(1.5f);
        layout.setText(font, "CREDITS");
        font.draw(batch, layout, (Gdx.graphics.getWidth() - layout.width) / 2, Gdx.graphics.getHeight() - padding);

        // Left Column (Top and Bottom)
        float leftX = padding;
        float leftY = Gdx.graphics.getHeight() - padding - layout.height - 60;
        float leftSectionWidth = halfWidth - padding * 1.5f;

        // Top-Left: Game Creators
        font.getData().setScale(1.1f);
        layout.setText(font, "Sprites");
        font.draw(batch, layout, leftX, leftY);
        leftY -= layout.height + 20;
        font.getData().setScale(0.7f);
        layout.setText(font, "Mosslight Series: Bunny Pack\nBy Givty", Color.WHITE, leftSectionWidth, Align.left, true);
        font.draw(batch, layout, leftX, leftY);
        layout.setText(font, "Carrot\nBy Defadaj", Color.WHITE, leftSectionWidth, Align.left, true);
        font.draw(batch, layout, leftX, leftY - 100);

        // Bottom-Left: Sound
        leftY = halfHeight - 40;
        font.getData().setScale(1.1f);
        layout.setText(font, "SOUND");
        font.draw(batch, layout, leftX, leftY);
        leftY -= layout.height + 20;
        font.getData().setScale(0.7f);
        layout.setText(font, "FX: yea i got no time bro sorry", Color.WHITE, leftSectionWidth, Align.left, true);
        font.draw(batch, layout, leftX, leftY);

        // Right Column (Top and Bottom)
        float rightX = halfWidth + padding / 2;
        float rightY = Gdx.graphics.getHeight() - padding - layout.height - 60;
        float rightSectionWidth = halfWidth - padding * 1.5f;

        // Top-Right: Art
        font.getData().setScale(1.1f);
        layout.setText(font, "SPECIAL THANKS");
        font.draw(batch, layout, rightX, rightY - 30);
        rightY -= layout.height + 50;
        font.getData().setScale(0.7f);
        layout.setText(font, "WORDLE from NYT", Color.WHITE, rightSectionWidth, Align.left, true);
        font.draw(batch, layout, rightX, rightY);

        // Bottom-Right: Special Thanks
        rightY = halfHeight - 20;
        font.getData().setScale(1.1f);
        layout.setText(font, "GUY WHO MADE TS");
        font.draw(batch, layout, rightX, rightY);
        rightY -= layout.height + 20;
        font.getData().setScale(0.7f);
        layout.setText(font, "Richmond\nFor Comments/Suggestions:\nDiscord: salted.caramel", Color.WHITE, rightSectionWidth, Align.left, true);
        font.draw(batch, layout, rightX, rightY);

        // Reset the font size to its default scale
        font.getData().setScale(1.0f);
    }

    /**
     * Helper method to draw only the shape of a rounded button.
     * @param button The Rectangle defining the button bounds.
     */
    private void drawRoundedButtonShape(Rectangle button) {
        shapeRenderer.rect(button.x + BUTTON_CORNER_RADIUS, button.y, button.width - 2 * BUTTON_CORNER_RADIUS, button.height);
        shapeRenderer.rect(button.x, button.y + BUTTON_CORNER_RADIUS, BUTTON_CORNER_RADIUS, button.height - 2 * BUTTON_CORNER_RADIUS);
        shapeRenderer.rect(button.x + button.width - BUTTON_CORNER_RADIUS, button.y + BUTTON_CORNER_RADIUS, BUTTON_CORNER_RADIUS, button.height - 2 * BUTTON_CORNER_RADIUS);
        shapeRenderer.circle(button.x + BUTTON_CORNER_RADIUS, button.y + BUTTON_CORNER_RADIUS, BUTTON_CORNER_RADIUS);
        shapeRenderer.circle(button.x + button.width - BUTTON_CORNER_RADIUS, button.y + BUTTON_CORNER_RADIUS, BUTTON_CORNER_RADIUS);
        shapeRenderer.circle(button.x + BUTTON_CORNER_RADIUS, button.y + button.height - BUTTON_CORNER_RADIUS, BUTTON_CORNER_RADIUS);
        shapeRenderer.circle(button.x + button.width - BUTTON_CORNER_RADIUS, button.y + button.height - BUTTON_CORNER_RADIUS, BUTTON_CORNER_RADIUS);
    }

    /**
     * Helper method to draw only the text on a rounded button.
     * @param button The Rectangle defining the button bounds.
     * @param text The text to display on the button.
     */
    private void drawRoundedButtonText(Rectangle button, String text) {
        font.setColor(Color.WHITE);
        layout.setText(font, text);
        float buttonTextX = button.x + (button.width - layout.width) / 2;
        float buttonTextY = button.y + (button.height + layout.height) / 2;
        font.draw(batch, text, buttonTextX, buttonTextY);
    }

    @Override
    public void resize(int width, int height) {
        // Position the fixed back button
        backButton = new Rectangle(Gdx.graphics.getWidth() - BUTTON_WIDTH - 50, 50, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    @Override
    public void pause() { }
    @Override
    public void resume() { }
    @Override
    public void hide() { }
    @Override
    public void dispose() { }
}
