package com.retro.racer.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

import java.util.Map;

/**
 * Lightweight pixel-art drawing wrapper over libGDX {@link Pixmap} supporting
 * 2D character/color-matrix rendering, filled shapes, and nearest-neighbor texture baking.
 */
public final class PixelArtCanvas {

    private final Pixmap pixmap;
    private final int width;
    private final int height;

    public PixelArtCanvas(int width, int height) {
        this.width = width;
        this.height = height;
        this.pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        this.pixmap.setBlending(Pixmap.Blending.SourceOver);
        this.pixmap.setColor(0f, 0f, 0f, 0f);
        this.pixmap.fill();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setPixel(int x, int y, Color color) {
        if (color == null || x < 0 || y < 0 || x >= width || y >= height) {
            return;
        }
        pixmap.setColor(color);
        pixmap.drawPixel(x, y);
    }

    public void fillRect(int x, int y, int w, int h, Color color) {
        if (color == null || w <= 0 || h <= 0) {
            return;
        }
        pixmap.setColor(color);
        pixmap.fillRectangle(x, y, w, h);
    }

    /**
     * Erases pixels to full transparency regardless of alpha blending mode.
     */
    public void clearRect(int x, int y, int w, int h) {
        if (w <= 0 || h <= 0) {
            return;
        }
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fillRectangle(x, y, w, h);
        pixmap.setBlending(Pixmap.Blending.SourceOver);
    }

    public void fillCircle(int cx, int cy, int radius, Color color) {
        if (color == null || radius <= 0) {
            return;
        }
        pixmap.setColor(color);
        pixmap.fillCircle(cx, cy, radius);
    }

    public void drawLine(int x1, int y1, int x2, int y2, Color color) {
        if (color == null) {
            return;
        }
        pixmap.setColor(color);
        pixmap.drawLine(x1, y1, x2, y2);
    }

    /**
     * Paints a 2D ASCII pixel matrix onto this canvas at {@code (originX, originY)}
     * using the supplied character-to-color palette. Characters missing from the map
     * (e.g. '.' or ' ') remain transparent.
     */
    public void drawMatrix(int originX, int originY, String[] rows, Map<Character, Color> palette, boolean flipX) {
        if (rows == null || palette == null) {
            return;
        }
        for (int r = 0; r < rows.length; r++) {
            String row = rows[r];
            int len = row.length();
            for (int c = 0; c < len; c++) {
                char key = row.charAt(c);
                Color color = palette.get(key);
                if (color != null) {
                    int px = originX + (flipX ? (len - 1 - c) : c);
                    int py = originY + r;
                    setPixel(px, py, color);
                }
            }
        }
    }

    /**
     * Converts the underlying {@link Pixmap} into a crisp {@link TextureFilter#Nearest}
     * {@link Texture} and immediately disposes the temporary {@link Pixmap}.
     */
    public Texture bakeTexture(boolean repeatX) {
        Texture texture = new Texture(pixmap);
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        texture.setWrap(
                repeatX ? TextureWrap.Repeat : TextureWrap.ClampToEdge,
                TextureWrap.ClampToEdge
        );
        pixmap.dispose();
        return texture;
    }
}
