package com.retro.racer.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.retro.racer.entities.CarSpriteState;

import java.util.HashMap;
import java.util.Map;

/**
 * Procedurally generates 16-bit OutRun-inspired rear-view car sprites:
 * the signature red Ferrari-style player sports car (across steering, uphill,
 * and braking states) plus four distinct rival traffic car colorways.
 */
public final class CarSpriteGenerator {

    private static final int PLAYER_W = 80;
    private static final int PLAYER_H = 44;

    private static final int TRAFFIC_W = 56;
    private static final int TRAFFIC_H = 34;

    private CarSpriteGenerator() {
    }

    /**
     * Generates a single crisp 16-bit rear-view red sports car frame matching {@code state}.
     */
    public static Texture createPlayerCarTexture(CarSpriteState state) {
        PixelArtCanvas canvas = new PixelArtCanvas(PLAYER_W, PLAYER_H);

        int steer = state.getSteerDirection(); // -1 left, 0 straight, +1 right
        boolean uphill = state.isUphill();
        boolean braking = state.isBraking();

        // Palette for iconic 16-bit crimson Ferrari Testarossa-style supercar
        Color shadow = new Color(0f, 0f, 0f, 0.42f);
        Color outline = new Color(0.10f, 0.04f, 0.06f, 1f);
        Color bodyDark = new Color(0.56f, 0.05f, 0.07f, 1f);
        Color bodyMid = new Color(0.84f, 0.09f, 0.11f, 1f);
        Color bodyBright = new Color(0.98f, 0.22f, 0.18f, 1f);
        Color bodySpec = new Color(1.00f, 0.56f, 0.48f, 1f);
        Color roofGlass = new Color(0.11f, 0.16f, 0.28f, 1f);
        Color glassHi = new Color(0.32f, 0.48f, 0.72f, 1f);
        Color grille = new Color(0.11f, 0.11f, 0.14f, 1f);
        Color slat = new Color(0.22f, 0.22f, 0.27f, 1f);
        Color tire = new Color(0.08f, 0.08f, 0.10f, 1f);
        Color tireEdge = new Color(0.19f, 0.19f, 0.22f, 1f);
        Color chrome = new Color(0.80f, 0.84f, 0.88f, 1f);
        Color exhaustInner = new Color(0.05f, 0.05f, 0.07f, 1f);
        Color plateYellow = new Color(0.96f, 0.84f, 0.28f, 1f);
        Color tailDim = new Color(0.75f, 0.10f, 0.08f, 1f);
        Color tailAmber = new Color(0.96f, 0.64f, 0.14f, 1f);
        Color tailBright = new Color(1.00f, 0.18f, 0.12f, 1f);
        Color tailCore = new Color(1.00f, 0.78f, 0.45f, 1f);
        Color brakeGlow = new Color(1.00f, 0.12f, 0.08f, 0.45f);

        int yShift = uphill ? -2 : 0;
        int topShift = steer * 3;
        int midShift = steer * 1;

        // Ground contact shadow
        canvas.fillRect(7, 39, 66, 4, shadow);
        canvas.fillRect(10, 42, 60, 2, shadow);

        // Wide rear tires (slight horizontal shift when steering to show wheel turn)
        int leftTireX = 8 + (steer < 0 ? -1 : 0);
        int rightTireX = 60 + (steer > 0 ? 1 : 0);
        canvas.fillRect(leftTireX, 27 + yShift, 12, 13, tire);
        canvas.fillRect(leftTireX + 1, 29 + yShift, 2, 9, tireEdge);
        canvas.fillRect(rightTireX, 27 + yShift, 12, 13, tire);
        canvas.fillRect(rightTireX + 9, 29 + yShift, 2, 9, tireEdge);

        // Lower rear diffuser & twin chrome exhaust tips
        canvas.fillRect(18, 32 + yShift, 44, 6, grille);
        canvas.fillRect(20, 34 + yShift, 40, 2, slat);
        // Left twin exhaust
        canvas.fillRect(21, 34 + yShift, 4, 3, chrome);
        canvas.fillRect(22, 35 + yShift, 2, 1, exhaustInner);
        canvas.fillRect(26, 34 + yShift, 4, 3, chrome);
        canvas.fillRect(27, 35 + yShift, 2, 1, exhaustInner);
        // Right twin exhaust
        canvas.fillRect(50, 34 + yShift, 4, 3, chrome);
        canvas.fillRect(51, 35 + yShift, 2, 1, exhaustInner);
        canvas.fillRect(55, 34 + yShift, 4, 3, chrome);
        canvas.fillRect(56, 35 + yShift, 2, 1, exhaustInner);

        // Wide sculpted rear fenders / lower body
        canvas.fillRect(7, 21 + yShift, 66, 12, outline);
        canvas.fillRect(8, 22 + yShift, 64, 10, bodyDark);
        canvas.fillRect(9, 21 + yShift, 62, 6, bodyMid);
        canvas.fillRect(11, 20 + yShift, 58, 3, bodyBright);

        // Iconic side air-intake strakes visible on rear haunches when turning
        if (steer < 0) {
            canvas.fillRect(7, 23 + yShift, 5, 1, grille);
            canvas.fillRect(7, 25 + yShift, 5, 1, grille);
            canvas.fillRect(7, 27 + yShift, 5, 1, grille);
        } else if (steer > 0) {
            canvas.fillRect(68, 23 + yShift, 5, 1, grille);
            canvas.fillRect(68, 25 + yShift, 5, 1, grille);
            canvas.fillRect(68, 27 + yShift, 5, 1, grille);
        }

        // Rear black louvered grille fascia across the tail
        canvas.fillRect(11 + midShift, 23 + yShift, 58, 7, grille);
        for (int y = 24 + yShift; y <= 28 + yShift; y += 2) {
            canvas.fillRect(12 + midShift, y, 56, 1, slat);
        }

        // License plate in center recess
        canvas.fillRect(34 + midShift, 25 + yShift, 12, 5, outline);
        canvas.fillRect(35 + midShift, 26 + yShift, 10, 3, plateYellow);
        canvas.fillRect(36 + midShift, 27 + yShift, 2, 1, outline);
        canvas.fillRect(40 + midShift, 27 + yShift, 4, 1, outline);

        // Taillight clusters (left & right) with vivid glow when braking
        int ltX = 12 + midShift;
        int rtX = 54 + midShift;
        int tailY = 24 + yShift;
        if (braking) {
            // Soft outer red glow halo
            canvas.fillRect(ltX - 2, tailY - 2, 18, 9, brakeGlow);
            canvas.fillRect(rtX - 2, tailY - 2, 18, 9, brakeGlow);

            canvas.fillRect(ltX, tailY, 14, 5, tailBright);
            canvas.fillRect(ltX + 2, tailY + 1, 10, 3, tailCore);
            canvas.fillRect(rtX, tailY, 14, 5, tailBright);
            canvas.fillRect(rtX + 2, tailY + 1, 10, 3, tailCore);
        } else {
            canvas.fillRect(ltX, tailY, 9, 4, tailDim);
            canvas.fillRect(ltX + 1, tailY + 1, 7, 2, tailBright);
            canvas.fillRect(ltX + 10, tailY, 4, 4, tailAmber);

            canvas.fillRect(rtX + 5, tailY, 9, 4, tailDim);
            canvas.fillRect(rtX + 6, tailY + 1, 7, 2, tailBright);
            canvas.fillRect(rtX, tailY, 4, 4, tailAmber);
        }

        // Rear engine deck & buttresses
        int deckY = uphill ? 14 : 15;
        canvas.fillRect(12 + midShift, deckY, 56, 6, bodyMid);
        canvas.fillRect(14 + midShift, deckY, 52, 2, bodyBright);
        canvas.fillRect(18 + midShift, deckY, 44, 1, bodySpec);
        // Engine louver vents on rear deck
        for (int vx = 25; vx <= 53; vx += 4) {
            canvas.fillRect(vx + midShift, deckY + 2, 2, 3, grille);
        }

        // Cabin greenhouse (tapered roof & rear windshield)
        int roofY = uphill ? 7 : 8;
        canvas.fillRect(19 + topShift, roofY + 1, 42, 8, outline);
        canvas.fillRect(21 + topShift, roofY, 38, 2, bodyBright);
        canvas.fillRect(24 + topShift, roofY, 32, 1, bodySpec);
        // C-pillars
        canvas.fillRect(19 + topShift, roofY + 2, 4, 6, bodyMid);
        canvas.fillRect(57 + topShift, roofY + 2, 4, 6, bodyMid);
        // Rear glass + diagonal 16-bit sky reflection streak
        canvas.fillRect(23 + topShift, roofY + 2, 34, 6, roofGlass);
        canvas.drawLine(26 + topShift, roofY + 7, 32 + topShift, roofY + 2, glassHi);
        canvas.drawLine(28 + topShift, roofY + 7, 34 + topShift, roofY + 2, glassHi);
        canvas.drawLine(45 + topShift, roofY + 7, 49 + topShift, roofY + 2, glassHi);

        // Side wing mirrors
        canvas.fillRect(14 + topShift, roofY + 5, 4, 3, bodyBright);
        canvas.fillRect(62 + topShift, roofY + 5, 4, 3, bodyBright);

        // Sleek rear racing spoiler lip
        int wingY = deckY + 3;
        canvas.fillRect(9 + midShift, wingY, 62, 2, outline);
        canvas.fillRect(10 + midShift, wingY - 1, 60, 2, bodyBright);
        canvas.fillRect(14 + midShift, wingY - 1, 52, 1, bodySpec);

        // Subtle chrome Ferrari-style emblem dot above plate
        canvas.fillRect(39 + midShift, 22 + yShift, 2, 2, chrome);

        return canvas.bakeTexture(false);
    }

    /**
     * Generates a retro 16-bit rear-view AI traffic car sprite for the given color variant (0..3).
     */
    public static Texture createTrafficCarTexture(int variantIndex) {
        PixelArtCanvas canvas = new PixelArtCanvas(TRAFFIC_W, TRAFFIC_H);

        Color bodyDark;
        Color bodyMid;
        Color bodyBright;
        switch (Math.abs(variantIndex) % 4) {
            case 0: // Electric Cyan-Blue Coupe
                bodyDark = new Color(0.08f, 0.30f, 0.58f, 1f);
                bodyMid = new Color(0.14f, 0.52f, 0.86f, 1f);
                bodyBright = new Color(0.42f, 0.78f, 0.98f, 1f);
                break;
            case 1: // Arcade Canary Yellow Sports Coupe
                bodyDark = new Color(0.62f, 0.44f, 0.06f, 1f);
                bodyMid = new Color(0.92f, 0.72f, 0.12f, 1f);
                bodyBright = new Color(1.00f, 0.90f, 0.40f, 1f);
                break;
            case 2: // Pearl Silver Sedan
                bodyDark = new Color(0.42f, 0.45f, 0.52f, 1f);
                bodyMid = new Color(0.72f, 0.76f, 0.82f, 1f);
                bodyBright = new Color(0.92f, 0.95f, 0.98f, 1f);
                break;
            default: // Retro Emerald GT
                bodyDark = new Color(0.07f, 0.38f, 0.28f, 1f);
                bodyMid = new Color(0.14f, 0.64f, 0.46f, 1f);
                bodyBright = new Color(0.36f, 0.86f, 0.66f, 1f);
                break;
        }

        Color shadow = new Color(0f, 0f, 0f, 0.38f);
        Color outline = new Color(0.08f, 0.08f, 0.12f, 1f);
        Color tire = new Color(0.07f, 0.07f, 0.09f, 1f);
        Color glass = new Color(0.12f, 0.18f, 0.30f, 1f);
        Color glassHi = new Color(0.40f, 0.56f, 0.78f, 1f);
        Color bumper = new Color(0.18f, 0.19f, 0.24f, 1f);
        Color tailRed = new Color(0.95f, 0.16f, 0.14f, 1f);
        Color tailAmber = new Color(0.98f, 0.68f, 0.18f, 1f);
        Color plate = new Color(0.95f, 0.95f, 0.90f, 1f);

        // Ground shadow
        canvas.fillRect(6, 30, 44, 3, shadow);

        // Tires
        canvas.fillRect(7, 21, 8, 10, tire);
        canvas.fillRect(41, 21, 8, 10, tire);

        // Main lower body
        canvas.fillRect(6, 15, 44, 12, outline);
        canvas.fillRect(7, 16, 42, 10, bodyDark);
        canvas.fillRect(8, 15, 40, 6, bodyMid);
        canvas.fillRect(10, 14, 36, 2, bodyBright);

        // Rear bumper
        canvas.fillRect(7, 23, 42, 4, bumper);
        canvas.fillRect(9, 24, 38, 1, outline);

        // License plate
        canvas.fillRect(23, 20, 10, 4, outline);
        canvas.fillRect(24, 21, 8, 2, plate);

        // Taillights
        canvas.fillRect(9, 18, 9, 4, outline);
        canvas.fillRect(10, 19, 6, 2, tailRed);
        canvas.fillRect(16, 19, 2, 2, tailAmber);

        canvas.fillRect(38, 18, 9, 4, outline);
        canvas.fillRect(40, 19, 6, 2, tailRed);
        canvas.fillRect(38, 19, 2, 2, tailAmber);

        // Cabin & rear window
        canvas.fillRect(13, 6, 30, 9, outline);
        canvas.fillRect(15, 5, 26, 2, bodyBright);
        canvas.fillRect(14, 7, 3, 7, bodyMid);
        canvas.fillRect(39, 7, 3, 7, bodyMid);
        canvas.fillRect(17, 7, 22, 6, glass);
        canvas.drawLine(19, 12, 24, 7, glassHi);
        canvas.drawLine(21, 12, 26, 7, glassHi);

        // Side mirrors
        canvas.fillRect(10, 11, 3, 2, bodyBright);
        canvas.fillRect(43, 11, 3, 2, bodyBright);

        // Decorative pixel-matrix badge (demonstrating 2D matrix drawing)
        Map<Character, Color> badgePalette = new HashMap<Character, Color>();
        badgePalette.put('C', new Color(0.88f, 0.90f, 0.94f, 1f));
        String[] badge = new String[] {
                ".CC.",
                "C..C"
        };
        canvas.drawMatrix(26, 16, badge, badgePalette, false);

        return canvas.bakeTexture(false);
    }
}
