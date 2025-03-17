package com.radius.system.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import java.util.HashMap;
import java.util.Map;

public class FontUtils {

    private static FontUtils instance;

    private Map<String, FreeTypeFontGenerator> generators;

    private Map<String, BitmapFont> fonts;

    private final String defaultFont = "fonts/octosquares.ttf";

    private FontUtils() {
        fonts = new HashMap<>();
        generators = new HashMap<>();
    }

    /**
     * The actual font creation takes place here.
     *
     * @param size        - The font size.
     * @param fontColor   - The font color.
     * @param borderWidth - The border width.
     * @param borderColor - The border color.
     * @return A bitmap font created based on the given parameters.
     */
    private BitmapFont CreateFont(String font, int size, Color fontColor, int borderWidth, Color borderColor) {
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = size;
        parameter.color = fontColor;
        parameter.borderWidth = borderWidth;
        parameter.borderColor = borderColor;

        parameter.borderStraight = true;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;

        if (!generators.containsKey(font)) {
            generators.put(font, new FreeTypeFontGenerator(Gdx.files.internal(font)));
        }

        return generators.get(font).generateFont(parameter);
    }

    public static BitmapFont GetFont(int size, Color fontColor, int borderWidth, Color borderColor) {
        if (instance == null) {
            instance = new FontUtils();
        }

        return GetFont(instance.defaultFont, size, fontColor, borderWidth, borderColor);
    }

    public static BitmapFont GetFont(String font, int size, Color fontColor, int borderWidth, Color borderColor) {
        if (instance == null) {
            instance = new FontUtils();
        }

        String key = stringify(size, fontColor, borderWidth, borderColor);

        if (!instance.fonts.containsKey(key)) {
            instance.fonts.put(key, instance.CreateFont(font, size, fontColor, borderWidth, borderColor));
        }

        return instance.fonts.get(key);
    }

    /**
     * Takes the given parameters and returns a string.
     *
     * @param size        - to be turned to string.
     * @param fontColor   - to be turned to string.
     * @param borderWidth - to be turned to string.
     * @param borderColor - to be turned to string.
     * @return A string based on the given parameters.
     */
    private static String stringify(int size, Color fontColor, int borderWidth,
                                    Color borderColor) {

        StringBuilder builder = new StringBuilder();
        builder.append(size);
        builder.append(fontColor.toString());
        builder.append(borderWidth);
        builder.append(borderColor);

        return builder.toString();
    }

    public static void Dispose() {
        if (instance == null) {
            return;
        }

        for (String key : instance.generators.keySet()) {
            instance.generators.get(key).dispose();
        }
    }

}

