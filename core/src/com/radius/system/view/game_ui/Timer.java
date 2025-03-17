package com.radius.system.view.game_ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.radius.system.utils.Assets;

public class Timer extends Actor {

    private static int tensMinutesValue, onesMinutesValue, tensSecondsValue, onesSecondsValue;
    private static final TextureRegion[][] SYMBOLS = Assets.LoadTextureRegion(Assets.SYMBOLS_TEXTURE_PATH, Assets.SYMBOLS_TEXTURE_REGION_SIZE, Assets.SYMBOLS_TEXTURE_REGION_SIZE);
    private final TextureRegion colon;
    private TextureRegion tensMinutes, onesMinutes, tensSeconds, onesSeconds;
    private float totalTime;
    private boolean started = false;
    private boolean running;
    private boolean bypassOvertime = false;
    private boolean overtime = false;
    private boolean countDown = true;

    public Timer(float x, float y, float width, float height) {
        this.colon = SYMBOLS[5][0];
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
        SetTime(299);
        StartTimer();
    }

    private void DeriveTime(float time) {
        int minutes = (int)time / 60;
        int seconds = (int)time % 60;

        tensMinutesValue = minutes / 10;
        onesMinutesValue = minutes % 10;

        tensMinutes = SYMBOLS[0][tensMinutesValue];
        onesMinutes = SYMBOLS[0][onesMinutesValue];

        tensSecondsValue = seconds / 10;
        onesSecondsValue = seconds % 10;

        tensSeconds = SYMBOLS[0][tensSecondsValue];
        onesSeconds = SYMBOLS[0][onesSecondsValue];
    }

    public float GetTime() {
        return totalTime;
    }

    public void SetTime(float time) {
        totalTime = time;
        if (time < 0) {
            bypassOvertime = true;
            totalTime = 0;
            return;
        }
        if (time == 0) {
            countDown = false;
        }
        bypassOvertime = overtime = false;
        DeriveTime(time);
    }

    @Override
    public void act(float delta) {
        if ((!running || !started) || bypassOvertime) {
            return;
        }

        totalTime += countDown ? -delta : delta;
        if (totalTime < 0) {
            //FireOverTimeEvent();
            System.out.println("Overtime detected! started: " + started + ", running: " + running);
            StopTimer();
            overtime = true;
            return;
        }

        DeriveTime(totalTime);
    }

    @Override
    public void draw(Batch batch, float alpha) {
        DrawItem(batch, tensMinutes, 0);
        DrawItem(batch, onesMinutes, 1);
        DrawItem(batch, colon, 2);
        DrawItem(batch, tensSeconds, 3);
        DrawItem(batch, onesSeconds, 4);
    }

    private void DrawItem(Batch batch, TextureRegion texture, int index) {
        batch.draw(texture, getX() + (getWidth() * index) / 1.5f, getY(), getWidth(), getHeight());
    }

    public void StopTimer() {
        started = false;
        SetRunningStatus(started);
    }

    public void StartTimer() {
        started = true;
        SetRunningStatus(started);
    }

    public void SetRunningStatus(boolean running) {
        this.running = running;
    }

    public boolean IsOvertime() {
        return overtime;
    }

    public static void LogTimeStamped(String message) {
        System.out.println("[" + tensMinutesValue + onesMinutesValue + ":" + tensSecondsValue + onesSecondsValue + "] " + message);
    }

}
