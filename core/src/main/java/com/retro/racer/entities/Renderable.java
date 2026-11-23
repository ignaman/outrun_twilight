package com.retro.racer.entities;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Common polymorphic rendering contract for all world-positioned sprites
 * (roadside props, AI traffic cars, and the player car).
 */
public interface Renderable {

    TextureRegion getSprite();

    float getZ();

    float getOffsetX();

    float getWidth();

    float getHeight();

    /**
     * Multiplier converting sprite pixel dimensions into world-projected scale.
     */
    float getSpriteScale();

    /**
     * Horizontal anchor in [0, 1] relative to sprite width (0.5 = centered, 0 = left, 1 = right).
     */
    float getAnchorX();

    boolean isFlipX();
}
