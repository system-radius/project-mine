package com.radius.system.controller.enums;

public enum MineFieldLevel {

    Beginner("Beginner", 9, 9, 10, 0.5f),
    Intermediate("Intermediate", 16, 16, 40, 0.25f),
    Expert("Expert", 16, 30, 99, 0.225f)
    //,Custom("Custom", -1, -1, -1)
    ;

    public final String label;
    public final int sizeX;
    public final int sizeY;
    public final int mineCount;
    public final float minZoom;

    MineFieldLevel(String label, int x, int y, int mineCount, float minZoom) {
        this.label = label;
        this.sizeX = x;
        this.sizeY = y;
        this.mineCount = mineCount;
        this.minZoom = minZoom;
    }

    public static MineFieldLevel FromString(String string) {
        for (MineFieldLevel level : MineFieldLevel.values()) {
            if (level.label.equals(string)) {
                return level;
            }
        }

        return Beginner;
    }
}
