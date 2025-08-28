package com.gamejam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Tile {
    private char letter;
    private TileState state;

    private static final GlyphLayout layout = new GlyphLayout();

    public Tile() {
        this.letter = ' ';
        this.state = TileState.EMPTY;
    }

    public void setLetter(char letter) {
        this.letter = letter;
    }
    public char getLetter() {
        return letter;
    }
    public TileState getState() {
        return state;
    }
    public void setState(TileState state) {
        this.state = state;
    }

    /**
     * Renders the tile's background shape and border.
     * This method assumes the ShapeRenderer has already been set up with begin().
     *
     * @param shapeRenderer The ShapeRenderer for drawing shapes.
     * @param x             The x-coordinate of the tile.
     * @param y             The y-coordinate of the tile.
     * @param tileSize      The size of the tile.
     * @param scale         The scale for animation.
     * @param alpha         The alpha value for fade-in effects.
     */
    public void renderShape(ShapeRenderer shapeRenderer, float x, float y, float tileSize, float scale, float alpha) {
        // --- Calculate scaled dimensions and offsets to center the tile ---
        float scaledSize = tileSize * scale;
        float xOffset = (tileSize - scaledSize) / 2;
        float yOffset = (tileSize - scaledSize) / 2;

        // --- Draw filled square only if the tile state has been updated (submitted) ---
        if (state != TileState.EMPTY) {
            Color color;
            switch (state) {
                case CORRECT:
                    color = WordleColors.CORRECT;
                    break;
                case PRESENT:
                    color = WordleColors.PRESENT;
                    break;
                case ABSENT:
                    color = WordleColors.ABSENT;
                    break;
                default:
                    // This case should not be reached with the current game logic,
                    // but we'll fall back to a transparent color just in case.
                    color = new Color(0, 0, 0, 0);
            }
            shapeRenderer.setColor(color.r, color.g, color.b, alpha);
            shapeRenderer.rect(x + xOffset, y + yOffset, scaledSize, scaledSize);
        }

        // --- Draw the border with variable thickness and color ---
        Color borderColor;
        float borderThickness;

        if (state == TileState.EMPTY) {
            if (letter != ' ') {
                // Letter has been typed, but not submitted
                borderColor = WordleColors.TYPED_OUTLINE;
                borderThickness = 3f;
            } else {
                // No letter, empty tile
                borderColor = WordleColors.DEFAULT_OUTLINE;
                borderThickness = 2f;
            }

            // Draw the outline using four separate rectangles
            shapeRenderer.setColor(borderColor.r, borderColor.g, borderColor.b, alpha);
            shapeRenderer.rect(x + xOffset, y + yOffset, scaledSize, borderThickness); // Bottom
            shapeRenderer.rect(x + xOffset, y + yOffset + scaledSize - borderThickness, scaledSize, borderThickness); // Top
            shapeRenderer.rect(x + xOffset, y + yOffset + borderThickness, borderThickness, scaledSize - 2 * borderThickness); // Left
            shapeRenderer.rect(x + xOffset + scaledSize - borderThickness, y + yOffset + borderThickness, borderThickness, scaledSize - 2 * borderThickness); // Right

        }
    }

    /**
     * Renders the tile's letter.
     * This method assumes the SpriteBatch has already been set up with begin().
     *
     * @param batch         The SpriteBatch for drawing text.
     * @param font          The font to use for the letter.
     * @param x             The x-coordinate of the tile.
     * @param y             The y-coordinate of the tile.
     * @param tileSize      The size of the tile.
     * @param alpha         The alpha value for fade-in effects.
     */
    public void renderText(SpriteBatch batch, BitmapFont font, float x, float y, float tileSize, float alpha) {
        if (letter != ' ') {
            String text = String.valueOf(letter);
            layout.setText(font, text);

            float textX = x + (tileSize - layout.width) / 2;
            float textY = y + (tileSize + layout.height) / 2;

            font.setColor(1.0f, 1.0f, 1.0f, alpha);
            font.draw(batch, layout, textX, textY);
        }
    }
}
