package com.radius.system.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.radius.system.utils.FontUtils;
import com.radius.system.view.GameScreen;

import java.util.Random;

public class MineField implements Disposable {

    private static final int UNREVEALED_CELL = 9;
    private static final int FALSE_FLAGGED_CELL = 10;
    private static final int RIGGED_CELL = 11;
    private static final int FLAGGED_CELL = 12;
    private static final int TRIGGERED_CELL = 13;

    public static final Random randomizer = new Random(System.currentTimeMillis());
    private final BitmapFont font;

    private final int width;
    private final int height;
    private final int mineCount;

    private final Cell[][] cells;

    private boolean gameOver = false;
    private boolean fromSave = false;

    private final Preferences prefs;

    public MineField(int width, int height, int mineCount, boolean bypassLoad) {
        this.prefs = Gdx.app.getPreferences(GameScreen.APP_NAME);
        this.width = width;
        this.height = height;
        this.mineCount = mineCount;
        font = FontUtils.GetFont((int) GameScreen.SCALE / 2, Color.WHITE, 0, Color.BLACK);
        cells = new Cell[width][height];
        for (int i = 0; i < this.width; i++) {
            for (int j = 0; j < this.height; j++) {
                cells[i][j] = new Cell(i, j, font);
            }
        }

        if (!bypassLoad) {
            Load();
        }
    }

    public void Populate(int eX, int eY, int mines) {
        gameOver = false;
        while (mines > 0) {
            mines--;
            boolean minePlaced = false;
            do {
                int x = randomizer.nextInt(width);
                int y = randomizer.nextInt(height);

                if (cells[x][y].IsMine() || IsExcluded(eX, eY, x, y)) continue;

                cells[x][y].Rig();
                minePlaced = true;
            } while (!minePlaced);
        }
    }

    private boolean IsExcluded(int eX, int eY, int x, int y) {
        boolean withinX = Math.abs(eX - x) <= 1;
        boolean withinY = Math.abs(eY - y) <= 1;

        return withinX && withinY;
    }

    private int CountSurroundingMines(int x, int y) {
        int value = 0;
        for (int i = -1; i <= 1; i++) {
            int checkX = x + i;
            if (checkX < 0 || checkX >= width) continue;
            for (int j = -1; j <= 1; j++) {
                int checkY = y + j;
                if (checkY < 0 || checkY >= height) continue;
                value += cells[checkX][checkY].IsMine() ? 1 : 0;
            }
        }

        return value;
    }

    private void HighlightCells(int x, int y) {
        for (int i = -1; i <= 1; i++) {
            int checkX = x + i;
            if (checkX < 0 || checkX >= width) continue;
            for (int j = -1; j <= 1; j++) {
                int checkY = y + j;
                if (checkY < 0 || checkY >= height) continue;

                if (!cells[checkX][checkY].IsRevealed() && !cells[checkX][checkY].IsFlagged()) {
                    cells[checkX][checkY].SetHighlightStatus(true);
                }
            }
        }
    }

    public void Unhighlight() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y].SetHighlightStatus(false);
            }
        }
    }

    public void ChordCell(int x, int y) {
        int value = cells[x][y].GetValue();
        int flags = 0;
        for (int i = -1; i <= 1; i++) {
            int checkX = x + i;
            if (checkX < 0 || checkX >= width) continue;
            for (int j = -1; j <= 1; j++) {
                int checkY = y + j;
                if (checkY < 0 || checkY >= height) continue;
                flags += cells[checkX][checkY].IsFlagged() ? 1 : 0;
            }
        }

        if (flags != value) {
            HighlightCells(x, y);
            Unhighlight();
            return;
        }

        for (int i = -1; i <= 1; i++) {
            int checkX = x + i;
            if (checkX < 0 || checkX >= width) continue;
            for (int j = -1; j <= 1; j++) {
                int checkY = y + j;
                if (checkY < 0 || checkY >= height) continue;

                if (!cells[checkX][checkY].IsRevealed()) {
                    RevealCell(checkX, checkY, 0, false);
                }
            }
        }
    }

    public void RevealCell(int x, int y, int stack, boolean shiftMine) {
        if (cells[x][y].IsFlagged() || stack > 99) return;
        if (cells[x][y].IsMine()) {
            gameOver = true;
            cells[x][y].Trigger();
            return;
        }

        if (cells[x][y].IsRevealed()) {
            ChordCell(x, y);
            return;
        }

        if (shiftMine) {
            Populate(x, y, mineCount);
        }

        int value = CountSurroundingMines(x, y);
        cells[x][y].SetValue(value);
        if (value > 0) return;
        for (int i = -1; i <= 1; i++) {
            int checkX = x + i;
            if (checkX < 0 || checkX >= width) continue;
            for (int j = -1; j <= 1; j++) {
                int checkY = y + j;
                if (checkY < 0 || checkY >= height || (checkX == x && checkY == y)) continue;
                if (cells[checkX][checkY].IsRevealed()) continue;
                RevealCell(checkX, checkY, stack + 1, false);
            }
        }
    }

    public void RevealAllMines() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (cells[x][y].IsMine()) {
                    if (cells[x][y].IsFlagged()) continue;
                    cells[x][y].RevealMine();
                } else if (cells[x][y].IsFlagged()) {
                    cells[x][y].FalseFlag();
                }
            }
        }
    }

    public int FlagCell(int x, int y) {
        return cells[x][y].SetFlagStatus();
    }

    public boolean GetCellState(int x, int y) {
        return cells[x][y].IsFlagged();
    }

    public boolean IsRevealed(int x, int y) {
        return cells[x][y].IsRevealed();
    }

    public boolean IsGameOver() {
        return gameOver;
    }

    public void DisableFromSave() {
        fromSave = false;
    }

    public boolean BoardCleared() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (!cells[x][y].IsMine() && !cells[x][y].IsRevealed()) return false;
            }
        }

        gameOver = true;
        return !fromSave;
    }

    public void Update(float delta) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y].Update(delta);
            }
        }
    }

    public void Draw(Batch batch) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y].Draw(batch);
            }
        }
    }

    public void DrawDebug(ShapeRenderer renderer) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                renderer.setColor(cells[x][y].IsMine() ? Color.RED: cells[x][y].IsRevealed() ? Color.BLUE : Color.GREEN);
                //renderer.rect(x * GameScreen.SCALE, y * GameScreen.SCALE, GameScreen.SCALE, GameScreen.SCALE);
            }
        }
    }

    public void Save() {
        System.out.println("Saving stuff!");
        // Things that need saving:
        // - board generation state
        // - cell revelation/click state

        // When saving the board state, save as numbers? 0 to 8 as click state,
        // 10 is unrevealed mine, 9 for unrevealed cell, 11 for flagged mine.
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (cells[x][y].IsFlagged()) {
                    sb.append(cells[x][y].IsMine() ? FLAGGED_CELL : FALSE_FLAGGED_CELL);
                }
                else if (cells[x][y].IsMine()) {
                    sb.append(cells[x][y].IsTriggered() ? TRIGGERED_CELL : RIGGED_CELL);
                }
                else if (!cells[x][y].IsRevealed()) {
                    sb.append(UNREVEALED_CELL);
                }
                else {
                    sb.append(cells[x][y].GetValue());
                }

                if (x + 1 < width) {
                    sb.append(",");
                }
            }
            if (y + 1 < height) {
                sb.append("\n");
            }
        }

        // Save the board state as string in the preferences.
        System.out.println("Board state:\n" + sb);

        prefs.putString("state", sb.toString());
        prefs.flush();
    }

    public void Load() {
        System.out.println("Loading!");
        String state = prefs.getString("state");
        if (state == null || state.isEmpty()) {
            System.out.println("Unable to load state: state is empty!");
            return;
        }

        String[] rows = state.split("\n");
        int dimY = rows.length;
        int dimX = rows[0].split(",").length;

        if (dimX != width || dimY != height) {
            System.out.println("Unable to load state: invalid dimensions! Expected: (" + width + ", " + height + "), get: (" + dimX + ", " + dimY + ")");
            return;
        }

        boolean revealAll = false;
        int triggerX = -1;
        int triggerY = -1;
        for (int y = 0; y < dimY; y++) {
            String[] cols = rows[y].split(",");
            for (int x = 0; x < dimX; x++) {
                int cellState = Integer.parseInt(cols[x]);

                if (cellState == FLAGGED_CELL || cellState == FALSE_FLAGGED_CELL) {
                    cells[x][y].SetFlagStatus();
                }

                if (cellState >= RIGGED_CELL) {
                    cells[x][y].Rig();
                    if (cellState == TRIGGERED_CELL && !revealAll) {
                        revealAll = true;
                        triggerX = x;
                        triggerY = y;
                    }
                } else if (cellState < UNREVEALED_CELL) {
                    cells[x][y].SetValue(cellState);
                }
            }
        }

        if (revealAll) {
            cells[triggerX][triggerY].Trigger();
            RevealAllMines();
            gameOver = true;
        }

        fromSave = true;
    }

    @Override
    public void dispose() {

        Save();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y].dispose();
            }
        }
    }
}
