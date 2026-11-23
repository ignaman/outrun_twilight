package com.retro.racer.entities;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Red-and-white reflective chevron warning sign placed along the outer edge of sharp curves.
 */
public final class ChevronSign extends RoadsideProp {

    public enum Direction {
        LEFT,
        RIGHT
    }

    private static final float COLLISION_WIDTH = 0.24f;
    private static final float WORLD_HEIGHT = 760f;
    private static final float COLLISION_LENGTH = 110f;
    private static final float SPRITE_SCALE = 0.72f;

    private final Direction direction;

    public ChevronSign(float z, float offsetX, Direction direction, TextureRegion sprite) {
        super(z, offsetX, COLLISION_WIDTH, WORLD_HEIGHT, COLLISION_LENGTH, SPRITE_SCALE, sprite, true);
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }
}
