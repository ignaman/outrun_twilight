package com.retro.racer.graphics;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.retro.racer.entities.CarSpriteState;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Procedural asset factory implementing {@link AssetFactory} and {@link com.badlogic.gdx.utils.Disposable}.
 * Generates all 16-bit pixel-art sprites, parallax twilight layers, and HUD fonts in memory
 * with zero external file dependencies, and cleanly disposes every native resource on exit.
 */
public final class ProceduralTextureFactory implements AssetFactory {

    private static final int TRAFFIC_VARIANTS = 4;

    private final List<Texture> managedTextures = new ArrayList<Texture>();
    private final Map<CarSpriteState, TextureRegion> playerCarSprites =
            new EnumMap<CarSpriteState, TextureRegion>(CarSpriteState.class);
    private final List<TextureRegion> trafficCarSprites = new ArrayList<TextureRegion>();

    private final TextureRegion leftStreetlight;
    private final TextureRegion rightStreetlight;
    private final TextureRegion leftChevronSign;
    private final TextureRegion rightChevronSign;

    private final Texture skyTexture;
    private final Texture cloudsTexture;
    private final Texture skylineTexture;

    private final BitmapFont hudFont;
    private final BitmapFont titleFont;

    public ProceduralTextureFactory() {
        // Generate all player sports-car states (straight/left/right x flat/uphill x braking)
        CarSpriteState[] states = CarSpriteState.values();
        for (int i = 0; i < states.length; i++) {
            CarSpriteState state = states[i];
            Texture tex = register(CarSpriteGenerator.createPlayerCarTexture(state));
            playerCarSprites.put(state, new TextureRegion(tex));
        }

        // Generate AI traffic vehicles
        for (int i = 0; i < TRAFFIC_VARIANTS; i++) {
            Texture tex = register(CarSpriteGenerator.createTrafficCarTexture(i));
            trafficCarSprites.add(new TextureRegion(tex));
        }

        // Generate roadside props
        this.leftStreetlight = new TextureRegion(register(PropSpriteGenerator.createStreetlightTexture(true)));
        this.rightStreetlight = new TextureRegion(register(PropSpriteGenerator.createStreetlightTexture(false)));
        this.leftChevronSign = new TextureRegion(register(PropSpriteGenerator.createChevronSignTexture(false)));
        this.rightChevronSign = new TextureRegion(register(PropSpriteGenerator.createChevronSignTexture(true)));

        // Generate seamless twilight parallax layers
        this.skyTexture = register(ParallaxLayerGenerator.createTwilightSkyTexture());
        this.cloudsTexture = register(ParallaxLayerGenerator.createCloudsTexture());
        this.skylineTexture = register(ParallaxLayerGenerator.createCitySkylineTexture());

        // Built-in libGDX BitmapFont (zero external assets) configured for crisp retro pixel look
        this.hudFont = new BitmapFont();
        this.hudFont.getRegion().getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        this.hudFont.getData().setScale(1.05f);
        this.hudFont.setUseIntegerPositions(true);

        this.titleFont = new BitmapFont();
        this.titleFont.getRegion().getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        this.titleFont.getData().setScale(2.0f);
        this.titleFont.setUseIntegerPositions(true);
    }

    private Texture register(Texture texture) {
        managedTextures.add(texture);
        return texture;
    }

    @Override
    public TextureRegion getPlayerCarSprite(CarSpriteState state) {
        TextureRegion region = playerCarSprites.get(state);
        return region != null ? region : playerCarSprites.get(CarSpriteState.STRAIGHT);
    }

    @Override
    public TextureRegion getTrafficCarSprite(int variantIndex) {
        int idx = ((variantIndex % trafficCarSprites.size()) + trafficCarSprites.size()) % trafficCarSprites.size();
        return trafficCarSprites.get(idx);
    }

    @Override
    public int getTrafficVariantCount() {
        return trafficCarSprites.size();
    }

    @Override
    public TextureRegion getStreetlightSprite(boolean leftSide) {
        return leftSide ? leftStreetlight : rightStreetlight;
    }

    @Override
    public TextureRegion getChevronSignSprite(boolean pointRight) {
        return pointRight ? rightChevronSign : leftChevronSign;
    }

    @Override
    public Texture getSkyTexture() {
        return skyTexture;
    }

    @Override
    public Texture getCloudsTexture() {
        return cloudsTexture;
    }

    @Override
    public Texture getSkylineTexture() {
        return skylineTexture;
    }

    @Override
    public BitmapFont getHudFont() {
        return hudFont;
    }

    @Override
    public BitmapFont getTitleFont() {
        return titleFont;
    }

    @Override
    public void dispose() {
        for (int i = 0; i < managedTextures.size(); i++) {
            managedTextures.get(i).dispose();
        }
        managedTextures.clear();
        hudFont.dispose();
        titleFont.dispose();
    }
}
