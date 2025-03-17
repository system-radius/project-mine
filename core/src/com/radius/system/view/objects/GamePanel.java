package com.radius.system.view.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.radius.system.utils.Assets;

public abstract class GamePanel extends Group {

    protected Texture background;
    private Color bgColor = Color.CLEAR;
    private float alpha = 0.5f;

    protected boolean renderBackground = true;

    public GamePanel(float x, float y, float width, float height, String backgroundTexturePath) {
        setX(x); setY(y); setWidth(width); setHeight(height);
        background = Assets.LoadTexture(backgroundTexturePath);
    }

    public GamePanel(float x, float y, float width, float height) {
        this(x, y, width, height, Assets.WHITE_SQUARE);
    }

    public void SetAlpha(float alpha) {
        this.alpha = alpha;
    }

    public void SetBGColor(Color color) {
        this.bgColor = color;
    }

    public abstract void Resize();

    @Override
    public final void draw(Batch batch, float alpha) {
        batch.setColor(bgColor.r, bgColor.g, bgColor.b, this.alpha);
        if (renderBackground) {
            batch.draw(background, getX(), getY(), getWidth(), getHeight());
        }
        batch.setColor(Color.WHITE);
        DrawOverride(batch, alpha);
    }

    protected void DrawOverride(Batch batch, float alpha) {
        super.draw(batch, alpha);
    }

}
