package com.retro.racer.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.retro.racer.GameState;
import com.retro.racer.graphics.AssetFactory;
import com.retro.racer.ui.HudMetrics;
import com.retro.racer.ui.OverlayBanner;
import com.retro.racer.ui.TimeFormatter;

/**
 * Renders the arcade HUD overlay in the {@code 640x480} virtual coordinate space:
 * the semi-transparent top-left rounded telemetry counter box with cyan metrics
 * (Speed, Lap Time, Distance, Lap), top-right best-lap badge, and centered
 * {@link GameState} overlays (including {@code "PRESS ENTER TO RACE"}).
 */
public final class HudRenderer implements Disposable {

    private static final Color PANEL_BG = new Color(0.03f, 0.06f, 0.14f, 0.78f);
    private static final Color PANEL_BORDER = new Color(0.22f, 0.92f, 1.00f, 0.92f);
    private static final Color CYAN_TEXT = new Color(0.26f, 0.96f, 1.00f, 1f);
    private static final Color CYAN_LABEL = new Color(0.60f, 0.90f, 0.98f, 0.95f);
    private static final Color GOLD_TITLE = new Color(1.00f, 0.85f, 0.24f, 1f);
    private static final Color WARN_ORANGE = new Color(1.00f, 0.42f, 0.20f, 1f);
    private static final Color BAR_BG = new Color(0.10f, 0.16f, 0.28f, 0.90f);
    private static final Color PAUSE_DIM = new Color(0.02f, 0.03f, 0.09f, 0.42f);

    private final AssetFactory assets;
    private final OrthographicCamera camera;
    private final FitViewport viewport;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch spriteBatch;
    private final GlyphLayout glyphLayout;
    private final OverlayBanner overlayBanner;

    public HudRenderer(AssetFactory assets) {
        this.assets = assets;
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(WorldRenderer.VIRTUAL_WIDTH, WorldRenderer.VIRTUAL_HEIGHT, camera);
        this.camera.position.set(WorldRenderer.VIRTUAL_WIDTH * 0.5f, WorldRenderer.VIRTUAL_HEIGHT * 0.5f, 0f);
        this.camera.update();

        this.shapeRenderer = new ShapeRenderer();
        this.spriteBatch = new SpriteBatch();
        this.glyphLayout = new GlyphLayout();
        this.overlayBanner = new OverlayBanner();
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    public void update(float delta) {
        overlayBanner.update(delta);
    }

    public void render(HudMetrics metrics) {
        viewport.apply();
        camera.update();

        if (metrics.getGameState().isOverlayVisible()) {
            drawStateOverlay(metrics);
        }

        // Draw HUD panels on top so telemetry remains crisp and vibrant in every state
        drawTopLeftTelemetryBox(metrics);
        drawTopRightInfoBox(metrics);
    }

    private void drawTopLeftTelemetryBox(HudMetrics metrics) {
        float boxX = 14f;
        float boxW = 212f;
        float boxH = 104f;
        float boxY = WorldRenderer.VIRTUAL_HEIGHT - boxH - 14f;
        float radius = 10f;

        drawRoundedPanel(boxX, boxY, boxW, boxH, radius);

        // Segmented retro speed gauge bar inside the box
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        float barX = boxX + 12f;
        float barY = boxY + 67f;
        float barW = boxW - 24f;
        float barH = 8f;
        shapeRenderer.setColor(BAR_BG);
        shapeRenderer.rect(barX, barY, barW, barH);

        int segments = 18;
        float segGap = 2f;
        float segW = (barW - segGap * (segments - 1)) / segments;
        int litSegments = Math.round(metrics.getSpeedRatio() * segments);
        for (int i = 0; i < litSegments; i++) {
            if (i >= 14) {
                shapeRenderer.setColor(1.0f, 0.24f, 0.20f, 1f);
            } else if (i >= 10) {
                shapeRenderer.setColor(1.0f, 0.80f, 0.20f, 1f);
            } else {
                shapeRenderer.setColor(CYAN_TEXT);
            }
            shapeRenderer.rect(barX + i * (segW + segGap), barY, segW, barH);
        }
        shapeRenderer.end();

        // Cyan BitmapFont metrics
        BitmapFont font = assets.getHudFont();
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        float textX = boxX + 12f;
        font.setColor(CYAN_LABEL);
        font.draw(spriteBatch, "SPEED", textX, boxY + 95f);
        font.setColor(CYAN_TEXT);
        font.draw(spriteBatch, TimeFormatter.formatSpeedKmh(metrics.getSpeedKmh()), textX + 62f, boxY + 95f);

        font.setColor(CYAN_LABEL);
        font.draw(spriteBatch, "TIME", textX, boxY + 56f);
        font.setColor(CYAN_TEXT);
        font.draw(spriteBatch, TimeFormatter.formatLapTime(metrics.getLapTimeSeconds()), textX + 62f, boxY + 56f);

        font.setColor(CYAN_LABEL);
        font.draw(spriteBatch, "DIST", textX, boxY + 37f);
        font.setColor(CYAN_TEXT);
        font.draw(spriteBatch, TimeFormatter.formatDistanceKm(metrics.getDistanceKm()), textX + 62f, boxY + 37f);

        font.setColor(CYAN_LABEL);
        font.draw(spriteBatch, "LAP", textX, boxY + 18f);
        font.setColor(GOLD_TITLE);
        font.draw(spriteBatch, metrics.getCurrentLap() + " / " + metrics.getTotalLaps(), textX + 62f, boxY + 18f);

        if (metrics.isOffRoad() && metrics.getGameState() == GameState.RACING && metrics.getSpeedKmh() > 15f) {
            font.setColor(WARN_ORANGE);
            font.draw(spriteBatch, "OFF-ROAD!", boxX + 120f, boxY + 18f);
        }

        spriteBatch.end();
    }

    private void drawTopRightInfoBox(HudMetrics metrics) {
        float boxW = 168f;
        float boxH = 54f;
        float boxX = WorldRenderer.VIRTUAL_WIDTH - boxW - 14f;
        float boxY = WorldRenderer.VIRTUAL_HEIGHT - boxH - 14f;

        drawRoundedPanel(boxX, boxY, boxW, boxH, 9f);

        BitmapFont font = assets.getHudFont();
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        font.setColor(CYAN_LABEL);
        font.draw(spriteBatch, "BEST", boxX + 12f, boxY + 44f);
        font.setColor(CYAN_TEXT);
        font.draw(spriteBatch, TimeFormatter.formatLapTime(metrics.getBestLapSeconds()), boxX + 58f, boxY + 44f);

        font.setColor(metrics.isMuted() ? WARN_ORANGE : CYAN_LABEL);
        font.draw(spriteBatch, metrics.isMuted() ? "AUDIO: MUTED (M)" : "AUDIO: ON (M)", boxX + 12f, boxY + 21f);
        spriteBatch.end();
    }

    private void drawStateOverlay(HudMetrics metrics) {
        GameState state = metrics.getGameState();

        // Keep the START screen skyline & car crisp and vibrant; subtly dim only on PAUSED / FINISHED
        if (state == GameState.PAUSED || state == GameState.FINISHED) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(PAUSE_DIM);
            shapeRenderer.rect(0f, 0f, WorldRenderer.VIRTUAL_WIDTH, WorldRenderer.VIRTUAL_HEIGHT);
            shapeRenderer.end();
        }

        float panelW = 472f;
        float panelH = state == GameState.FINISHED ? 168f : 146f;
        float panelX = (WorldRenderer.VIRTUAL_WIDTH - panelW) * 0.5f;
        float panelY = (WorldRenderer.VIRTUAL_HEIGHT - panelH) * 0.5f + 26f;

        drawRoundedPanel(panelX, panelY, panelW, panelH, 12f);

        BitmapFont titleFont = assets.getTitleFont();
        BitmapFont hudFont = assets.getHudFont();

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        // Retro drop-shadow title
        String title = overlayBanner.getTitle(state);
        float titleY = panelY + panelH - 22f;
        drawCenteredText(titleFont, title, WorldRenderer.VIRTUAL_WIDTH * 0.5f + 2f, titleY - 2f, Color.BLACK);
        drawCenteredText(titleFont, title, WorldRenderer.VIRTUAL_WIDTH * 0.5f, titleY, GOLD_TITLE);

        // Finished-race summary stats
        if (state == GameState.FINISHED) {
            String summary = "TOTAL: " + TimeFormatter.formatLapTime(metrics.getTotalTimeSeconds())
                    + "   BEST LAP: " + TimeFormatter.formatLapTime(metrics.getBestLapSeconds());
            drawCenteredText(hudFont, summary, WorldRenderer.VIRTUAL_WIDTH * 0.5f, panelY + 86f, CYAN_TEXT);
        }

        // Blinking arcade call-to-action ("PRESS ENTER TO RACE", etc.)
        if (overlayBanner.isPromptVisible()) {
            String subtitle = overlayBanner.getSubtitle(state);
            float subY = state == GameState.FINISHED ? panelY + 54f : panelY + 68f;
            drawCenteredText(hudFont, subtitle, WorldRenderer.VIRTUAL_WIDTH * 0.5f, subY, CYAN_TEXT);
        }

        // Controls hint bar at bottom of panel
        hudFont.getData().setScale(0.84f);
        drawCenteredText(
                hudFont,
                overlayBanner.getControlsHint(),
                WorldRenderer.VIRTUAL_WIDTH * 0.5f,
                panelY + 24f,
                CYAN_LABEL
        );
        hudFont.getData().setScale(1.05f);

        spriteBatch.end();
    }

    private void drawCenteredText(BitmapFont font, String text, float centerX, float y, Color color) {
        font.setColor(color);
        glyphLayout.setText(font, text);
        font.draw(spriteBatch, glyphLayout, Math.round(centerX - glyphLayout.width * 0.5f), Math.round(y));
    }

    /**
     * Draws a non-overlapping translucent rounded panel with a crisp 1px cyan outline
     * so alpha blending stays completely uniform across corners and edges.
     */
    private void drawRoundedPanel(float x, float y, float w, float h, float r) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(PANEL_BG);

        // Non-overlapping interior strips
        shapeRenderer.rect(x + r, y, w - 2f * r, h);
        shapeRenderer.rect(x, y + r, r, h - 2f * r);
        shapeRenderer.rect(x + w - r, y + r, r, h - 2f * r);

        // Four non-overlapping quarter-circle corners
        shapeRenderer.arc(x + r, y + r, r, 180f, 90f, 10);
        shapeRenderer.arc(x + w - r, y + r, r, 270f, 90f, 10);
        shapeRenderer.arc(x + w - r, y + h - r, r, 0f, 90f, 10);
        shapeRenderer.arc(x + r, y + h - r, r, 90f, 90f, 10);
        shapeRenderer.end();

        // Crisp glowing cyan perimeter outline
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(PANEL_BORDER);
        shapeRenderer.line(x + r, y, x + w - r, y);
        shapeRenderer.line(x + r, y + h, x + w - r, y + h);
        shapeRenderer.line(x, y + r, x, y + h - r);
        shapeRenderer.line(x + w, y + r, x + w, y + h - r);
        drawCornerOutlineArc(x + r, y + r, r, 180f, 270f);
        drawCornerOutlineArc(x + w - r, y + r, r, 270f, 360f);
        drawCornerOutlineArc(x + w - r, y + h - r, r, 0f, 90f);
        drawCornerOutlineArc(x + r, y + h - r, r, 90f, 180f);
        shapeRenderer.end();
    }

    private void drawCornerOutlineArc(float cx, float cy, float r, float startDeg, float endDeg) {
        int steps = 8;
        float step = (endDeg - startDeg) / steps;
        for (int i = 0; i < steps; i++) {
            double a1 = Math.toRadians(startDeg + i * step);
            double a2 = Math.toRadians(startDeg + (i + 1) * step);
            shapeRenderer.line(
                    cx + (float) Math.cos(a1) * r,
                    cy + (float) Math.sin(a1) * r,
                    cx + (float) Math.cos(a2) * r,
                    cy + (float) Math.sin(a2) * r
            );
        }
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
    }
}
