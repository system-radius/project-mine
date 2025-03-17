package com.radius.system.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Assets {
    private static final Random randomizer = new Random(System.currentTimeMillis());
    public static final String[] PLAYER_TEXTURE_PATHS = new String[]{
            "img/player_1.png", "img/player_2.png", "img/player_3.png", "img/player_4.png",
    };

    public static final int ENEMY_TEXTURE_REGION_WIDTH = 96;
    public static final int ENEMY_TEXTURE_REGION_HEIGHT = 128;
    public static final String[] ENEMY_TEXTURE_PATHS = new String[]{
            "enemies/01.png",  "enemies/02.png",  "enemies/03.png"
    };

    public static final String BOMB_TEXTURE_PATH = "img/bomb.png";
    public static final int BOMB_TEXTURE_REGION_SIZE = 32;

    public static final String FIRE_TEXTURE_PATH = "img/spiralingFire.png";
    public static final int FIRE_TEXTURE_REGION_SIZE = 64;

    public static final String BLOCKS_TEXTURE_PATH = "img/blocks.png";
    public static final int BLOCKS_TEXTURE_REGION_SIZE = 32;

    public static final String SYMBOLS_TEXTURE_PATH = "img/Lettering.png";
    public static final int SYMBOLS_TEXTURE_REGION_SIZE = 20;

    public static final String BUTTON_A_TEXTURE_PATH = "img/A.png";
    public static final String BUTTON_B_TEXTURE_PATH = "img/B.png";
    public static final String BUTTON_PAUSE_TEXTURE_PATH = "img/pause.png";
    public static final String BUTTON_PLAY_TEXTURE_PATH = "img/play.png";
    public static final String BUTTON_RESTART_TEXTURE_PATH = "img/restart.png";
    public static final String BUTTON_CANCEL_TEXTURE_PATH = "img/cancel.png";

    public static final String BACKGROUND_TEXTURE_PATH = "img/background.png";
    public static final String WHITE_SQUARE = "img/background_white.png";
    public static final String WARNING_SIGN_PATH = "img/warning_sign.png";
    public static final String BURN_TEST_PATH = "field/burn_test.png";

    public static final String DOOR_TEXTURE_PATH = "img/doors.png";
    public static final int DOOR_TEXTURE_REGION_SIZE = 32;

    public static final String SEAL_TEXTURE_PATH = "img/seal.png";
    public static final int SEAL_TEXTURE_REGION_SIZE = 192;

    public static final String BASE_TEXTURE_PATH = "mine/base.png";
    public static final String REVEALED_TEXTURE_PATH = "mine/0.png";
    public static final String FLAG_TEXTURE_PATH = "mine/flag.png";
    public static final String MINE_TEXTURE_PATH = "mine/mine.png";

    /* * * * * * * * * SFX  PATHS * * * * * */
    public static final String EXPLOSION_SFX_PATH = "sfx/ex.wav";
    public static final String BOMB_SET_SFX_PATH = "sfx/set.wav";
    public static final String BONUS_GET_SFX_PATH = "sfx/get.wav";
    public static final String PLAYER_BURN_SFX_PATH = "sfx/dead.wav";

    public static final String[] SFX_PATHS = {
            EXPLOSION_SFX_PATH,
            BOMB_SET_SFX_PATH,
            BONUS_GET_SFX_PATH,
            PLAYER_BURN_SFX_PATH
    };
    /* * * * * * * * * * *  * * * * * * * * */

    /* * * * * * * * * BMG  PATHS * * * * * */
    public static final String AFTERGLOW_BGM_PATH = "bgm/Afterglow.mp3";
    public static final String OMINOUS_CROSS_BGM_PATH = "bgm/Ominous Cross.mp3";
    public static final String BURNING_DESIRE_BGM_PATH = "bgm/burning desire.mp3";
    public static final String[] NORMAL_BGM = new String[] {
            BURNING_DESIRE_BGM_PATH
    };

    public static final String[] BOSS_BGM = new String[] {
            AFTERGLOW_BGM_PATH,
            OMINOUS_CROSS_BGM_PATH
    };
    /* * * * * * * * * * *  * * * * * * * * */

    /* * * * * * * CONFIG RELATED * * * * * */

    public static final String DEATH_MATCH_MODE_PATH = "mode_banners/death_match.png";
    public static final String CLASSIC_MODE_PATH = "mode_banners/classic.png";
    public static final String CFT_MODE_PATH = "mode_banners/cft.png";
    public static final String TEST_MODE_PATH = "mode_banners/test.png";
    public static final String FORWARD_TEXTURE_PATH = "img/forward.png";
    public static final String BACKWARD_TEXTURE_PATH = "img/backward.png";

    /* * * * * * * * * * * * * * * * * * * */

    private static final Map<String, Texture> textureMap = new HashMap<>();
    private static final Map<String, Sound> soundMap = new HashMap<>();
    private static final Map<String, TextureRegion[][]> textureRegionMap = new HashMap<>();
    private static final Map<String, Long> soundTimerMap = new HashMap<>();

    private static float bgmVolume = 0.5f;
    private static float sfxVolume = 0.5f;

    private static Music activeMusic;
    private static String activeMusicPath;

    private static Thread loadThread;

    private Assets() {}

    public static void PreLoad() {
        for (String path : PLAYER_TEXTURE_PATHS) {
            LoadTexture(path);
        }

        for (String path : ENEMY_TEXTURE_PATHS) {
            LoadTexture(path);
        }

        LoadTexture(BOMB_TEXTURE_PATH);
        LoadTexture(FIRE_TEXTURE_PATH);
        LoadTexture(BLOCKS_TEXTURE_PATH);
        LoadTexture(WARNING_SIGN_PATH);
        LoadTexture(DOOR_TEXTURE_PATH);
        LoadTexture(SEAL_TEXTURE_PATH);

        for (String path : SFX_PATHS) {
            LoadSound(path);
        }
    }

    public static Texture LoadTexture(String path) {
        if (textureMap.containsKey(path)) {
            return textureMap.get(path);
        }

        Texture texture = new Texture(Gdx.files.internal(path));
        textureMap.put(path, texture);
        return texture;
    }

    public static TextureRegion[][] LoadTextureRegion(String path, int tileWidth, int tileHeight) {
        if (textureRegionMap.containsKey(path)) {
            return textureRegionMap.get(path);
        }

        Texture texture = LoadTexture(path);
        TextureRegion[][] textureRegions = TextureRegion.split(texture, tileWidth, tileHeight);
        textureRegionMap.put(path, textureRegions);
        return textureRegions;
    }

    public static TextureRegion[] GetFrames(String path, int tileWidth, int tileHeight, int row) {
        TextureRegion[][] textureRegion = LoadTextureRegion(path, tileWidth, tileHeight);
        return textureRegion[row];
    }

    public static TextureRegion GetFrame(String path, int tileWidth, int tileHeight, int row, int col) {
        return GetFrames(path, tileWidth, tileHeight, row)[col];
    }

    public static TextureRegion[] GetFrames(String path, int row) {
        if (!textureRegionMap.containsKey(path)) {
            return null;
        }

        TextureRegion[][] regions = textureRegionMap.get(path);
        return regions[row];
    }

    public static Sound LoadSound(String path) {
        if (soundMap.containsKey(path)) {
            return soundMap.get(path);
        }

        Sound sound = Gdx.audio.newSound(Gdx.files.internal(path));
        soundMap.put(path, sound);
        return sound;
    }

    public static void PlaySound(String path) {
        LoadSound(path).play(sfxVolume);
    }

    public static Music LoadMusic(String path) {
        Music music = Gdx.audio.newMusic(Gdx.files.internal(path));
        music.setLooping(true);
        music.setVolume(bgmVolume);
        return music;
    }

    public static void PlayBackgroundMusic(String path) {
        if (path.equals(activeMusicPath)) {
            PlayMusic();
            return;
        }
        StopActiveBackgroundMusic();
        activeMusic = LoadMusic(path);
        activeMusicPath = path;
        activeMusic.play();
    }

    public static void PlayRandomBackgroundMusic() {
        PlayBackgroundMusic(NORMAL_BGM[randomizer.nextInt(NORMAL_BGM.length)]);
    }

    public static void PlayRandomBossBGM() {
        PlayBackgroundMusic(BOSS_BGM[randomizer.nextInt(BOSS_BGM.length)]);
    }

    public static void StopActiveBackgroundMusic() {
        if (activeMusic != null && activeMusic.isPlaying()) {
            activeMusic.stop();
            activeMusic.dispose();
            activeMusic = null;
            activeMusicPath = null;
        }
    }

    public static void PauseMusic() {
        if (activeMusic != null && activeMusic.isPlaying()) {
            activeMusic.pause();
        }
    }

    public static void PlayMusic() {
        if (activeMusic != null) {
            activeMusic.play();
        }
    }

    public static void SetBGMVolume(float volume) {
        bgmVolume = volume;
        activeMusic.setVolume(volume);
    }

    public static void SetSFXVolume(float volume) {
        sfxVolume = volume;
    }

    public static void Dispose() {
        for (String key : textureMap.keySet()) {
            textureMap.get(key).dispose();
        }

        for (String key : soundMap.keySet()) {
            soundMap.get(key).dispose();
        }
    }
}
