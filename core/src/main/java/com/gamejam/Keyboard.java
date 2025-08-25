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
        "ENTERZXCVBNMDEL"   // 1 = Enter, 2 = Backspace
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
        float startY = 50f;
        GlyphLayout layout = new GlyphLayout();

        for (int r = 0; r < rows.length; r++) {
            String rowStr = rows[r];

            // compute total row width
            float rowWidth = 0f;
            for (int i = 0; i < rowStr.length(); i++) {
                boolean isEnter = r == 2 && i == 0;
                boolean isDel   = r == 2 && i == rowStr.length() - 3;
                rowWidth += (isEnter || isDel ? keyWidth*2 : keyWidth) + spacing;
                if (isEnter) i += 4;
                if (isDel) i += 2;
            }
            rowWidth -= spacing;
            float startX = (screenWidth - rowWidth) / 2f;
            float xPos = startX;
            float yPos = startY + (rows.length - 1 - r) * (keyHeight + spacing);

            for (int i = 0; i < rowStr.length(); i++) {
                boolean isEnter = r == 2 && i == 0;
                boolean isDel   = r == 2 && i == rowStr.length() - 3;

                String keyLabel = String.valueOf(rowStr.charAt(i));
                float thisWidth = keyWidth;
                if (isEnter) { keyLabel = "ENTER"; thisWidth = keyWidth*2; }
                if (isDel)   { keyLabel = "DEL"; thisWidth = keyWidth*2; }

                // key background color
                Color keyBackgroundColor;
                if (keyLabel.length() == 1) {
                    TileState state = keyStates.get(keyLabel.charAt(0));
                    if (state == TileState.CORRECT) {
                        keyBackgroundColor = Color.GREEN;
                    } else if (state == TileState.PRESENT) {
                        keyBackgroundColor = Color.YELLOW;
                    } else if (state == TileState.ABSENT) {
                        keyBackgroundColor = Color.DARK_GRAY;
                    } else { // TileState.EMPTY
                        keyBackgroundColor = Color.LIGHT_GRAY;
                    }
                } else { // ENTER/DEL
                    keyBackgroundColor = Color.LIGHT_GRAY;
                }

                // draw background
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(keyBackgroundColor);
                shapeRenderer.rect(xPos, yPos, thisWidth, keyHeight);
                shapeRenderer.end();

                // draw border
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(Color.BLACK);
                shapeRenderer.rect(xPos, yPos, thisWidth, keyHeight);
                shapeRenderer.end();

                // draw label
                batch.begin();
                float scale = keyLabel.length() > 1 ? 0.7f : 1f;
                font.getData().setScale(scale);
                layout.setText(font, keyLabel);
                float textX = xPos + (thisWidth - layout.width)/2f;
                float textY = yPos + (keyHeight + layout.height)/2f;
                font.setColor(Color.BLACK);
                font.draw(batch, layout, textX, textY);
                font.getData().setScale(1f);
                batch.end();

                xPos += thisWidth + spacing;

                // skip extra letters for multi-letter keys
                if (isEnter) i += 4;
                if (isDel) i += 2;
            }
        }
    }

    public void reset() {
        for (char c = 'A'; c <= 'Z'; c++) {
            keyStates.put(c, TileState.EMPTY);
        }
    }
}
