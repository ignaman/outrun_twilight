package com.retro.racer.entities;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.retro.racer.graphics.AssetFactory;

/**
 * Modular animation component composed by {@link PlayerCar}.
 * Selects the current {@link TextureRegion} from {@link AssetFactory} based on
 * {@link CarSpriteState} and calculates suspension vibration & off-road bounce offsets.
 */
public final class CarAnimator {

    private final AssetFactory assets;
    private float bouncePhase;
    private float verticalBouncePx;
    private float horizontalShakePx;

    public CarAnimator(AssetFactory assets) {
        this.assets = assets;
        reset();
    }

    public void reset() {
        this.bouncePhase = 0f;
        this.verticalBouncePx = 0f;
        this.horizontalShakePx = 0f;
    }

    /**
     * Advances subtle engine/road vibration and stronger off-road grass rumble bounce.
     */
    public void update(float delta, float speedRatio, boolean offRoad) {
        float freq = 14f + speedRatio * 34f;
        bouncePhase += delta * freq;

        if (speedRatio < 0.02f) {
            // Subtle idle engine purr
            verticalBouncePx = (float) Math.sin(bouncePhase * 0.5f) * 0.45f;
            horizontalShakePx = 0f;
        } else if (offRoad) {
            verticalBouncePx = (float) Math.sin(bouncePhase * 1.85f) * (2.6f * speedRatio + 0.9f);
            horizontalShakePx = (float) Math.cos(bouncePhase * 2.4f) * (1.8f * speedRatio);
        } else {
            verticalBouncePx = (float) Math.sin(bouncePhase) * (1.15f * speedRatio);
            horizontalShakePx = 0f;
        }
    }

    public TextureRegion getSpriteFor(CarSpriteState state) {
        return assets.getPlayerCarSprite(state);
    }

    public float getVerticalBouncePx() {
        return verticalBouncePx;
    }

    public float getHorizontalShakePx() {
        return horizontalShakePx;
    }
}
