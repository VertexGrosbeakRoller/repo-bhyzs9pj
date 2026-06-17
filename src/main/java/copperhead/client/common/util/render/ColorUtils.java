package copperhead.client.common.util.render;

import java.awt.*;

public final class ColorUtils {
    private static int themeColor = 0xFF6E44FF;

    private ColorUtils() {}

    public static int getColor() {
        return themeColor;
    }

    public static void setThemeColor(int color) {
        themeColor = color;
    }

    public static int rgba(int r, int g, int b, int a) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int rgb(int r, int g, int b) {
        return rgba(r, g, b, 255);
    }

    public static float[] rgba(int color) {
        return new float[]{
                ((color >> 16) & 0xFF) / 255f,
                ((color >> 8) & 0xFF) / 255f,
                (color & 0xFF) / 255f,
                ((color >> 24) & 0xFF) / 255f
        };
    }

    public static float[] getColorhands(int color) {
        return new float[]{
                ((color >> 16) & 0xFF) / 255f,
                ((color >> 8) & 0xFF) / 255f,
                (color & 0xFF) / 255f
        };
    }

    public static int replAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    public static int multAlpha(int color, float mult) {
        int a = (int) (((color >> 24) & 0xFF) * mult);
        return replAlpha(color, Math.max(0, Math.min(255, a)));
    }

    public static int setAlpha(int color, int alpha) {
        return replAlpha(color, alpha);
    }

    public static int applyOpacity(int color, float opacity) {
        return multAlpha(color, opacity);
    }

    public static int darken(int color, float factor) {
        int r = Math.max(0, (int) (((color >> 16) & 0xFF) / factor));
        int g = Math.max(0, (int) (((color >> 8) & 0xFF) / factor));
        int b = Math.max(0, (int) ((color & 0xFF) / factor));
        int a = (color >> 24) & 0xFF;
        return rgba(r, g, b, a);
    }

    public static int multDark(int color, float factor) {
        int r = (int) (((color >> 16) & 0xFF) * factor);
        int g = (int) (((color >> 8) & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        int a = (color >> 24) & 0xFF;
        return rgba(r, g, b, a);
    }

    public static int gradient(int color1, int color2, int step, int maxSteps) {
        float ratio = (float) (step % maxSteps) / maxSteps;
        int r = (int) (((color1 >> 16) & 0xFF) * (1 - ratio) + ((color2 >> 16) & 0xFF) * ratio);
        int g = (int) (((color1 >> 8) & 0xFF) * (1 - ratio) + ((color2 >> 8) & 0xFF) * ratio);
        int b = (int) ((color1 & 0xFF) * (1 - ratio) + (color2 & 0xFF) * ratio);
        int a = (int) (((color1 >> 24) & 0xFF) * (1 - ratio) + ((color2 >> 24) & 0xFF) * ratio);
        return rgba(r, g, b, a);
    }

    public static int overCol(int color1, int color2, float factor) {
        float inv = 1f - factor;
        int r = (int) (((color1 >> 16) & 0xFF) * inv + ((color2 >> 16) & 0xFF) * factor);
        int g = (int) (((color1 >> 8) & 0xFF) * inv + ((color2 >> 8) & 0xFF) * factor);
        int b = (int) ((color1 & 0xFF) * inv + (color2 & 0xFF) * factor);
        int a = (int) (((color1 >> 24) & 0xFF) * inv + ((color2 >> 24) & 0xFF) * factor);
        return rgba(r, g, b, a);
    }

    public static int hex(String hex) {
        if (hex.startsWith("#")) hex = hex.substring(1);
        return (int) Long.parseLong(hex, 16);
    }

    public static int getAlphaFromColor(int color) {
        return (color >> 24) & 0xFF;
    }

    public static float getBrightnessFromColor(int color) {
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        return Math.max(r, Math.max(g, b));
    }

    public static int swapAlpha(int color, float alpha) {
        return replAlpha(color, (int) Math.max(0, Math.min(255, alpha)));
    }

    public static int toDark(int color, float factor) {
        return darken(color, 1f / factor);
    }

    public static int getOverallColorFrom(int c1, int c2, float factor) {
        return overCol(c1, c2, factor);
    }
}
