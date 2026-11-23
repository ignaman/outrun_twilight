package com.retro.racer.ui;

import com.retro.racer.GameState;

/**
 * Immutable snapshot of telemetry metrics passed from {@link com.retro.racer.world.RaceWorld}
 * to {@link com.retro.racer.rendering.HudRenderer}.
 */
public final class HudMetrics {

    private final float speedKmh;
    private final float speedRatio;
    private final float lapTimeSeconds;
    private final float bestLapSeconds;
    private final float totalTimeSeconds;
    private final float distanceKm;
    private final int currentLap;
    private final int totalLaps;
    private final boolean offRoad;
    private final boolean muted;
    private final GameState gameState;

    public HudMetrics(
            float speedKmh,
            float speedRatio,
            float lapTimeSeconds,
            float bestLapSeconds,
            float totalTimeSeconds,
            float distanceKm,
            int currentLap,
            int totalLaps,
            boolean offRoad,
            boolean muted,
            GameState gameState
    ) {
        this.speedKmh = speedKmh;
        this.speedRatio = speedRatio;
        this.lapTimeSeconds = lapTimeSeconds;
        this.bestLapSeconds = bestLapSeconds;
        this.totalTimeSeconds = totalTimeSeconds;
        this.distanceKm = distanceKm;
        this.currentLap = currentLap;
        this.totalLaps = totalLaps;
        this.offRoad = offRoad;
        this.muted = muted;
        this.gameState = gameState;
    }

    public float getSpeedKmh() {
        return speedKmh;
    }

    public float getSpeedRatio() {
        return speedRatio;
    }

    public float getLapTimeSeconds() {
        return lapTimeSeconds;
    }

    public float getBestLapSeconds() {
        return bestLapSeconds;
    }

    public float getTotalTimeSeconds() {
        return totalTimeSeconds;
    }

    public float getDistanceKm() {
        return distanceKm;
    }

    public int getCurrentLap() {
        return currentLap;
    }

    public int getTotalLaps() {
        return totalLaps;
    }

    public boolean isOffRoad() {
        return offRoad;
    }

    public boolean isMuted() {
        return muted;
    }

    public GameState getGameState() {
        return gameState;
    }
}
