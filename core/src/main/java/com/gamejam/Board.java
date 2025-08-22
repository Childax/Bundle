package com.gamejam;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Board {
    private Tile[][] tiles;
    private int rows = 6;
    private int cols = 5;
    private int currentRow = 0;
    private int currentCol = 0;
    private GameManager manager;

    public Board(GameManager manager) {
        this.manager = manager;
        tiles = new Tile[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                tiles[r][c] = new Tile();
            }
        }
    }

    public void typeLetter(char letter) {
        if (currentCol < cols) {
            tiles[currentRow][currentCol].setLetter(letter);
            currentCol++;
        }
    }

    public void deleteLetter() {
        if (currentCol > 0) {
            currentCol--;
            tiles[currentRow][currentCol].setLetter(' ');
        }
    }

    public void submitGuess() {
        if (currentCol < cols) return; // not full row yet

        StringBuilder guess = new StringBuilder();
        for (int c = 0; c < cols; c++) {
            guess.append(tiles[currentRow][c].getLetter());
        }

        String guessWord = guess.toString().trim();
        TileState[] result = manager.submitGuess(guessWord);
        if (result == null) return;

        for (int c = 0; c < cols; c++) {
            tiles[currentRow][c].setState(result[c]);
        }

        if (!manager.isGameOver()) {
            currentRow++;
            currentCol = 0;
        }
    }

    public Tile getTile(int row, int col) {
        return tiles[row][col];
    }

    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, BitmapFont font) {
        float tileSize = 64f;   // pixels per square
        float gap = 10f;

        float boardWidth = cols * tileSize + (cols - 1) * gap;
        float boardHeight = rows * tileSize + (rows - 1) * gap;

        // center horizontally, keep top of board near vertical center
        float startX = (Gdx.graphics.getWidth() - boardWidth) / 2f;
        int bottomMargin = 50;
        float startY = (Gdx.graphics.getHeight() + boardHeight) / 2f + bottomMargin;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Tile tile = tiles[r][c];

                float x = startX + c * (tileSize + gap);
                float y = startY - r * (tileSize + gap);

                tile.render(batch, shapeRenderer, font, x, y, tileSize);
            }
        }
    }

}
