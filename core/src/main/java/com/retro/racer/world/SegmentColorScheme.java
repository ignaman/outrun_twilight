package com.retro.racer.world;

import com.badlogic.gdx.graphics.Color;

/**
 * Immutable value object describing the color palette for a single road segment band
 * (grass shoulders, rumble strips, asphalt surface, and optional lane dashes).
 */
public final class SegmentColorScheme {

    /** Forest-green / light-asphalt stripe with red rumble strips and visible dashed lane markings. */
    public static final SegmentColorScheme LIGHT = new SegmentColorScheme(
            new Color(0.11f, 0.44f, 0.18f, 1f),
            new Color(0.86f, 0.14f, 0.14f, 1f),
            new Color(0.36f, 0.37f, 0.41f, 1f),
            new Color(0.92f, 0.94f, 0.96f, 1f),
            false
    );

    /** Darker twilight-green / deep-asphalt stripe with white rumble strips and no lane dash. */
    public static final SegmentColorScheme DARK = new SegmentColorScheme(
            new Color(0.08f, 0.34f, 0.14f, 1f),
            new Color(0.94f, 0.94f, 0.96f, 1f),
            new Color(0.30f, 0.31f, 0.35f, 1f),
            null,
            false
    );

    /** Checkered start/finish line segment palette. */
    public static final SegmentColorScheme START_FINISH = new SegmentColorScheme(
            new Color(0.11f, 0.44f, 0.18f, 1f),
            new Color(0.95f, 0.95f, 0.98f, 1f),
            new Color(0.92f, 0.92f, 0.95f, 1f),
            null,
            true
    );

    private final Color grass;
    private final Color rumble;
    private final Color road;
    private final Color lane;
    private final boolean startFinish;

    public SegmentColorScheme(Color grass, Color rumble, Color road, Color lane, boolean startFinish) {
        this.grass = new Color(grass);
        this.rumble = new Color(rumble);
        this.road = new Color(road);
        this.lane = lane == null ? null : new Color(lane);
        this.startFinish = startFinish;
    }

    public Color getGrass() {
        return grass;
    }

    public Color getRumble() {
        return rumble;
    }

    public Color getRoad() {
        return road;
    }

    public Color getLane() {
        return lane;
    }

    public boolean hasLaneMarkings() {
        return lane != null;
    }

    public boolean isStartFinish() {
        return startFinish;
    }
}
