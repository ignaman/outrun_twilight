package com.retro.racer.entities;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Abstract base class for all entities positioned along the pseudo-3D track.
 * Provides shared world coordinates (z, normalized lateral offsetX, collision width/height)
 * and axis-aligned bounding-box collision queries.
 */
public abstract class RoadEntity implements Renderable {

    private float z;
    private float offsetX;
    private float width;
    private float height;
    private float collisionLength;
    private float spriteScale;

    protected RoadEntity(float z, float offsetX, float width, float height, float collisionLength, float spriteScale) {
        this.z = z;
        this.offsetX = offsetX;
        this.width = width;
        this.height = height;
        this.collisionLength = collisionLength;
        this.spriteScale = spriteScale;
    }

    /**
     * Updates entity simulation state by {@code delta} seconds.
     */
    public abstract void update(float delta);

    @Override
    public abstract TextureRegion getSprite();

    /**
     * Checks 1D lateral overlap between this entity and another entity in normalized road-width units.
     */
    public boolean overlapsX(RoadEntity other, float paddingFactor) {
        if (other == null) {
            return false;
        }
        float halfA = this.width * 0.5f * paddingFactor;
        float halfB = other.width * 0.5f * paddingFactor;
        float minA = this.offsetX - halfA;
        float maxA = this.offsetX + halfA;
        float minB = other.offsetX - halfB;
        float maxB = other.offsetX + halfB;
        return maxA >= minB && minA <= maxB;
    }

    /**
     * Checks longitudinal Z overlap accounting for track wrap-around.
     */
    public boolean overlapsZ(RoadEntity other, float trackLength) {
        if (other == null) {
            return false;
        }
        float dz = Math.abs(this.z - other.z);
        if (trackLength > 0f) {
            dz = Math.min(dz, trackLength - dz);
        }
        return dz <= (this.collisionLength + other.collisionLength) * 0.5f;
    }

    /**
     * Full 2D track-plane bounding-box collision test (X and Z).
     */
    public boolean collidesWith(RoadEntity other, float trackLength, float xPaddingFactor) {
        return overlapsZ(other, trackLength) && overlapsX(other, xPaddingFactor);
    }

    @Override
    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    @Override
    public float getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    @Override
    public float getWidth() {
        return width;
    }

    protected void setWidth(float width) {
        this.width = width;
    }

    @Override
    public float getHeight() {
        return height;
    }

    protected void setHeight(float height) {
        this.height = height;
    }

    public float getCollisionLength() {
        return collisionLength;
    }

    @Override
    public float getSpriteScale() {
        return spriteScale;
    }

    @Override
    public float getAnchorX() {
        return 0.5f;
    }

    @Override
    public boolean isFlipX() {
        return false;
    }
}
