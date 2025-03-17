package com.radius.system.view.game_ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.SnapshotArray;
import com.radius.system.utils.Assets;
import com.radius.system.view.objects.GamePanel;

public class HeadsUpDisplay extends GamePanel {

    public HeadsUpDisplay(float x, float y, float width, float height) {
        super(x, y, width, height, Assets.BACKGROUND_TEXTURE_PATH);

    }

    @Override
    public void Resize() {

    }

    @Override
    public void DrawOverride(Batch batch, float alpha) {
        super.DrawOverride(batch, alpha);
    }
}
