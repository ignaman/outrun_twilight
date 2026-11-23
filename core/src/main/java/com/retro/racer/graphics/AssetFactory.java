package com.retro.racer.graphics;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.retro.racer.entities.CarSpriteState;

/**
 * Factory interface for retrieving game sprites, parallax layers, and fonts.
 * Implementations manage native libGDX resources and must release them in {@link #dispose()}.
 */
public interface AssetFactory extends Disposable {

    TextureRegion getPlayerCarSprite(CarSpriteState state);

    TextureRegion getTrafficCarSprite(int variantIndex);

    int getTrafficVariantCount();

    TextureRegion getStreetlightSprite(boolean leftSide);

    TextureRegion getChevronSignSprite(boolean pointRight);

    Texture getSkyTexture();

    Texture getCloudsTexture();

    Texture getSkylineTexture();

    BitmapFont getHudFont();

    BitmapFont getTitleFont();
}
