package com.gamejam;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.HashMap;
import java.util.Map;

public class Keyboard {
    private final String[] rows = {
        "QWERTYUIOP",
        "ASDFGHJKL",
        "↵ZXCVBNM⌫"   // ↵ = Enter, ⌫ = Backspace
    };

    private Map<Character, TileState> keyStates = new HashMap<>();

    public Keyboard() {
        // init all keys as unused
        for (String row : rows) {
            for (char c : row.toCharArray()) {
                if (Character.isLetter(c)) {
                    keyStates.put(c, TileState.EMPTY);
                }
            }
        }
    }

    public void updateKeyState(char c, TileState state) {
        c = Character.toUpperCase(c);
        if (!keyStates.containsKey(c)) return;

        // Don't downgrade GREEN → YELLOW/GRAY
        TileState current = keyStates.get(c);
        if (current == TileState.CORRECT) return;
        if (current == TileState.PRESENT && state == TileState.ABSENT) return;

        keyStates.put(c, state);
    }

    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, BitmapFont font, float screenWidth) {
        float keyWidth = 48f;
        float keyHeight = 64f;
        float spacing = 8f;
        float startY = 50f; // bottom margin
        GlyphLayout layout = new GlyphLayout();

        for (int r = 0; r < rows.length; r++) {
            String row = rows[r];
            float rowWidth = row.length() * (keyWidth + spacing) - spacing;
            float startX = (screenWidth - rowWidth) / 2f;

            for (int i = 0; i < row.length(); i++) {
                char c = row.charAt(i);

                float x = startX + i * (keyWidth + spacing);
                float y = startY + (rows.length - 1 - r) * (keyHeight + spacing);

                // background color
                Color color = Color.DARK_GRAY;
                if (Character.isLetter(c)) {
                    switch (keyStates.get(c)) {
                        case CORRECT: color = Color.GREEN; break;
                        case PRESENT: color = Color.YELLOW; break;
                        case ABSENT:  color = Color.GRAY; break;
                        default:      color = Color.DARK_GRAY;
                    }
                } else {
                    color = Color.LIGHT_GRAY; // Enter/Backspace
                }

                // draw key background
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(color);
                shapeRenderer.rect(x, y, keyWidth, keyHeight);
                shapeRenderer.end();

                // draw key border
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(Color.BLACK);
                shapeRenderer.rect(x, y, keyWidth, keyHeight);
                shapeRenderer.end();

                // draw letter centered
                batch.begin();
                String label = String.valueOf(c);
                layout.setText(font, label);
                float textX = x + (keyWidth - layout.width) / 2f;
                float textY = y + (keyHeight + layout.height) / 2f;
                font.draw(batch, layout, textX, textY);
                batch.end();
            }
        }
    }
}

