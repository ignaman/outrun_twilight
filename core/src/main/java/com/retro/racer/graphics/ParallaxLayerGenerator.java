package com.retro.racer.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.retro.racer.world.ProjectionMath;

import java.util.Random;

/**
 * Generates the three seamlessly tileable 16-bit twilight parallax layers:
 * starry twilight sky, fluffy stylized clouds, and a dark city skyline featuring
 * skyscrapers, lit windows, a tower crane, a rooftop water tower, and horizon trees.
 */
public final class ParallaxLayerGenerator {

    public static final int LAYER_WIDTH = 640;

    private ParallaxLayerGenerator() {
    }

    /**
     * Generates the deep twilight starry sky gradient (640x280, horizontally seamless).
     */
    public static Texture createTwilightSkyTexture() {
        int w = LAYER_WIDTH;
        int h = 280;
        PixelArtCanvas canvas = new PixelArtCanvas(w, h);

        Color topColor = new Color(0.03f, 0.05f, 0.16f, 1f);
        Color midUpper = new Color(0.08f, 0.12f, 0.30f, 1f);
        Color midLower = new Color(0.22f, 0.18f, 0.42f, 1f);
        Color horizonPink = new Color(0.52f, 0.26f, 0.44f, 1f);
        Color horizonAmber = new Color(0.76f, 0.40f, 0.36f, 1f);

        // Quantized 16-bit banding for authentic retro twilight gradient
        Color rowColor = new Color();
        for (int y = 0; y < h; y++) {
            float t = (float) y / (h - 1);
            // Subtle 16-bit color banding
            float q = (float) (Math.floor(t * 28f) / 28f);
            if (q < 0.38f) {
                float k = q / 0.38f;
                rowColor.set(
                        ProjectionMath.lerp(topColor.r, midUpper.r, k),
                        ProjectionMath.lerp(topColor.g, midUpper.g, k),
                        ProjectionMath.lerp(topColor.b, midUpper.b, k),
                        1f
                );
            } else if (q < 0.72f) {
                float k = (q - 0.38f) / 0.34f;
                rowColor.set(
                        ProjectionMath.lerp(midUpper.r, midLower.r, k),
                        ProjectionMath.lerp(midUpper.g, midLower.g, k),
                        ProjectionMath.lerp(midUpper.b, midLower.b, k),
                        1f
                );
            } else if (q < 0.90f) {
                float k = (q - 0.72f) / 0.18f;
                rowColor.set(
                        ProjectionMath.lerp(midLower.r, horizonPink.r, k),
                        ProjectionMath.lerp(midLower.g, horizonPink.g, k),
                        ProjectionMath.lerp(midLower.b, horizonPink.b, k),
                        1f
                );
            } else {
                float k = (q - 0.90f) / 0.10f;
                rowColor.set(
                        ProjectionMath.lerp(horizonPink.r, horizonAmber.r, k),
                        ProjectionMath.lerp(horizonPink.g, horizonAmber.g, k),
                        ProjectionMath.lerp(horizonPink.b, horizonAmber.b, k),
                        1f
                );
            }
            canvas.fillRect(0, y, w, 1, rowColor);
        }

        // Crisp pixel stars in the upper twilight sky
        Random rng = new Random(1986L);
        Color starBright = new Color(0.98f, 0.98f, 1.00f, 1f);
        Color starCyan = new Color(0.72f, 0.90f, 1.00f, 0.90f);
        Color starWarm = new Color(1.00f, 0.92f, 0.72f, 0.85f);
        Color starDim = new Color(0.65f, 0.72f, 0.95f, 0.55f);

        for (int i = 0; i < 145; i++) {
            int sx = rng.nextInt(w);
            int sy = rng.nextInt((int) (h * 0.68f));
            float pick = rng.nextFloat();
            if (pick < 0.14f) {
                // Bright 16-bit cross twinkle star
                canvas.setPixel(sx, sy, starBright);
                canvas.setPixel((sx + 1) % w, sy, starDim);
                canvas.setPixel((sx - 1 + w) % w, sy, starDim);
                canvas.setPixel(sx, Math.max(0, sy - 1), starDim);
                canvas.setPixel(sx, Math.min(h - 1, sy + 1), starDim);
            } else if (pick < 0.45f) {
                canvas.setPixel(sx, sy, starCyan);
            } else if (pick < 0.70f) {
                canvas.setPixel(sx, sy, starWarm);
            } else {
                canvas.setPixel(sx, sy, starDim);
            }
        }

        // Subtle retro crescent moon in upper right sky
        int moonX = 518;
        int moonY = 46;
        canvas.fillCircle(moonX, moonY, 13, new Color(0.98f, 0.94f, 0.78f, 0.95f));
        canvas.fillCircle(moonX - 5, moonY - 2, 11, new Color(0.05f, 0.08f, 0.22f, 1f));

        return canvas.bakeTexture(true);
    }

    /**
     * Generates fluffy twilight clouds on a transparent background (640x150, seamless horizontal wrap).
     * Uses multi-pass layered silhouettes so adjacent puffs merge smoothly into cohesive cloud banks.
     */
    public static Texture createCloudsTexture() {
        int w = LAYER_WIDTH;
        int h = 150;
        PixelArtCanvas canvas = new PixelArtCanvas(w, h);

        Color cloudShadow = new Color(0.20f, 0.17f, 0.36f, 0.92f);
        Color cloudMid = new Color(0.42f, 0.32f, 0.54f, 0.96f);
        Color cloudLight = new Color(0.74f, 0.56f, 0.68f, 0.98f);
        Color cloudRim = new Color(0.96f, 0.78f, 0.72f, 1.00f);

        int[][] clouds = new int[][] {
                { 42, 86, 26 }, { 74, 74, 36 }, { 112, 70, 40 }, { 148, 82, 28 },
                { 222, 66, 24 }, { 252, 54, 34 }, { 288, 52, 38 }, { 324, 62, 28 }, { 352, 72, 20 },
                { 438, 82, 28 }, { 474, 68, 40 }, { 516, 66, 42 }, { 556, 78, 30 }, { 584, 86, 22 },
                { 12, 92, 24 }, { 628, 92, 24 } // Seamless wrap pair across edges
        };

        // Pass 1: Deep twilight shadow base across all puffs so shapes fuse cleanly
        for (int i = 0; i < clouds.length; i++) {
            drawWrappedCloudPuff(canvas, w, clouds[i][0], clouds[i][1] + 5, clouds[i][2], cloudShadow);
        }
        // Pass 2: Mid-tone lavender body
        for (int i = 0; i < clouds.length; i++) {
            drawWrappedCloudPuff(canvas, w, clouds[i][0] - 1, clouds[i][1] + 1, (int) (clouds[i][2] * 0.92f), cloudMid);
        }
        // Pass 3: Soft warm twilight upper puffs
        for (int i = 0; i < clouds.length; i++) {
            drawWrappedCloudPuff(canvas, w, clouds[i][0] - 3, clouds[i][1] - 4, (int) (clouds[i][2] * 0.76f), cloudLight);
        }
        // Pass 4: Crisp sunlit coral-cream top highlight crests
        for (int i = 0; i < clouds.length; i++) {
            drawWrappedCloudPuff(canvas, w, clouds[i][0] - 5, clouds[i][1] - 9, (int) (clouds[i][2] * 0.48f), cloudRim);
        }

        // Flatten cloud bases cleanly for classic 16-bit OutRun cumulus silhouettes
        canvas.clearRect(0, 102, w, h - 102);

        return canvas.bakeTexture(true);
    }

    private static void drawWrappedCloudPuff(PixelArtCanvas canvas, int w, int cx, int cy, int r, Color color) {
        canvas.fillCircle(cx, cy, r, color);
        if (cx - r < 0) {
            canvas.fillCircle(cx + w, cy, r, color);
        }
        if (cx + r >= w) {
            canvas.fillCircle(cx - w, cy, r, color);
        }
    }

    /**
     * Generates the dark twilight city skyline (640x170, seamless horizontal wrap) with
     * skyscrapers, glowing pixel windows, a lattice tower crane, a rooftop water tower,
     * and dark tree silhouettes along the horizon base.
     */
    public static Texture createCitySkylineTexture() {
        int w = LAYER_WIDTH;
        int h = 170;
        PixelArtCanvas canvas = new PixelArtCanvas(w, h);

        Color farTower = new Color(0.12f, 0.11f, 0.22f, 1f);
        Color midTower = new Color(0.08f, 0.08f, 0.16f, 1f);
        Color nearTower = new Color(0.05f, 0.06f, 0.11f, 1f);
        Color rimHighlight = new Color(0.24f, 0.20f, 0.36f, 1f);
        Color winAmber = new Color(0.98f, 0.82f, 0.40f, 0.95f);
        Color winCyan = new Color(0.45f, 0.88f, 0.96f, 0.85f);
        Color beaconRed = new Color(0.98f, 0.22f, 0.20f, 1f);
        Color craneSteel = new Color(0.11f, 0.12f, 0.20f, 1f);
        Color treeDark = new Color(0.04f, 0.12f, 0.07f, 1f);
        Color treeMid = new Color(0.07f, 0.19f, 0.10f, 1f);

        Random rng = new Random(2049L);

        // Layer 1: Distant muted skyline silhouettes
        int[][] farBuildings = new int[][] {
                { 10, 62, 42 }, { 64, 48, 36 }, { 112, 74, 48 }, { 172, 40, 44 },
                { 230, 68, 52 }, { 296, 52, 40 }, { 348, 36, 46 }, { 408, 66, 50 },
                { 470, 44, 42 }, { 526, 60, 48 }, { 584, 50, 46 }
        };
        for (int i = 0; i < farBuildings.length; i++) {
            int bx = farBuildings[i][0];
            int by = farBuildings[i][1];
            int bw = farBuildings[i][2];
            canvas.fillRect(bx, by, bw, h - by, farTower);
            canvas.fillRect(bx, by, bw, 2, rimHighlight);
        }

        // Layer 2: Midground skyscrapers with glowing pixel windows & antennas
        int[][] buildings = new int[][] {
                { 0, 92, 38 }, { 42, 66, 34 }, { 82, 82, 46 }, { 142, 54, 40 },
                { 194, 78, 36 }, { 244, 46, 44 }, { 298, 86, 42 }, { 366, 62, 38 },
                { 412, 76, 44 }, { 466, 48, 36 }, { 514, 72, 46 }, { 570, 58, 38 },
                { 612, 88, 28 }
        };
        for (int i = 0; i < buildings.length; i++) {
            int bx = buildings[i][0];
            int by = buildings[i][1];
            int bw = buildings[i][2];
            Color body = (i % 2 == 0) ? midTower : nearTower;
            canvas.fillRect(bx, by, bw, h - by, body);
            canvas.fillRect(bx, by, 2, h - by, rimHighlight);

            // Spire / antenna on tall skyscrapers
            if (by < 65) {
                int spireX = bx + bw / 2;
                canvas.fillRect(spireX, by - 18, 2, 18, body);
                canvas.fillRect(spireX, by - 19, 2, 2, beaconRed);
            }

            // Lit 16-bit office windows
            for (int wy = by + 7; wy < h - 28; wy += 7) {
                for (int wx = bx + 5; wx < bx + bw - 6; wx += 6) {
                    float roll = rng.nextFloat();
                    if (roll < 0.30f) {
                        canvas.fillRect(wx, wy, 3, 4, winAmber);
                    } else if (roll < 0.42f) {
                        canvas.fillRect(wx, wy, 3, 4, winCyan);
                    }
                }
            }
        }

        // Signature landmark 1: Construction tower crane (around x = 338)
        int craneX = 338;
        int craneTopY = 30;
        // Vertical lattice mast
        canvas.fillRect(craneX, craneTopY, 2, h - craneTopY - 12, craneSteel);
        canvas.fillRect(craneX + 7, craneTopY, 2, h - craneTopY - 12, craneSteel);
        for (int y = craneTopY + 4; y < h - 18; y += 8) {
            canvas.drawLine(craneX, y, craneX + 8, y + 6, craneSteel);
            canvas.drawLine(craneX + 8, y, craneX, y + 6, craneSteel);
        }
        // Horizontal jib boom + counterweight + hanging hook cable
        canvas.fillRect(craneX - 24, craneTopY + 4, 82, 2, craneSteel);
        canvas.fillRect(craneX - 24, craneTopY + 8, 82, 2, craneSteel);
        canvas.drawLine(craneX + 4, craneTopY - 8, craneX - 22, craneTopY + 4, craneSteel);
        canvas.drawLine(craneX + 4, craneTopY - 8, craneX + 54, craneTopY + 4, craneSteel);
        canvas.fillRect(craneX - 24, craneTopY + 6, 10, 6, nearTower); // counterweight
        canvas.fillRect(craneX + 44, craneTopY + 10, 1, 24, craneSteel); // cable
        canvas.fillRect(craneX + 42, craneTopY + 34, 5, 3, rimHighlight); // hook block
        canvas.fillRect(craneX + 3, craneTopY - 10, 2, 2, beaconRed);

        // Signature landmark 2: Classic rooftop water tower on stilts (atop building around x = 194, y = 78)
        int wtX = 204;
        int wtBaseY = 78;
        // Stilts
        canvas.fillRect(wtX + 2, wtBaseY - 14, 2, 14, nearTower);
        canvas.fillRect(wtX + 12, wtBaseY - 14, 2, 14, nearTower);
        canvas.drawLine(wtX + 3, wtBaseY - 12, wtX + 12, wtBaseY - 2, nearTower);
        canvas.drawLine(wtX + 12, wtBaseY - 12, wtX + 3, wtBaseY - 2, nearTower);
        // Wooden barrel tank
        canvas.fillRect(wtX, wtBaseY - 28, 16, 14, midTower);
        canvas.fillRect(wtX, wtBaseY - 25, 16, 1, rimHighlight);
        canvas.fillRect(wtX, wtBaseY - 19, 16, 1, rimHighlight);
        // Conical roof
        for (int row = 0; row < 7; row++) {
            canvas.fillRect(wtX - 1 + row, wtBaseY - 29 - row, 18 - row * 2, 1, nearTower);
        }

        // Foreground dark twilight treeline along the skyline base (seamless wrap)
        for (int tx = 0; tx < w; tx += 14) {
            int radius = 10 + (int) (Math.sin(tx * 0.13) * 4 + 4);
            int ty = h - 10 - (radius / 3);
            drawWrappedCloudPuff(canvas, w, tx, ty, radius, treeMid);
            drawWrappedCloudPuff(canvas, w, tx + 5, ty + 4, (int) (radius * 0.85f), treeDark);
        }
        canvas.fillRect(0, h - 10, w, 10, treeDark);

        return canvas.bakeTexture(true);
    }
}
