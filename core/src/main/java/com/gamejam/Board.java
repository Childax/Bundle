package com.gamejam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;

public class Board {
    private Tile[][] tiles;
    private int rows = 6;
    private int cols = 5;
    private int currentRow = 0;
    private int currentCol = 0;
    private GameManager manager;

    // Animation variables
    private float[][] bounceTimers;
    private final float JUMP_DURATION = 0.2f;
    private final float POP_DURATION = 0.1f;

    public Board(GameManager manager) {
        this.manager = manager;
        tiles = new Tile[rows][cols];
        bounceTimers = new float[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                tiles[r][c] = new Tile();
                bounceTimers[r][c] = -1.0f; // -1.0 indicates no active animation
            }
        }
    }

    public void typeLetter(char letter) {
        if (currentCol < cols) {
            tiles[currentRow][currentCol].setLetter(letter);
            bounceTimers[currentRow][currentCol] = 0.0f; // Start the jump animation
            currentCol++;
        }
    }

    public void deleteLetter() {
        if (currentCol > 0) {
            currentCol--;
            tiles[currentRow][currentCol].setLetter(' ');
            bounceTimers[currentRow][currentCol] = -1.0f; // Stop animation if letter is deleted
        }
    }

    /**
     * Submits the current guess to the game manager and updates the board.
     * @param currentRow The current row number.
     * @return The array of TileStates for the guessed word, or null if the guess is invalid.
     */
    public TileState[] submitGuess(int currentRow) {
        if (currentCol < cols) return null; // not a full row yet

        String guessWord = getSubmittedWord();
        TileState[] result = manager.submitGuess(guessWord, currentRow);
        if (result == null) return null;

        for (int c = 0; c < cols; c++) {
            // Apply the result and start the pop animation for each tile
            tiles[currentRow][c].setState(result[c]);
            bounceTimers[currentRow][c] = 0.0f;
        }

        return result;
    }

    /**
     * Gets the word from the current row being submitted.
     * @return The 5-letter guess string.
     */
    public String getSubmittedWord() {
        StringBuilder guess = new StringBuilder();
        for (int c = 0; c < cols; c++) {
            guess.append(tiles[currentRow][c].getLetter());
        }
        return guess.toString().trim();
    }

    /**
     * Moves the board's internal state to the next row, and resets the column for the next guess.
     */
    public void advanceRow() {
        if (currentRow < rows - 1) {
            this.currentRow++;
            this.currentCol = 0;
        }
    }

    /**
     * Loads a solved word and its tile states directly onto the board.
     * @param word The solved word string.
     * @param states The TileState array for that word.
     */
    public void loadSolvedWord(String word, TileState[] states) {
        if (currentRow < rows) {
            for (int c = 0; c < cols; c++) {
                tiles[currentRow][c].setLetter(word.charAt(c));
                tiles[currentRow][c].setState(states[c]);
            }
            currentRow++;
        }
    }

    public Tile getTile(int row, int col) {
        return tiles[row][col];
    }

    /**
     * Renders the entire game board. This method now performs two distinct passes
     * (shapes then text) and assumes the renderers are already in their respective begin() calls.
     *
     * @param batch The SpriteBatch for drawing text.
     * @param shapeRenderer The ShapeRenderer for drawing tile backgrounds and borders.
     * @param font The font to use for the letters.
     * @param alpha The alpha value for fade-in effects.
     */
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, BitmapFont font, float alpha) {
        float tileSize = 64f;   // pixels per square
        float gap = 10f;

        float boardWidth = cols * tileSize + (cols - 1) * gap;
        float boardHeight = rows * tileSize + (rows - 1) * gap;

        // center horizontally, keep top of board near vertical center
        float startX = (Gdx.graphics.getWidth() - boardWidth) / 2f;
        int bottomMargin = 50;
        float startY = (Gdx.graphics.getHeight() + boardHeight) / 2f + bottomMargin;

        // --- FIRST PASS: Render all tile shapes and borders ---
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Tile tile = tiles[r][c];
                float x = startX + c * (tileSize + gap);
                float y = startY - r * (tileSize + gap);

                // Update and render animation if it's active
                float scale = 1.0f;
                if (bounceTimers[r][c] >= 0.0f) {
                    bounceTimers[r][c] += Gdx.graphics.getDeltaTime();
                    float duration = (r == currentRow) ? POP_DURATION : JUMP_DURATION;
                    if (bounceTimers[r][c] < duration) {
                        float progress = bounceTimers[r][c] / duration;
                        if (r == currentRow) {
                            scale = 1.0f + Interpolation.pow2In.apply(progress) * 0.1f;
                        } else {
                            scale = 1.0f + Interpolation.bounceOut.apply(progress) * 0.15f;
                        }
                    } else {
                        // Animation is complete, reset timer
                        bounceTimers[r][c] = -1.0f;
                    }
                }
                // Call the render method that handles shapes
                tile.renderShape(shapeRenderer, x, y, tileSize, scale, alpha);
            }
        }
        shapeRenderer.end();

        // --- SECOND PASS: Render all tile letters ---
        batch.begin();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Tile tile = tiles[r][c];
                float x = startX + c * (tileSize + gap);
                float y = startY - r * (tileSize + gap);
                // Call the render method that handles text
                tile.renderText(batch, font, x, y, tileSize, alpha);
            }
        }
        batch.end();
    }

    public int getCurrentRow() {
        return currentRow;
    }

    public void reset() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                tiles[r][c].setLetter(' ');
                tiles[r][c].setState(TileState.EMPTY);
                bounceTimers[r][c] = -1.0f;
            }
        }
        this.currentRow = 0;
        this.currentCol = 0;
    }
}
