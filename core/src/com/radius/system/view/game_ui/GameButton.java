package com.radius.system.view.game_ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.radius.system.controller.ButtonPressListener;
import com.radius.system.controller.enums.ButtonType;
import com.radius.system.controller.parameters.ButtonPressEvent;

import java.util.ArrayList;
import java.util.List;

public class GameButton extends Image  {

    private final List<ButtonPressListener> buttonPressListeners = new ArrayList<>();

    private final ButtonPressEvent event;

    public GameButton(ButtonType buttonType, float x, float y, float width, float height) {
        setX(x); setY(y); setWidth(width); setHeight(height);

        event = new ButtonPressEvent();
        event.buttonType = buttonType;

        this.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                FireButtonEvent();
            }
        });
    }

    public GameButton(TextureRegion texture, ButtonType buttonType, float x, float y, float width, float height, float alpha) {
        super(texture);
        setX(x); setY(y); setWidth(width); setHeight(height);
        getColor().a = alpha;

        event = new ButtonPressEvent();
        event.buttonType = buttonType;

        this.addListener(new ClickListener(){
           @Override
           public void clicked(InputEvent event, float x, float y) {
               FireButtonEvent();
           }
        });
    }

    public void AddListener(ButtonPressListener listener) {
        if (buttonPressListeners.contains(listener)) return;
        buttonPressListeners.add(listener);
    }

    public void FireButtonEvent() {
        for (ButtonPressListener listener : buttonPressListeners) {
            listener.OnButtonPress(event);
        }
    }

}
