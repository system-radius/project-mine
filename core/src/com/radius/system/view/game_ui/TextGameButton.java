package com.radius.system.view.game_ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Align;
import com.radius.system.controller.enums.ButtonType;
import com.radius.system.utils.FontUtils;
import com.radius.system.view.GameScreen;

public class TextGameButton extends GameButton {

    private static final BitmapFont TEXT_RENDERER = FontUtils.GetFont((int)(GameScreen.SCALE / 4f), Color.WHITE, 1, Color.BLACK);

    private String text = "waddup";

    private Color color = Color.WHITE;

    public TextGameButton(TextureRegion texture, ButtonType buttonType, float x, float y, float width, float height) {
        super(texture, buttonType, x, y, width, height, 1);
    }

    public void SetText(String text) {
        this.text = text;
    }

    @Override
    public void draw(Batch batch, float alpha) {
        super.draw(batch, alpha);
        TEXT_RENDERER.draw(batch, text, getX(), getY() + getHeight() / 2 + GameScreen.SCALE / 16f, getWidth(), Align.center, false);
    }
}
