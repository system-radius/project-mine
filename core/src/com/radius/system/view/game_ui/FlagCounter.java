package com.radius.system.view.game_ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.radius.system.utils.Assets;

public class FlagCounter extends Actor {
    private static final TextureRegion[][] SYMBOLS = Assets.LoadTextureRegion(Assets.SYMBOLS_TEXTURE_PATH, Assets.SYMBOLS_TEXTURE_REGION_SIZE, Assets.SYMBOLS_TEXTURE_REGION_SIZE);
    private TextureRegion hundreds, tens, ones;
    private int remainingFlags;

    public FlagCounter(float x, float y, float width, float height) {
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
        DeriveValue();
    }

    private void DeriveValue() {
        int hundredsIndex = (remainingFlags % 1000) / 100;
        int tensIndex = (remainingFlags % 100) / 10;
        int onesIndex = (remainingFlags % 100) % 10;

        hundreds = SYMBOLS[0][hundredsIndex];
        tens = SYMBOLS[0][tensIndex];
        ones = SYMBOLS[0][onesIndex];
    }

    public void SetRemainingFlags(int flags) {
        this.remainingFlags = flags;
    }

    public void UpdateFlagCount(int increase) {
        remainingFlags += increase;
    }

    public int GetRemainingFlags() {
        return remainingFlags;
    }

    @Override
    public void act(float delta) {
        DeriveValue();
    }

    @Override
    public void draw(Batch batch, float alpha) {
        DrawItem(batch, hundreds, 0);
        DrawItem(batch, tens, 1);
        DrawItem(batch, ones, 2);
    }

    private void DrawItem(Batch batch, TextureRegion texture, int index) {
        batch.draw(texture, getX() + (getWidth() * index) / 1.5f, getY(), getWidth(), getHeight());
    }
}
