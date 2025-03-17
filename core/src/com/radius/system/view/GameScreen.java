package com.radius.system.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.radius.system.controller.ButtonPressListener;
import com.radius.system.controller.LevelChangeListener;
import com.radius.system.controller.enums.ButtonType;
import com.radius.system.controller.enums.MineFieldLevel;
import com.radius.system.controller.parameters.ButtonPressEvent;
import com.radius.system.model.MineField;
import com.radius.system.utils.FontUtils;

public class GameScreen extends AbstractScreen implements ButtonPressListener, LevelChangeListener {

    private static final float LONG_PRESS_THRESHOLD = 0.3f;
    private static final float MIN_ZOOM = 0.5f;
    private static final float MAX_ZOOM = 2f;

    public static final String APP_NAME = "project-mine";

    public static final float SCALE = 64f;
    private static final float VIEWPORT_WIDTH = 9f;
    private static final float VIEWPORT_HEIGHT = 16f;
    private static final float EFFECTIVE_VIEWPORT_DIVIDER = 2f;

    private final Preferences prefs;

    private final Vector3 touchVector = new Vector3();
    private final Vector3 gameTouchVector = new Vector3();
    private final Vector3 prevDragVector = new Vector3();
    private final Vector3 dragVector = new Vector3();
    private final Vector3 gameDragVector = new Vector3();

    private Camera dynamicCamera;
    private Viewport dynamicViewport;
    private Camera staticCamera;
    private Viewport staticViewport;
    private final GameStage gameStage;

    private boolean firstTouch = true;
    private boolean draggingTouch;
    private boolean restarting;
    private boolean gameOver;
    private float touchDownTimer;

    private MineFieldLevel level;

    private int flags = 99;
    private float zoom = 0.5f;
    private float worldWidth = 16;
    private float worldHeight = 33;
    private MineField mineField;

    public GameScreen() {
        prefs = Gdx.app.getPreferences(APP_NAME);
        String levelString = prefs.getString("level", MineFieldLevel.Expert.label);
        level = MineFieldLevel.FromString(levelString);
        InitializeCamera();

        debug = true;
        gameStage = new GameStage(staticViewport);
        gameStage.AddButtonPressListener(this);
        gameStage.AddLevelChangeListener(this);

        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(gameStage);
        inputMultiplexer.addProcessor(new GestureDetector(20, 0.4f, LONG_PRESS_THRESHOLD, Integer.MAX_VALUE, this));
        Gdx.input.setInputProcessor(inputMultiplexer);

        Restart(false);
    }

    private void InitializeCamera() {
        zoom = prefs.getFloat("zoom", 0.5f);
        dynamicCamera = new OrthographicCamera(SCALE, SCALE);
        dynamicCamera.viewportWidth = (SCALE * VIEWPORT_WIDTH);
        dynamicCamera.viewportHeight = (SCALE * VIEWPORT_HEIGHT);
        //System.out.println("Viewport width: " + dynamicCamera.viewportWidth + ", viewport height: " + dynamicCamera.viewportHeight);
        //dynamicViewport = new ExtendViewport((SCALE * VIEWPORT_WIDTH) / 1.5f, (SCALE * VIEWPORT_HEIGHT) / 1.5f, dynamicCamera);
        dynamicViewport = new ScalingViewport(Scaling.fill, dynamicCamera.viewportWidth, dynamicCamera.viewportHeight, dynamicCamera);
        //dynamicViewport = new FitViewport(dynamicCamera.viewportWidth, dynamicCamera.viewportHeight, dynamicCamera);
        dynamicViewport.apply();
        dynamicCamera.position.x = prefs.getFloat("camX", 0);
        dynamicCamera.position.y = prefs.getFloat("camY", 0);
        dynamicCamera.update();

        staticCamera = new OrthographicCamera();
        staticViewport = new ExtendViewport((SCALE * VIEWPORT_WIDTH) / 2, (SCALE * VIEWPORT_HEIGHT) / 2, staticCamera);
        staticCamera.update();
    }

    private void AdjustViewport() {
        float viewportWidth = (SCALE * VIEWPORT_WIDTH) / zoom;
        float viewportHeight = (SCALE * VIEWPORT_HEIGHT) / zoom;

        dynamicViewport.setWorldWidth(viewportWidth);
        dynamicViewport.setWorldHeight(viewportHeight);
        dynamicViewport.apply();
        ClampCamera(dynamicCamera.position.x, dynamicCamera.position.y);
    }

    private void Restart(boolean bypassLoad) {
        gameOver = false;
        worldWidth = level.sizeX;
        worldHeight = level.sizeY;
        flags = level.mineCount;
        //zoom = Math.max(zoom, level.minZoom);
        zoom = 0.5f;
        AdjustViewport();

        mineField = new MineField((int) worldWidth, (int) worldHeight, flags, bypassLoad);
        //mineField.Populate(flags);
        gameStage.Start(level, bypassLoad);

        firstTouch = gameStage.GetTime() == 0;
        restarting = false;
        draggingTouch = false;
    }

    private void ClampCamera(float x, float y) {
        boolean higherWorldWidth = (worldWidth + 2) * SCALE >= dynamicCamera.viewportWidth || (worldWidth + 2) * SCALE >= Gdx.graphics.getWidth();
        //boolean higherWorldWidth = true;
        boolean higherWorldHeight = (worldHeight + 1) * SCALE >= dynamicCamera.viewportHeight || (worldHeight + 1) * SCALE >= Gdx.graphics.getHeight();

        float effectiveViewportWidth = higherWorldWidth ? dynamicCamera.viewportWidth / EFFECTIVE_VIEWPORT_DIVIDER : (worldWidth * SCALE) / EFFECTIVE_VIEWPORT_DIVIDER;
        float effectiveViewportHeight = higherWorldHeight ? dynamicCamera.viewportHeight / EFFECTIVE_VIEWPORT_DIVIDER : (worldHeight * SCALE) / EFFECTIVE_VIEWPORT_DIVIDER;
                //Math.min(worldHeight, dynamicCamera.viewportHeight) / EFFECTIVE_VIEWPORT_DIVIDER;
        float borderSizeX = higherWorldWidth ? 2 : 0;
        float borderSizeY = higherWorldHeight ? 2 : 0;
        //float borderSizeY = 2;
        dynamicCamera.position.x = MathUtils.clamp(x, effectiveViewportWidth - (borderSizeX * SCALE), (worldWidth * SCALE) - effectiveViewportWidth + (borderSizeX * SCALE));
        dynamicCamera.position.y = MathUtils.clamp(y, effectiveViewportHeight - (borderSizeY * SCALE), (worldHeight * SCALE) - effectiveViewportHeight + (borderSizeY * SCALE));
        dynamicCamera.update();
    }

    private void TriggerLongPress() {
        if (gameOver) return;
        mineField.DisableFromSave();

        Gdx.input.vibrate(50, false);
        if (mineField.IsRevealed((int)gameTouchVector.x, (int)gameTouchVector.y)) {
            mineField.ChordCell((int)gameTouchVector.x, (int)gameTouchVector.y);
            return;
        } else {
            mineField.RevealCell((int)gameTouchVector.x, (int)gameTouchVector.y, 0, firstTouch);
        }/**/

        if (firstTouch) {
            firstTouch = false;
            gameStage.StartTime();
        }
    }

    private void TriggerTap() {
        if (gameOver) return;
        mineField.DisableFromSave();

        if (firstTouch || mineField.IsRevealed((int)gameTouchVector.x, (int)gameTouchVector.y)) {
            TriggerLongPress();
            return;
        }/**/

        if (gameStage.GetRemainingFlags() <= 0 && !mineField.GetCellState((int)gameTouchVector.x, (int)gameTouchVector.y)) return;

        int cellState = mineField.FlagCell((int)gameTouchVector.x, (int)gameTouchVector.y);
        gameStage.UpdateFlagCount(cellState);
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        touchVector.x = x;
        touchVector.y = y;
        touchVector.set(dynamicCamera.unproject(touchVector, dynamicViewport.getScreenX(), dynamicViewport.getScreenY(), dynamicViewport.getScreenWidth(), dynamicViewport.getScreenHeight()));

        gameTouchVector.x = (float)Math.floor(touchVector.x / GameScreen.SCALE);
        gameTouchVector.y = (float)Math.floor(touchVector.y / GameScreen.SCALE);

        if (gameTouchVector.x < 0 || gameTouchVector.y < 0 || gameTouchVector.x >= worldWidth || gameTouchVector.y >= worldHeight) {
            return false;
        }

        if (!draggingTouch && touchDownTimer <= LONG_PRESS_THRESHOLD) {
            TriggerTap();
        }

        //mineField.Unhighlight();
        draggingTouch = false;
        touchDownTimer = 0;
        return !gameOver;
    }

    @Override
    public boolean longPress(float x, float y) {
        touchVector.x = x;
        touchVector.y = y;
        touchVector.set(dynamicCamera.unproject(touchVector, dynamicViewport.getScreenX(), dynamicViewport.getScreenY(), dynamicViewport.getScreenWidth(), dynamicViewport.getScreenHeight()));

        gameTouchVector.x = (float)Math.floor(touchVector.x / GameScreen.SCALE);
        gameTouchVector.y = (float)Math.floor(touchVector.y / GameScreen.SCALE);

        if (gameTouchVector.x < 0 || gameTouchVector.y < 0 || gameTouchVector.x >= worldWidth || gameTouchVector.y >= worldHeight) {
            return false;
        }

        TriggerLongPress();
        return !gameOver;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        dragVector.x = x;
        dragVector.y = y;
        dragVector.set(dynamicCamera.unproject(dragVector, dynamicViewport.getScreenX(), dynamicViewport.getScreenY(), dynamicViewport.getScreenWidth(), dynamicViewport.getScreenHeight()));

        if (draggingTouch) {
            dynamicCamera.position.x -= Math.round((dragVector.x) - (prevDragVector.x));
            dynamicCamera.position.y -= Math.round((dragVector.y) - (prevDragVector.y));
            ClampCamera(dynamicCamera.position.x, dynamicCamera.position.y);
            //prevDragVector.set(dragVector);
        }

        gameDragVector.x = (float)Math.floor(dragVector.x / GameScreen.SCALE);
        gameDragVector.y = (float)Math.floor(dragVector.y / GameScreen.SCALE);

        //System.out.println(gameDragVector);

        if (!draggingTouch && Math.abs(gameTouchVector.dst(gameDragVector)) >= 1f) {
            draggingTouch = true;
            prevDragVector.set(dragVector);
            //ClampCamera(gameDragVector.x * SCALE, gameDragVector.y * SCALE);
            //ClampCamera(gameDragVector.x, gameDragVector.y);
            return true;
        }

        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        draggingTouch = false;
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        float initialVectorDistance = (float) Math.sqrt(Math.pow(initialPointer1.x - initialPointer2.x, 2) + Math.pow(initialPointer1.y - initialPointer2.y, 2));
        float vectorDistance = (float) Math.sqrt(Math.pow(pointer1.x - pointer2.x, 2) + Math.pow(pointer1.y - pointer2.y, 2));
        float distanceDelta = vectorDistance - initialVectorDistance;
        //System.out.println("distanceDelta: " + distanceDelta + ", diff: " + (distanceDelta - prevDistanceDelta));

        zoom = MathUtils.clamp(zoom + (distanceDelta) * 0.0001f, MIN_ZOOM, MAX_ZOOM);
        AdjustViewport();
        return false;
    }

    /*
    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {

        touchVector.x = x;
        touchVector.y = y;
        touchVector.set(dynamicCamera.unproject(touchVector, dynamicViewport.getScreenX(), dynamicViewport.getScreenY(), dynamicViewport.getScreenWidth(), dynamicViewport.getScreenHeight()));

        gameTouchVector.x = (float)Math.floor(touchVector.x / GameScreen.SCALE);
        gameTouchVector.y = (float)Math.floor(touchVector.y / GameScreen.SCALE);

        if (gameTouchVector.x < 0 || gameTouchVector.y < 0 || gameTouchVector.x >= WORLD_WIDTH || gameTouchVector.y >= WORLD_HEIGHT) {
            return false;
        }

        touchingDown = true;
        return !gameOver;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        touchVector.x = x;
        touchVector.y = y;
        touchVector.set(dynamicCamera.unproject(touchVector, dynamicViewport.getScreenX(), dynamicViewport.getScreenY(), dynamicViewport.getScreenWidth(), dynamicViewport.getScreenHeight()));

        gameTouchVector.x = (float)Math.floor(touchVector.x / GameScreen.SCALE);
        gameTouchVector.y = (float)Math.floor(touchVector.y / GameScreen.SCALE);

        if (gameTouchVector.x < 0 || gameTouchVector.y < 0 || gameTouchVector.x >= WORLD_WIDTH || gameTouchVector.y >= WORLD_HEIGHT) {
            return false;
        }

        if (!draggingTouch && touchDownTimer <= LONG_PRESS_THRESHOLD) {
            System.out.println("Touch up triggered!");
            TriggerTap();
        }

        longPressed = draggingTouch = touchingDown = false;
        System.out.println("Reset!");
        touchDownTimer = 0;
        return !gameOver;
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        if (!touchingDown) return false;
        dragVector.x = x;
        dragVector.y = y;
        dragVector.set(dynamicCamera.unproject(dragVector, dynamicViewport.getScreenX(), dynamicViewport.getScreenY(), dynamicViewport.getScreenWidth(), dynamicViewport.getScreenHeight()));

        if (draggingTouch) {
            dynamicCamera.position.x -= Math.round((dragVector.x) - (prevDragVector.x));
            dynamicCamera.position.y -= Math.round((dragVector.y) - (prevDragVector.y));
            ClampCamera(dynamicCamera.position.x, dynamicCamera.position.y);
            //prevDragVector.set(dragVector);
        }

        gameDragVector.x = (float)Math.floor(dragVector.x / GameScreen.SCALE);
        gameDragVector.y = (float)Math.floor(dragVector.y / GameScreen.SCALE);

        //System.out.println(gameDragVector);

        if (!draggingTouch && Math.abs(gameTouchVector.dst(gameDragVector)) >= 0.0001f) {
            System.out.println("Dragging touch!");
            draggingTouch = true;
            prevDragVector.set(dragVector);
            //ClampCamera(gameDragVector.x * SCALE, gameDragVector.y * SCALE);
            //ClampCamera(gameDragVector.x, gameDragVector.y);
            return true;
        }

        return false;
    }/**/

    @Override
    public void resize(int width, int height) {
        dynamicViewport.update(width, height);
        //ClampCamera(worldWidth * SCALE / 2f, worldHeight * SCALE / 2f);
        ClampCamera(dynamicCamera.position.x, dynamicCamera.position.y);
        dynamicCamera.update();
        gameStage.Resize();
    }

    @Override
    public void pause() {
        draggingTouch = false;
        prefs.putString("level", level.label);
        prefs.putFloat("camX", dynamicCamera.position.x);
        prefs.putFloat("camY", dynamicCamera.position.y);
        prefs.putFloat("zoom", zoom);
        prefs.flush();
        mineField.Save();
        gameStage.Save();
    }

    @Override
    public void Update(float delta) {

        //System.out.println("Status -> touchDown: " + touchingDown + ", dragTouch: " + draggingTouch);
        /*
        if (touchingDown && !draggingTouch && !longPressed) {
            touchDownTimer += delta;
            if (touchDownTimer > LONG_PRESS_THRESHOLD) {
                TriggerLongPress();
            }
        }/**/

        mineField.Update(delta);

        if (mineField.BoardCleared() && !gameOver) {
            gameOver = true;
            gameStage.SetGameOver(true);
        }

        if (mineField.IsGameOver() && !gameOver) {
            gameOver = true;
            mineField.RevealAllMines();
            gameStage.SetGameOver(false);
        }

        gameStage.act(delta);
    }

    @Override
    public void Draw(SpriteBatch spriteBatch) {
        spriteBatch.begin();
        spriteBatch.setProjectionMatrix(dynamicCamera.projection);
        spriteBatch.setTransformMatrix(dynamicCamera.view);
        dynamicViewport.apply();
        if (!gameStage.IsPaused()) {
            mineField.Draw(spriteBatch);
        }
        spriteBatch.end();

        gameStage.draw();
    }

    @Override
    public void DrawDebug(ShapeRenderer renderer) {
        renderer.setProjectionMatrix(dynamicCamera.projection);
        renderer.setTransformMatrix(dynamicCamera.view);
        dynamicViewport.apply();
        renderer.begin(ShapeRenderer.ShapeType.Line);

        renderer.setColor(Color.GREEN);
        if (mineField != null)
            mineField.DrawDebug(renderer);

        renderer.end();
    }

    @Override
    public void dispose() {
        super.dispose();
        gameStage.dispose();
        mineField.dispose();
        FontUtils.Dispose();
    }

    @Override
    public void OnButtonPress(ButtonPressEvent event) {
        if (event.buttonType.equals(ButtonType.RESTART)) {
            Restart(true);
        }
    }

    @Override
    public void OnLevelChange(MineFieldLevel level) {
        this.level = level;
    }
}
