package com.gamejam;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter implements InputProcessor {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private BitmapFont keyboardFont;

    private Board board;
    private Keyboard keyboard;
    private GameManager gameManager;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = FontLoader.loadFont("fonts/HelveticaNeue-BlackCond.otf", 48);
        keyboardFont = FontLoader.loadFont("fonts/HelveticaNeue-BlackCond.otf", 32);

        gameManager = new GameManager();
        board = new Board(gameManager);
        keyboard = new Keyboard();

        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        board.render(batch, shapeRenderer, font);
        keyboard.render(batch, shapeRenderer, keyboardFont, 1024);
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode >= Input.Keys.A && keycode <= Input.Keys.Z) {
            char letter = (char) ('A' + (keycode - Input.Keys.A));
            board.typeLetter(letter);
        } else if (keycode == Input.Keys.ENTER) {
            TileState[] result = board.submitGuess();
            if (result != null) {
                // get the letters of the submitted row
                for (int c = 0; c < 5; c++) {
                    char letter = board.getTile(board.getCurrentRow() - 1, c).getLetter(); // last submitted row
                    keyboard.updateKeyState(letter, result[c]);
                }
            }
        } else if (keycode == Input.Keys.BACKSPACE) {
            board.deleteLetter();
        }
        return true;
    }

    // unused InputProcessor methods
    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchDown(int x, int y, int pointer, int button) { return false; }
    @Override public boolean touchUp(int x, int y, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int x, int y, int pointer) { return false; }
    @Override public boolean mouseMoved(int x, int y) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
    @Override public boolean touchCancelled(int x, int y, int z, int button) { return false; }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }
}
