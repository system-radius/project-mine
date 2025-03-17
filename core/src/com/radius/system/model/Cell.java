package com.radius.system.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Disposable;
import com.radius.system.utils.Assets;
import com.radius.system.view.GameScreen;

public class Cell implements Disposable {

    private static final Color[] TILE_COLOR = new Color[] {Color.CLEAR, new Color(0x0202f1ff), new Color(0x367e1aff), new Color(0xec3324ff), new Color(0x01027aff), new Color(0x74180cff), new Color(0x397c76ff), new Color(0x010101ff), new Color(0x808080ff)};
    private static final float HIGHLIGHT_LIMIT = 0.15f;

    private final BitmapFont fontRenderer;
    private final Texture base;
    private final Texture revealed;
    private final Texture flag;
    private final Texture bomb;

    public final int x;
    public final int y;

    private Texture activeTexture;

    private boolean rigged;
    private boolean triggered;
    private boolean flagged;
    private boolean falseFlagged;
    private boolean highlighted;
    private boolean showHighlight;
    private int value = -1;

    private float highlightTimer = 0f;

    public Cell(int x, int y, BitmapFont font) {
        this.fontRenderer = font;
        this.x = x; this.y = y;
        base = Assets.LoadTexture(Assets.BASE_TEXTURE_PATH);
        revealed = Assets.LoadTexture(Assets.REVEALED_TEXTURE_PATH);
        flag = Assets.LoadTexture(Assets.FLAG_TEXTURE_PATH);
        bomb = Assets.LoadTexture(Assets.MINE_TEXTURE_PATH);

        activeTexture = base;
    }

    public int GetValue() {
        return value;
    }

    public boolean IsRevealed() {
        return value >= 0;
    }

    public boolean IsMine() {
        return rigged;
    }

    public boolean IsFlagged() {
        return flagged;
    }

    public boolean IsTriggered() {
        return triggered;
    }

    public int SetFlagStatus() {
        if (value >= 0) return 0;
        flagged = !flagged;
        activeTexture = flagged ? flag : base;

        return flagged ? -1 : 1;
    }

    public void SetHighlightStatus(boolean highlight) {
        if (highlighted && !highlight) {
            showHighlight = true;
            highlightTimer = HIGHLIGHT_LIMIT;
        }
        this.highlighted = highlight;
    }

    public void Trigger() {
        triggered = true;
    }

    public void FalseFlag() {
        falseFlagged = true;
    }

    public void Rig() {
        rigged = true;
    }

    public void Defuse() {
        rigged = false;
    }

    public void SetValue(int value) {
        activeTexture = revealed;
        this.value = value;
    }
    public void RevealMine() {
        activeTexture = bomb;
    }

    public void Update(float delta) {
        if (showHighlight) {
            highlightTimer -= delta;
            if (highlightTimer <= 0) {
                showHighlight = false;
            }
        }
    }

    public void Draw(Batch batch) {
        batch.setColor(highlighted || showHighlight ? Color.CYAN : Color.WHITE);

        if (triggered) batch.setColor(Color.RED);
        else if (falseFlagged) batch.setColor(Color.ORANGE);
        batch.draw(activeTexture, x * GameScreen.SCALE, y * GameScreen.SCALE, GameScreen.SCALE, GameScreen.SCALE);

        batch.setColor(Color.WHITE);

        if (value > 0) {
            fontRenderer.setColor(TILE_COLOR[value]);
            fontRenderer.draw(batch, String.valueOf(value), x * GameScreen.SCALE + GameScreen.SCALE / 3, y * GameScreen.SCALE + GameScreen.SCALE - GameScreen.SCALE / 3);
        }
    }

    @Override
    public void dispose() {
    }
}
