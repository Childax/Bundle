package com.gamejam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
            case CORRECT: color = WordleColors.CORRECT; break;
            case PRESENT: color = WordleColors.PRESENT; break;
            case ABSENT:  color = WordleColors.ABSENT; break;
            default:      color = new Color(0.2f, 0.2f, 0.2f, 1f); // A slightly darker shade of background for EMPTY
        }

        // --- Calculate scaled dimensions and offsets to center the tile ---
        float scaledSize = tileSize * scale;
        float xOffset = (tileSize - scaledSize) / 2;
        float yOffset = (tileSize - scaledSize) / 2;

        // --- Draw filled square only if the tile state has been updated (submitted) ---
        if (state != TileState.EMPTY) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(color);
            shapeRenderer.rect(x + xOffset, y + yOffset, scaledSize, scaledSize);
            shapeRenderer.end();
        }

        // --- Draw the border with variable thickness ---
        // Gdx.gl.glLineWidth() is a global GL state, so we must set it and then reset it.
        // The default line width is 1.0.
        float borderWidth = 1.0f;
        Color borderColor;
        if (letter != ' ' && state == TileState.EMPTY) {
            // Thicker border for typed letters that haven't been submitted
            borderColor = WordleColors.TYPED_OUTLINE;
        } else if (letter == ' ' && state == TileState.EMPTY) {
            // Thin border for initial empty tiles
            borderColor = WordleColors.DEFAULT_OUTLINE;
        } else {
            // No outline for submitted tiles
            borderWidth = 0;
            borderColor = getColor(getState());
        }

        Gdx.gl.glLineWidth(borderWidth);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(borderColor);
        shapeRenderer.rect(x + xOffset, y + yOffset, scaledSize, scaledSize);
        shapeRenderer.end();
        // Reset the line width back to default to avoid affecting other renderings
        Gdx.gl.glLineWidth(1.0f);

        // --- Draw letter (centered) ---
        if (letter != ' ') {
            String text = String.valueOf(letter);
            layout.setText(font, text);

            float textX = x + (tileSize - layout.width) / 2;
            float textY = y + (tileSize + layout.height) / 2;

            batch.begin();
            font.setColor(Color.WHITE); // Use white for the letter color
            font.draw(batch, layout, textX, textY);
            batch.end();
        }
    }

    public static Color getColor(TileState state) {
        switch (state) {
            case CORRECT: return WordleColors.CORRECT;
            case PRESENT: return WordleColors.PRESENT;
            case ABSENT:  return WordleColors.ABSENT;
            default:      return Color.LIGHT_GRAY;
        }
    }
}
