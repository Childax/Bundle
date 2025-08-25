package com.gamejam;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

public class Tile {
    private char letter;
    private TileState state;

    // Reuse one GlyphLayout to avoid GC churn
    private static final GlyphLayout layout = new GlyphLayout();

    public Tile() {
        this.letter = ' ';
        this.state = TileState.EMPTY;
    }

    public void setLetter(char letter) { this.letter = letter; }
    public char getLetter() { return letter; }
    public TileState getState() { return state; }
    public void setState(TileState state) { this.state = state; }

    /**
     * Renders the tile with a default scale of 1.0.
     */
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, BitmapFont font,
                       float x, float y, float tileSize) {
        render(batch, shapeRenderer, font, x, y, tileSize, 1.0f);
    }

    /**
     * Renders the tile with a specified scale for animation.
     */
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, BitmapFont font,
                       float x, float y, float tileSize, float scale) {

        // --- Pick color based on state ---
        Color color;
        switch (state) {
            case CORRECT: color = Color.GREEN; break;
            case PRESENT: color = Color.GOLD; break;
            case ABSENT:  color = Color.DARK_GRAY; break;
            default:      color = Color.LIGHT_GRAY; // EMPTY
        }

        // --- Calculate scaled dimensions and offsets to center the tile ---
        float scaledSize = tileSize * scale;
        float xOffset = (tileSize - scaledSize) / 2;
        float yOffset = (tileSize - scaledSize) / 2;

        // --- Draw filled square ---
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x + xOffset, y + yOffset, scaledSize, scaledSize);
        shapeRenderer.end();

        // --- Draw border ---
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(x + xOffset, y + yOffset, scaledSize, scaledSize);
        shapeRenderer.end();

        // --- Draw letter (centered) ---
        if (letter != ' ') {
            String text = String.valueOf(letter);
            layout.setText(font, text);

            float textX = x + (tileSize - layout.width) / 2;
            float textY = y + (tileSize + layout.height) / 2;

            batch.begin();
            font.setColor(Color.BLACK);
            font.draw(batch, layout, textX, textY);
            batch.end();
        }
    }
}
