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
 * A screen that displays the game rules and instructions.
 * This screen displays all content on a single screen without scrolling.
 */
public class HowToPlayScreen implements Screen {

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

    public HowToPlayScreen(Main game) {
        this.game = game;
        this.batch = game.getBatch();
        this.shapeRenderer = game.getShapeRenderer();
        this.font = game.getFont();

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void show() {
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

        batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawRoundedButtonShape(backButton);
        drawContentShapes();
        shapeRenderer.end();

        batch.begin();
        drawRoundedButtonText(backButton, "BACK TO MENU");
        drawContentText();
        batch.end();
    }

    /**
     * Helper method to draw the shapes for the main content.
     */
    private void drawContentShapes() {
        float padding = 50;
        float columnWidth = (Gdx.graphics.getWidth() / 2) - (padding * 1.5f);
        float currentYLeft = Gdx.graphics.getHeight() - padding;
        float currentYRight = Gdx.graphics.getHeight() - padding;
        float column1X = padding;
        float column2X = Gdx.graphics.getWidth() / 2 + padding / 2;

        font.getData().setScale(1.5f);
        layout.setText(font, "HOW TO PLAY");
        currentYLeft -= layout.height + 40;
        currentYRight -= layout.height + 40;
        font.getData().setScale(0.7f);

        String rulesText = "Guess the word in 6 tries. After each guess, the color of the tiles will change to show how close your guess was to the word.";
        layout.setText(font, rulesText, Color.WHITE, columnWidth, Align.left, true);
        currentYLeft -= layout.height + 30;

        // Example 1: Correct
        currentYLeft -= 30;
        drawExampleTilesShapes(column1X, columnWidth, currentYLeft, "WEARY", new boolean[]{true, false, false, false, false});
        currentYLeft -= 80;

        // Example 2: Exists
        currentYLeft -= 30;
        drawExampleTilesShapes(column1X, columnWidth, currentYLeft, "PILOT", new boolean[]{false, true, false, false, false});
        currentYLeft -= 80;

        // Example 3: Wrong
        currentYLeft -= 30;
        drawExampleTilesShapes(column1X, columnWidth, currentYLeft, "VAGUE", new boolean[]{false, false, false, false, true});

        // Game Modes (Right Column)
        font.getData().setScale(1.1f);
        layout.setText(font, "GAME MODES");
        currentYRight -= layout.height + 30;
        font.getData().setScale(0.7f);

        // BUNDLE Mode
        layout.setText(font, "BUNDLE");
        currentYRight -= layout.height + 10;
        String bundleText = "A bundle of 6 words will have to be guessed. Each previous solution word will be carried over as a guess to the next stage.";
        layout.setText(font, bundleText, Color.WHITE, columnWidth, Align.left, true);
        currentYRight -= layout.height + 30;

        // CLASSIC Mode
        layout.setText(font, "CLASSIC");
        currentYRight -= layout.height + 10;
        String classicText = "This is a normal game of wordle with a single word to guess.";
        layout.setText(font, classicText, Color.WHITE, columnWidth, Align.left, true);
        currentYRight -= layout.height + 30;

        // Abilities
        font.getData().setScale(1.1f);
        layout.setText(font, "ABILITIES");
        currentYRight -= layout.height + 30;
        font.getData().setScale(0.7f);
        layout.setText(font, "I didn't have time to implement this shi");
        currentYRight -= layout.height + 10;
    }

    /**
     * Helper method to draw the text for the main content.
     */
    private void drawContentText() {
        float padding = 50;
        float columnWidth = (Gdx.graphics.getWidth() / 2) - (padding * 1.5f);
        float currentYLeft = Gdx.graphics.getHeight() - padding;
        float currentYRight = Gdx.graphics.getHeight() - padding;
        float column1X = padding;
        float column2X = Gdx.graphics.getWidth() / 2 + padding / 2;

        // HOW TO PLAY
        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);
        layout.setText(font, "HOW TO PLAY");
        font.draw(batch, layout, (Gdx.graphics.getWidth() - layout.width) / 2, currentYLeft);
        currentYLeft -= layout.height + 40;
        currentYRight -= layout.height + 40;
        font.getData().setScale(0.7f);

        // Wordle Rules (Left Column)
        String rulesText = "Guess the word in 6 tries. After each guess, the color of the tiles will change to show how close your guess was to the word.";
        layout.setText(font, rulesText, Color.WHITE, columnWidth, Align.left, true);
        font.draw(batch, layout, column1X, currentYLeft);
        currentYLeft -= layout.height + 30;

        // Example 1: Correct
        font.draw(batch, "Correct Letter, Correct Position", column1X, currentYLeft);
        currentYLeft -= 30;
        drawExampleTilesLetters(column1X, columnWidth, currentYLeft - 70, "WEARY");
        currentYLeft -= 80;

        // Example 2: Exists
        font.draw(batch, "Correct Letter, Wrong Position", column1X, currentYLeft);
        currentYLeft -= 30;
        drawExampleTilesLetters(column1X, columnWidth, currentYLeft - 70, "PILOT");
        currentYLeft -= 80;

        // Example 3: Wrong
        font.draw(batch, "Wrong Letter", column1X, currentYLeft);
        currentYLeft -= 30;
        drawExampleTilesLetters(column1X, columnWidth, currentYLeft - 70, "VAGUE");

        // Game Modes (Right Column)
        font.getData().setScale(1.1f);
        layout.setText(font, "GAME MODES");
        font.draw(batch, layout, column2X, currentYRight);
        currentYRight -= layout.height + 30;
        font.getData().setScale(0.7f);

        // BUNDLE Mode
        layout.setText(font, "BUNDLE");
        font.draw(batch, layout, column2X, currentYRight);
        currentYRight -= layout.height + 10;
        String bundleText = "A bundle of 6 words will have to be guessed. Each previous solution word will be carried over as a guess to the next stage.";
        layout.setText(font, bundleText, Color.WHITE, columnWidth, Align.left, true);
        font.draw(batch, layout, column2X, currentYRight);
        currentYRight -= layout.height + 30;

        // CLASSIC Mode
        layout.setText(font, "CLASSIC");
        font.draw(batch, layout, column2X, currentYRight);
        currentYRight -= layout.height + 10;
        String classicText = "This is a normal game of WORDLE with a single word to guess.";
        layout.setText(font, classicText, Color.WHITE, columnWidth, Align.left, true);
        font.draw(batch, layout, column2X, currentYRight);
        currentYRight -= layout.height + 30;

        // Abilities
        font.getData().setScale(0.3f);
        layout.setText(font, "ABILITIES");
        font.draw(batch, layout, column2X - 50, currentYRight - 80);
        currentYRight -= layout.height + 30;
        font.getData().setScale(0.3f);
        layout.setText(font, "I didn't have time to implement this shi");
        font.draw(batch, layout, column2X - 50, currentYRight - 60);
        font.getData().setScale(1f);
    }

    /**
     * Helper method to draw only the shapes of the example tiles.
     */
    private void drawExampleTilesShapes(float x, float columnWidth, float y, String word, boolean[] isCorrect) {
        float tileSize = 60;
        float tileSpacing = 10;
        float totalWidth = (tileSize + tileSpacing) * 5 - tileSpacing;
        float startX = x + (columnWidth - totalWidth) / 2;

        for (int i = 0; i < 5; i++) {
            float tileX = startX + (tileSize + tileSpacing) * i;

            Color tileColor = Color.valueOf("#3A3A3C");
            if (i == 0 && word.equals("WEARY")) {
                tileColor = Color.valueOf("#538D4E"); // Green for correct
            } else if (i == 1 && word.equals("PILOT")) {
                tileColor = Color.valueOf("#B59F3B"); // Yellow for exists
            } else if (i == 4 && word.equals("VAGUE")) {
                tileColor = Color.valueOf("#3A3A3C"); // Grey for wrong
            }

            shapeRenderer.setColor(tileColor);
            shapeRenderer.rect(tileX, y - 70, tileSize, tileSize);
        }
    }

    /**
     * Helper method to draw only the letters of the example tiles.
     */
    private void drawExampleTilesLetters(float x, float columnWidth, float y, String word) {
        float tileSize = 60;
        float tileSpacing = 10;
        float totalWidth = (tileSize + tileSpacing) * 5 - tileSpacing;
        float startX = x + (columnWidth - totalWidth) / 2;

        font.setColor(Color.WHITE);
        for (int i = 0; i < 5; i++) {
            float tileX = startX + (tileSize + tileSpacing) * i;
            String letter = String.valueOf(word.charAt(i));
            layout.setText(font, letter);
            font.draw(batch, layout, tileX + (tileSize - layout.width) / 2, y + (tileSize + layout.height) / 2);
        }
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
