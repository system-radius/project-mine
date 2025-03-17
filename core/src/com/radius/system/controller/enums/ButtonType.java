package com.radius.system.controller.enums;

public enum ButtonType {
    CONTINUE(0),
    RESTART(1),
    PAUSE(2),
    PROCEED(3);

    public final int id;

    ButtonType(int id) {
        this.id = id;
    }
}
