package com.radius.system.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.radius.system.controller.ButtonPressListener;
import com.radius.system.controller.LevelChangeListener;
import com.radius.system.controller.enums.ButtonType;
import com.radius.system.controller.enums.MineFieldLevel;
import com.radius.system.controller.parameters.ButtonPressEvent;
import com.radius.system.utils.Assets;
import com.radius.system.utils.FontUtils;
import com.radius.system.view.game_ui.FlagCounter;
import com.radius.system.view.game_ui.GameButton;
import com.radius.system.view.game_ui.HeadsUpDisplay;
import com.radius.system.view.game_ui.score.ScoreEntry;
import com.radius.system.view.game_ui.score.ScoreEntryDisplay;
import com.radius.system.view.game_ui.TextGameButton;
import com.radius.system.view.game_ui.Timer;
import com.radius.system.view.game_ui.score.ScoreNameInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameStage extends Stage implements ButtonPressListener {

    private static final String GAME_OVER_TEXT = "GAME OVER!";
    private static final String WIN_TEXT = "YOU WIN!";

    private static final int MAX_SCORES_DISPLAY = 5;
    private static final String DEFAULT_NAME = "AAA";
    private static final float DEFAULT_TIME = 5999.999f;

    private final java.util.List<ButtonPressListener> buttonPressListeners = new ArrayList<>();
    private final java.util.List<LevelChangeListener> levelChangeListeners = new ArrayList<>();

    private final Timer timer;
    private final FlagCounter flagCounter;
    private final HeadsUpDisplay hud;

    private final GameButton pauseButton;
    private final TextGameButton okayButton;

    //private final Table scoresTable;
    private final Table settingsTable;

    private final Map<MineFieldLevel, List<ScoreEntry>> scoresMap = new HashMap<>();
    private final List<ScoreEntryDisplay> activeScoresList = new ArrayList<>();
    //private final SelectBox<String> levelSelection;
    private final BitmapFont font;
    private final TextField nameInput;

    private float worldWidth;
    private float worldHeight;

    private boolean paused;
    private boolean askingName = false;
    private boolean gameOver;

    private String nameEntry;
    private String endMessage;
    private MineFieldLevel activeLevel;
    private final Preferences prefs;

    private int overwriteScoreIndex;

    public GameStage(Viewport viewport) {
        super(viewport);
        this.prefs = Gdx.app.getPreferences(GameScreen.APP_NAME);
        font = FontUtils.GetFont((int) GameScreen.SCALE / 2, Color.WHITE, 1, Color.BLACK);
        LoadScores();

        worldWidth = getViewport().getWorldWidth();
        worldHeight = getViewport().getWorldHeight();

        this.addActor(hud = new HeadsUpDisplay(0, 0, worldWidth, worldHeight / 16f));
        this.addActor(timer = new Timer(0, 0, GameScreen.SCALE / 3, GameScreen.SCALE / 3));
        this.addActor(flagCounter = new FlagCounter(0, 0, GameScreen.SCALE / 3, GameScreen.SCALE / 3));
        this.addActor(pauseButton = CreateGameButton(Assets.BUTTON_PAUSE_TEXTURE_PATH, ButtonType.PAUSE, 0, 0, GameScreen.SCALE / 2.5f, GameScreen.SCALE / 2.5f, 1));
        this.addActor(okayButton = CreateTextButton(Assets.WHITE_SQUARE, ButtonType.PROCEED, 0, 0, GameScreen.SCALE / 2.5f, GameScreen.SCALE / 2.5f, 1, "OK"));
        this.addActor(settingsTable = CreateSettings());
        this.addActor(nameInput = CreateTextInput());

        //this.addActor(scoresTable = CreateScoresTable());

        SetUIStates();
    }

    private TextField CreateTextInput() {
        TextField.TextFieldStyle style = new TextField.TextFieldStyle();
        style.font = FontUtils.GetFont((int) GameScreen.SCALE / 4, Color.WHITE, 1, Color.BLACK);
        style.fontColor = Color.WHITE;
        style.background = new TextureRegionDrawable(Assets.LoadTexture(Assets.WHITE_SQUARE));
        nameEntry = prefs.getString(DEFAULT_NAME, DEFAULT_NAME);
        TextField field = new TextField(nameEntry, style);
        field.setAlignment(Align.center);
        field.setWidth(GameScreen.SCALE * 2);
        field.setHeight(font.getLineHeight() / 2f);
        field.setMaxLength(3);
        field.setPosition(worldWidth / 2 - field.getWidth() / 2, worldHeight / 2 - field.getHeight() / 2);
        return field;
    }

    private Table CreateSettings() {

        float buttonSizeX = GameScreen.SCALE / 2.5f;
        float buttonSizeY = GameScreen.SCALE / 2.5f;

        Table settings = new Table();
        //settings.setDebug(true);

        SelectBox<String> selectBox = CreateSelectionBox();
        settings.add(selectBox).padBottom(GameScreen.SCALE / 16).colspan(2);
        settings.row();
        settings.add(CreateScoresTable()).colspan(2).padBottom(GameScreen.SCALE / 4).row();
        settings.add(CreateGameButton(Assets.BUTTON_PLAY_TEXTURE_PATH, ButtonType.CONTINUE, 0, 0, buttonSizeX, buttonSizeY, 1)).width(buttonSizeX).height(buttonSizeY);
        settings.add(CreateGameButton(Assets.BUTTON_RESTART_TEXTURE_PATH, ButtonType.RESTART, 0, 0, buttonSizeX, buttonSizeY, 1)).width(buttonSizeX).height(buttonSizeY);
        settings.setPosition(worldWidth / 2 - settings.getWidth() / 2, worldHeight / 2 + GameScreen.SCALE);

        return settings;
    }

    private Table CreateScoresTable() {
        Table scoresTable = new Table();
        //scoresTable.setDebug(true);
        Label.LabelStyle headerStyle = new Label.LabelStyle();
        headerStyle.font = FontUtils.GetFont((int) GameScreen.SCALE / 3, Color.WHITE, 1, Color.BLACK);
        scoresTable.add(new Label("Best Times", headerStyle)).colspan(2);
        scoresTable.row();
        for (int i = 0; i < MAX_SCORES_DISPLAY; i++) {
            ScoreEntryDisplay entry = new ScoreEntryDisplay(DEFAULT_NAME, DEFAULT_TIME);
            activeScoresList.add(entry);
            scoresTable.add(entry.nameLabel).padRight(GameScreen.SCALE / 16);
            scoresTable.add(entry.timeLabel);
            scoresTable.row();
        }

        //scoresTable.setPosition(worldWidth / 2 - scoresTable.getWidth() / 2, worldHeight / 2 + GameScreen.SCALE);
        SetScoresDisplay();
        return scoresTable;
    }

    private SelectBox<String> CreateSelectionBox() {


        BitmapFont styleFont = FontUtils.GetFont((int) GameScreen.SCALE / 3, Color.WHITE, 1, Color.BLACK);

        // Create the list style for items display.
        com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle listStyle = new com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle(styleFont, Color.WHITE, Color.WHITE, new TextureRegionDrawable(Assets.LoadTexture(Assets.REVEALED_TEXTURE_PATH)));

        TextureRegionDrawable drawable = new TextureRegionDrawable(Assets.LoadTexture(Assets.WHITE_SQUARE));
        drawable.setMinSize(GameScreen.SCALE, GameScreen.SCALE);
        // Create the scroll style for allowing scroll interaction.
        ScrollPane.ScrollPaneStyle scrollPaneStyle = new ScrollPane.ScrollPaneStyle(drawable, null, new TextureRegionDrawable(Assets.LoadTexture(Assets.BASE_TEXTURE_PATH)), null, null);

        // Create the actual selection box style.
        SelectBox.SelectBoxStyle style = new SelectBox.SelectBoxStyle(styleFont, Color.WHITE, null, scrollPaneStyle, listStyle);

        SelectBox<String> stringSelectBox = new SelectBox<>(style);
        //stringSelectBox.getScrollPane().setSize(worldWidth / 3, GameScreen.SCALE / 4);

        stringSelectBox.setAlignment(Align.center);
        stringSelectBox.getList().setAlignment(Align.center);
        stringSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                FireLevelChangeEvent(MineFieldLevel.FromString(stringSelectBox.getSelected()));
            }
        });

        Array<String> items = new Array<>();
        for (MineFieldLevel level : MineFieldLevel.values()) {
            items.add(level.label);
        }
        stringSelectBox.setItems(items);

        String levelString = prefs.getString("level", MineFieldLevel.Expert.label);
        stringSelectBox.setSelected(levelString);
        activeLevel = MineFieldLevel.FromString(levelString);
        return stringSelectBox;
        /**/

        /*
        SelectBox<String> defaultStyleBox = new SelectBox<>(new Skin(Gdx.files.internal("ui/skin/uiskin.json")));
        defaultStyleBox.setStyle(style);
        return defaultStyleBox;
        /**/
    }

    private GameButton CreateGameButton(String texturePath, ButtonType type, float x, float y, float width, float height, float alpha) {
        TextureRegion texture = new TextureRegion(Assets.LoadTexture(texturePath));
        GameButton button = new GameButton(texture, type, x, y, width, height, alpha);
        button.AddListener(this);
        return button;
    }

    private TextGameButton CreateTextButton(String texturePath, ButtonType type, float x, float y, float width, float height, float alpha, String text) {
        TextureRegion texture = new TextureRegion(Assets.LoadTexture(texturePath));
        TextGameButton button = new TextGameButton(texture, type, x, y, width, height);
        button.SetText(text);
        button.getColor().a = alpha;
        button.AddListener(this);
        return button;
    }

    private void SetUIStates() {
        pauseButton.setVisible(!paused);
        settingsTable.setVisible(paused && !askingName);
        //scoresTable.setVisible(paused);

        nameInput.setVisible(askingName);
        okayButton.setVisible(askingName);

        Resize();
    }

    public void Resize() {
        worldWidth = getViewport().getWorldWidth();
        worldHeight = getViewport().getWorldHeight();

        hud.setPosition(hud.getX(), worldHeight - hud.getHeight());
        hud.setWidth(worldWidth);

        pauseButton.setPosition((worldWidth - GameScreen.SCALE) + GameScreen.SCALE / 4.5f, worldHeight - hud.getHeight() / 2 - pauseButton.getHeight() / 2);
        okayButton.setPosition(worldWidth / 2 - okayButton.getWidth() / 2, worldHeight / 2 - GameScreen.SCALE);

        //timer.setPosition(worldWidth - timer.getWidth() * 6, worldHeight - hud.getHeight() / 2 - timer.getHeight() / 2);
        timer.setPosition(worldWidth / 2f - timer.getWidth() * 1.75f, worldHeight - hud.getHeight() / 2 - timer.getHeight() / 2);
        flagCounter.setPosition(GameScreen.SCALE / 4.5f, worldHeight - hud.getHeight() / 2 - flagCounter.getHeight() / 2);
    }

    public void StartTime() {
        timer.StartTimer();
    }

    public void Start(MineFieldLevel level, boolean bypassLoad) {
        timer.SetTime(0);
        timer.StopTimer();
        flagCounter.SetRemainingFlags(level.mineCount);
        gameOver = paused = false;
        SetUIStates();

        if (!bypassLoad) {
            Load(level);
        }
    }

    private void Load(MineFieldLevel level) {
        float time = prefs.getFloat("time", 0);
        timer.SetTime(time);
        if (time > 0) timer.StartTimer();
        flagCounter.SetRemainingFlags(prefs.getInteger("flags", level.mineCount));
    }

    public void UpdateFlagCount(int increase) {
        flagCounter.UpdateFlagCount(increase);
    }

    public float GetTime() {
        return timer.GetTime();
    }

    public int GetRemainingFlags() {
        return flagCounter.GetRemainingFlags();
    }

    public void SetGameOver(boolean win) {
        gameOver = true;
        timer.SetRunningStatus(false);
        SetUIStates();

        Gdx.input.vibrate(500, false);

        endMessage = win ? WIN_TEXT : GAME_OVER_TEXT;
        if (win) {
            UpdateScores();
        }

    }

    public boolean IsPaused() {
        return paused;
    }

    private void UpdateScores() {
        List<ScoreEntry> entries = scoresMap.get(activeLevel);
        int i = entries.size();
        for (; i > 0; i--) {
            ScoreEntry entry = entries.get(i - 1);
            if (entry.GetTime() < timer.GetTime()) break;
        }

        if (i >= entries.size()) return;
        //Gdx.input.getTextInput();

        askingName = true;
        entries.remove(entries.size() - 1);
        overwriteScoreIndex = i;
        SetUIStates();
    }

    private void OverwriteScore() {
        if (overwriteScoreIndex < 0) return;
        List<ScoreEntry> entries = scoresMap.get(activeLevel);
        nameEntry = nameInput.getText();
        entries.add(overwriteScoreIndex, new ScoreEntry(nameEntry, timer.GetTime()));
        SetScoresDisplay();

        overwriteScoreIndex = -1;
        Gdx.input.setOnscreenKeyboardVisible(false);
    }

    private void SetScoresDisplay() {
        List<ScoreEntry> displayEntries = scoresMap.get(activeLevel);
        for (int i = 0; i < MAX_SCORES_DISPLAY; i++) {
            if (i >= displayEntries.size() || i >= activeScoresList.size()) break;
            ScoreEntry entry = displayEntries.get(i);
            ScoreEntry activeEntry = activeScoresList.get(i);
            activeEntry.SetName(entry.GetName());
            activeEntry.SetTime(entry.GetTime());
        }
    }

    private void LoadScores() {
        for (MineFieldLevel level : MineFieldLevel.values()) {
            List<ScoreEntry> entries = new ArrayList<>();
            for (int i = 0; i < MAX_SCORES_DISPLAY; i++) {
                String entryName = prefs.getString(level.label + i + DEFAULT_NAME, DEFAULT_NAME);
                float entryTime = prefs.getFloat(level.label + i + DEFAULT_TIME, DEFAULT_TIME);
                entries.add(new ScoreEntry(entryName, entryTime));
            }

            scoresMap.put(level, entries);
        }

        //SetScoresDisplay();
    }

    public void SaveScores() {
        for (MineFieldLevel level : MineFieldLevel.values()) {
            List<ScoreEntry> entries = scoresMap.get(level);
            for (int i = 0; i < MAX_SCORES_DISPLAY && i < entries.size(); i++) {
                ScoreEntry entry = entries.get(i);
                prefs.putString(level.label + i + DEFAULT_NAME, entry.GetName());
                prefs.putFloat(level.label + i + DEFAULT_TIME, entry.GetTime());
            }
        }
        prefs.flush();
    }

    public void Save() {
        prefs.putFloat("time", timer.GetTime());
        prefs.putInteger("flags", flagCounter.GetRemainingFlags());
        prefs.putString(DEFAULT_NAME, nameEntry);
        prefs.flush();
        SaveScores();
    }

    @Override
    public void draw() {
        Batch batch = getBatch();
        getViewport().apply();
        batch.setProjectionMatrix(getCamera().combined);
        batch.begin();

        if (gameOver && !paused) {
            if (!endMessage.equals(GAME_OVER_TEXT)) {
                font.draw(batch, endMessage, getCamera().viewportWidth / 2 - (GAME_OVER_TEXT.length() * GameScreen.SCALE) / 2f, getCamera().viewportHeight - GameScreen.SCALE, GAME_OVER_TEXT.length() * GameScreen.SCALE, Align.center, false);
            }
        }

        batch.end();
        super.draw();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return super.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return super.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return super.touchDragged(screenX, screenY, pointer);
    }

    @Override
    public void dispose() {
        super.dispose();
        Save();
    }

    @Override
    public void OnButtonPress(ButtonPressEvent event) {
        switch (event.buttonType) {
            case RESTART:
            case CONTINUE:
                paused = false;
                break;
            case PAUSE:
                paused = true;
                break;
            case PROCEED:
                askingName = false;
                OverwriteScore();
                break;
        }

        timer.SetRunningStatus(!paused && !gameOver);
        SetUIStates();
        FireButtonEvent(event);
    }

    public void AddButtonPressListener(ButtonPressListener listener) {
        if (buttonPressListeners.contains(listener)) return;
        buttonPressListeners.add(listener);
    }

    private void FireButtonEvent(ButtonPressEvent event) {
        for (ButtonPressListener listener : buttonPressListeners) {
            listener.OnButtonPress(event);
        }
    }

    public void AddLevelChangeListener(LevelChangeListener listener) {
        if (levelChangeListeners.contains(listener)) return;
        levelChangeListeners.add(listener);
    }

    private void FireLevelChangeEvent(MineFieldLevel level) {
        activeLevel = level;
        SetScoresDisplay();

        for (LevelChangeListener listener : levelChangeListeners) {
            listener.OnLevelChange(level);
        }
    }
}
