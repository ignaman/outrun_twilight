package com.retro.racer.entities;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Abstract base class for static scenery props placed alongside road segments
 * (such as streetlights and directional chevron signs).
 */
public abstract class RoadsideProp extends RoadEntity {

    private final TextureRegion sprite;
    private final boolean solid;

    protected RoadsideProp(
            float z,
            float offsetX,
            float width,
            float height,
            float collisionLength,
            float spriteScale,
            TextureRegion sprite,
            boolean solid
    ) {
        super(z, offsetX, width, height, collisionLength, spriteScale);
        this.sprite = sprite;
        this.solid = solid;
    }

    @Override
    public void update(float delta) {
        // Static roadside props do not move per frame.
    }

    @Override
    public TextureRegion getSprite() {
        return sprite;
    }

    public boolean isSolid() {
        return solid;
    }
}
