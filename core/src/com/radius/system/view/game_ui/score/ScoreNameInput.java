package com.radius.system.view.game_ui.score;

import com.badlogic.gdx.Input;

public class ScoreNameInput implements Input.TextInputListener {

    @Override
    public void input(String text) {
        System.out.println("Detected input: " + text);
    }

    @Override
    public void canceled() {

    }
}
