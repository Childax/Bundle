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
        "ENTERZXCVBNMDEL"
    };

    private Map<Character, TileState> keyStates = new HashMap<>();

    public Keyboard() {
        for (String row : rows) {
            for (char c : row.toCharArray()) {
                if (Character.isLetter(c)) {
                    keyStates.put(c, TileState.EMPTY);
                }
            }
        }
    }

    private void drawRoundedRect(ShapeRenderer shapeRenderer, float x, float y, float width, float height, float radius) {
        shapeRenderer.rect(x + radius, y, width - 2 * radius, height);
        shapeRenderer.rect(x, y + radius, width, height - 2 * radius);

        shapeRenderer.circle(x + radius, y + radius, radius);
        shapeRenderer.circle(x + width - radius, y + radius, radius);
        shapeRenderer.circle(x + radius, y + height - radius, radius);
        shapeRenderer.circle(x + width - radius, y + height - radius, radius);
    }

    public void updateKeyState(char c, TileState state) {
        c = Character.toUpperCase(c);
        if (!keyStates.containsKey(c)) return;

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
        float cornerRadius = 8f;
        GlyphLayout layout = new GlyphLayout();

        for (int r = 0; r < rows.length; r++) {
            String rowStr = rows[r];

            float rowWidth = 0f;
            for (int i = 0; i < rowStr.length(); i++) {
                boolean isEnter = r == 2 && i == 0;
                boolean isDel   = r == 2 && i == rowStr.length() - 3;
                rowWidth += (isEnter || isDel ? keyWidth*2 + spacing : keyWidth + spacing);
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

                Color keyBackgroundColor;
                if (keyLabel.length() == 1) {
                    TileState state = keyStates.get(keyLabel.charAt(0));
                    if (state == TileState.CORRECT) {
                        keyBackgroundColor = WordleColors.CORRECT;
                    } else if (state == TileState.PRESENT) {
                        keyBackgroundColor = WordleColors.PRESENT;
                    } else if (state == TileState.ABSENT) {
                        keyBackgroundColor = WordleColors.ABSENT;
                    } else {
                        keyBackgroundColor = Color.valueOf("#949799");
                    }
                } else {
                    keyBackgroundColor = Color.valueOf("#949799");
                }

                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(keyBackgroundColor);
                drawRoundedRect(shapeRenderer, xPos, yPos, thisWidth, keyHeight, cornerRadius);
                shapeRenderer.end();

                batch.begin();
                float scale = keyLabel.length() > 1 ? 0.7f : 1f;
                font.getData().setScale(scale);
                layout.setText(font, keyLabel);
                float textX = xPos + (thisWidth - layout.width)/2f;
                float textY = yPos + (keyHeight + layout.height)/2f;
                font.setColor(Color.WHITE); // Set font color to white
                font.draw(batch, layout, textX, textY);
                font.getData().setScale(1f);
                batch.end();

                xPos += thisWidth + spacing;

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
