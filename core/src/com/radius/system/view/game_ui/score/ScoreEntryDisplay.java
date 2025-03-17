package com.radius.system.view.game_ui.score;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.radius.system.utils.FontUtils;
import com.radius.system.view.GameScreen;

public class ScoreEntryDisplay extends ScoreEntry {

    private static final BitmapFont NAME_FONT = FontUtils.GetFont((int) GameScreen.SCALE / 4, Color.WHITE, 1, Color.BLACK);
    private static final BitmapFont TIME_FONT = FontUtils.GetFont((int) GameScreen.SCALE / 5, Color.WHITE, 1, Color.BLACK);
    private static final Label.LabelStyle NAME_STYLE = new Label.LabelStyle();
    private static final Label.LabelStyle TIME_STYLE = new Label.LabelStyle();
    static {
        NAME_STYLE.font = NAME_FONT;
        TIME_STYLE.font = TIME_FONT;
    }

    public final Label nameLabel;
    public final Label timeLabel;

    public ScoreEntryDisplay(String name, float time) {
        super(name, time);
        nameLabel = new Label("", NAME_STYLE);
        timeLabel = new Label("", TIME_STYLE);
        SetName(name);
        SetTime(time);
    }

    @Override
    public void SetName(String name) {
        super.SetName(name);
        nameLabel.setText(name);
    }

    @Override
    public void SetTime(float time) {
        super.SetTime(time);
        timeLabel.setText(DeriveTime(time));
    }
}
