package com.radius.system.view.game_ui.score;

public class ScoreEntry implements Comparable<ScoreEntry> {

    private String name;
    private float time;

    public ScoreEntry(String name, float time) {
        this.name = name;
        this.time = time;
    }

    public void SetName(String name) {
        this.name = name;
    }

    public void SetTime(float time) {
        this.time = time;
    }

    protected String DeriveTime(float time) {
        int minutes = (int)time / 60;
        int seconds = (int)time % 60;
        int millis = (int)(time * 1000) % 1000;

        String tensMinutes = String.valueOf(minutes / 10);
        String onesMinutes = String.valueOf(minutes % 10);

        String tensSeconds = String.valueOf(seconds / 10);
        String onesSeconds = String.valueOf(seconds % 10);

        String millisValues = String.valueOf(millis);

        return tensMinutes + onesMinutes + ":" + tensSeconds + onesSeconds + "." + millisValues;
    }

    public String GetName() {
        return name;
    }

    public float GetTime() {
        return time;
    }

    @Override
    public int compareTo(ScoreEntry that) {
        return Float.compare(this.time, that.time);
    }
}
