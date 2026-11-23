package com.retro.racer.entities;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Tall twilight highway streetlight positioned along the left or right road edge.
 */
public final class Streetlight extends RoadsideProp {

    private static final float COLLISION_WIDTH = 0.14f;
    private static final float WORLD_HEIGHT = 1850f;
    private static final float COLLISION_LENGTH = 120f;
    private static final float SPRITE_SCALE = 0.92f;

    private final boolean leftSide;

    public Streetlight(float z, float offsetX, boolean leftSide, TextureRegion sprite) {
        super(z, offsetX, COLLISION_WIDTH, WORLD_HEIGHT, COLLISION_LENGTH, SPRITE_SCALE, sprite, true);
        this.leftSide = leftSide;
    }

    public boolean isLeftSide() {
        return leftSide;
    }

    @Override
    public float getAnchorX() {
        // Anchor near the pole base so the lamp arm extends gracefully over the shoulder.
        return leftSide ? 0.22f : 0.78f;
    }
}
