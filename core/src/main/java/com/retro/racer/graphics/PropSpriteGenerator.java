package com.retro.racer.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

import java.util.HashMap;
import java.util.Map;

/**
 * Procedurally generates crisp 16-bit pixel-art roadside prop textures:
 * twilight highway streetlights and reflective red-and-white chevron turn signs.
 */
public final class PropSpriteGenerator {

    private PropSpriteGenerator() {
    }

    /**
     * Creates a tall cantilevered twilight highway streetlight sprite.
     *
     * @param leftSide true when positioned on the left edge (arm reaches right over the road)
     */
    public static Texture createStreetlightTexture(boolean leftSide) {
        int w = 44;
        int h = 112;
        PixelArtCanvas canvas = new PixelArtCanvas(w, h);

        Color poleDark = new Color(0.18f, 0.21f, 0.28f, 1f);
        Color poleMid = new Color(0.34f, 0.39f, 0.48f, 1f);
        Color poleHi = new Color(0.56f, 0.62f, 0.72f, 1f);
        Color baseColor = new Color(0.14f, 0.16f, 0.22f, 1f);
        Color hood = new Color(0.24f, 0.27f, 0.34f, 1f);
        Color bulbCore = new Color(1.00f, 0.96f, 0.78f, 1f);
        Color bulbWarm = new Color(1.00f, 0.76f, 0.30f, 1f);
        Color glowOuter = new Color(1.00f, 0.72f, 0.24f, 0.22f);
        Color glowMid = new Color(1.00f, 0.84f, 0.42f, 0.42f);

        int poleX = leftSide ? 8 : (w - 13);

        // Concrete / metal footing base
        canvas.fillRect(poleX - 3, h - 10, 11, 10, baseColor);
        canvas.fillRect(poleX - 2, h - 9, 3, 8, poleMid);

        // Tall tapered vertical mast
        canvas.fillRect(poleX, 14, 5, h - 22, poleDark);
        canvas.fillRect(poleX + 1, 14, 2, h - 22, poleMid);
        canvas.fillRect(poleX + 1, 16, 1, h - 26, poleHi);

        // Curved cantilevered arm reaching toward the road
        int lampX;
        if (leftSide) {
            canvas.fillRect(poleX + 2, 10, 10, 4, poleDark);
            canvas.fillRect(poleX + 10, 8, 18, 4, poleDark);
            canvas.fillRect(poleX + 4, 10, 22, 2, poleMid);
            lampX = poleX + 24;
        } else {
            canvas.fillRect(poleX - 7, 10, 10, 4, poleDark);
            canvas.fillRect(poleX - 23, 8, 18, 4, poleDark);
            canvas.fillRect(poleX - 21, 10, 22, 2, poleMid);
            lampX = poleX - 25;
        }

        // Soft pixel light halo around sodium-vapor bulb
        int cx = lampX + 4;
        int cy = 14;
        canvas.fillCircle(cx, cy, 10, glowOuter);
        canvas.fillCircle(cx, cy, 6, glowMid);

        // Lamp housing hood & warm glowing bulb
        canvas.fillRect(lampX - 1, 8, 11, 4, hood);
        canvas.fillRect(lampX + 1, 7, 7, 2, poleHi);
        canvas.fillRect(lampX + 1, 12, 7, 3, bulbWarm);
        canvas.fillRect(lampX + 2, 12, 5, 2, bulbCore);

        return canvas.bakeTexture(false);
    }

    /**
     * Creates a bold red-and-white 16-bit chevron turn-warning sign on twin metal posts.
     *
     * @param pointRight true for a right-pointing chevron (&gt;&gt;), false for left (&lt;&lt;)
     */
    public static Texture createChevronSignTexture(boolean pointRight) {
        int w = 36;
        int h = 46;
        PixelArtCanvas canvas = new PixelArtCanvas(w, h);

        Color postDark = new Color(0.24f, 0.27f, 0.32f, 1f);
        Color postHi = new Color(0.55f, 0.60f, 0.68f, 1f);
        Color border = new Color(0.10f, 0.10f, 0.13f, 1f);
        Color red = new Color(0.90f, 0.12f, 0.14f, 1f);
        Color redHi = new Color(1.00f, 0.28f, 0.24f, 1f);
        Color white = new Color(0.96f, 0.96f, 0.98f, 1f);

        // Twin steel posts
        canvas.fillRect(8, 22, 3, 24, postDark);
        canvas.fillRect(9, 22, 1, 24, postHi);
        canvas.fillRect(25, 22, 3, 24, postDark);
        canvas.fillRect(26, 22, 1, 24, postHi);

        // Sign board frame
        canvas.fillRect(2, 2, 32, 22, border);

        // 2D pixel-art matrix for right-pointing red-and-white chevron sign (30x20)
        Map<Character, Color> palette = new HashMap<Character, Color>();
        palette.put('R', red);
        palette.put('H', redHi);
        palette.put('W', white);

        String[] rightChevron = new String[] {
                "HHHHHHHHHHHHHHHHHHHHHHHHHHHHHH",
                "RRRRRWWWWWWRRRRRWWWWWWRRRRRRRR",
                "RRRRRRWWWWWWRRRRRWWWWWWRRRRRRR",
                "RRRRRRRWWWWWWRRRRRWWWWWWRRRRRR",
                "RRRRRRRRWWWWWWRRRRRWWWWWWRRRRR",
                "RRRRRRRRRWWWWWWRRRRRWWWWWWRRRR",
                "RRRRRRRRRRWWWWWWRRRRRWWWWWWRRR",
                "RRRRRRRRRRRWWWWWWRRRRRWWWWWWRR",
                "RRRRRRRRRRRRWWWWWWRRRRRWWWWWWR",
                "RRRRRRRRRRRRRWWWWWWRRRRRWWWWWW",
                "RRRRRRRRRRRRRWWWWWWRRRRRWWWWWW",
                "RRRRRRRRRRRRWWWWWWRRRRRWWWWWWR",
                "RRRRRRRRRRRWWWWWWRRRRRWWWWWWRR",
                "RRRRRRRRRRWWWWWWRRRRRWWWWWWRRR",
                "RRRRRRRRRWWWWWWRRRRRWWWWWWRRRR",
                "RRRRRRRRWWWWWWRRRRRWWWWWWRRRRR",
                "RRRRRRRWWWWWWRRRRRWWWWWWRRRRRR",
                "RRRRRRWWWWWWRRRRRWWWWWWRRRRRRR",
                "RRRRRWWWWWWRRRRRWWWWWWRRRRRRRR",
                "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRR"
        };

        canvas.drawMatrix(3, 3, rightChevron, palette, !pointRight);

        return canvas.bakeTexture(false);
    }
}
