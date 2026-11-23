package com.retro.racer.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.retro.racer.entities.PlayerCar;
import com.retro.racer.entities.RoadEntity;
import com.retro.racer.entities.TrafficCar;
import com.retro.racer.graphics.AssetFactory;
import com.retro.racer.world.ProjectedPoint;
import com.retro.racer.world.ProjectionMath;
import com.retro.racer.world.RaceWorld;
import com.retro.racer.world.RoadSegment;
import com.retro.racer.world.SegmentColorScheme;
import com.retro.racer.world.Track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Renders the 2.5D OutRun-style racing world within a crisp {@code 640x480} {@link FitViewport}:
 * twilight parallax sky/clouds/city skyline, back-to-front scanline road trapezoids,
 * depth-sorted polymorphic {@link RoadEntity} sprites with hill-crest clipping,
 * and the player's red sports car.
 */
public final class WorldRenderer implements Disposable {

    public static final int VIRTUAL_WIDTH = 640;
    public static final int VIRTUAL_HEIGHT = 480;

    private static final float HORIZON_Y = VIRTUAL_HEIGHT * 0.5f;
    private static final float PLAYER_DRAW_SCALE = 2.0f;
    private static final float PLAYER_BASE_Y = 20f;
    private static final float FOG_DENSITY = 1.75f;

    private static final Color TWILIGHT_CLEAR = new Color(0.03f, 0.05f, 0.14f, 1f);
    private static final Color DISTANT_GROUND = new Color(0.08f, 0.30f, 0.14f, 1f);
    private static final Color TWILIGHT_FOG = new Color(0.24f, 0.18f, 0.34f, 1f);
    private static final Color CHECKER_DARK = new Color(0.10f, 0.10f, 0.13f, 1f);
    private static final Color CHECKER_LIGHT = new Color(0.95f, 0.95f, 0.98f, 1f);

    private final AssetFactory assets;
    private final OrthographicCamera camera;
    private final FitViewport viewport;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch spriteBatch;

    private final List<RoadEntity> segmentEntityBuffer = new ArrayList<RoadEntity>();
    private final Color workColor = new Color();

    private final Comparator<RoadEntity> depthComparator = new Comparator<RoadEntity>() {
        @Override
        public int compare(RoadEntity a, RoadEntity b) {
            return Float.compare(b.getZ(), a.getZ());
        }
    };

    public WorldRenderer(AssetFactory assets) {
        this.assets = assets;
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        this.camera.position.set(VIRTUAL_WIDTH * 0.5f, VIRTUAL_HEIGHT * 0.5f, 0f);
        this.camera.update();

        this.shapeRenderer = new ShapeRenderer();
        this.spriteBatch = new SpriteBatch();
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    public Viewport getViewport() {
        return viewport;
    }

    public void render(RaceWorld world) {
        ScreenUtils.clear(TWILIGHT_CLEAR);
        viewport.apply();
        camera.update();

        Track track = world.getTrack();
        PlayerCar player = world.getPlayerCar();

        float cameraZ = world.getCameraZ();
        float segLen = track.getSegmentLength();
        float roadWidth = track.getRoadWidth();
        int totalSegments = track.getSegmentCount();

        RoadSegment baseSegment = track.findSegment(cameraZ);
        float basePercent = ProjectionMath.percentRemaining(cameraZ, segLen);

        float playerWorldZ = track.wrapZ(cameraZ + world.getPlayerZOffset());
        RoadSegment playerSegment = track.findSegment(playerWorldZ);
        float playerPercent = ProjectionMath.percentRemaining(playerWorldZ, segLen);
        float playerWorldY = ProjectionMath.lerp(
                playerSegment.getP1().getWorldY(),
                playerSegment.getP2().getWorldY(),
                playerPercent
        );

        // Pass 1 (front-to-back): Project visible road segments and track hill-crest maxY clip horizon
        float camBaseX = player.getOffsetX() * roadWidth + world.getCameraJitterX() * 14f;
        float camBaseY = playerWorldY + RaceWorld.CAMERA_HEIGHT + world.getCameraJitterY() * 10f;

        float x = 0f;
        float dx = -(baseSegment.getCurve() * basePercent);
        float maxY = 0f;
        int drawDistance = Math.min(RaceWorld.DRAW_DISTANCE, totalSegments - 2);

        for (int n = 0; n < drawDistance; n++) {
            RoadSegment seg = track.getSegment(baseSegment.getIndex() + n);
            boolean looped = seg.getIndex() < baseSegment.getIndex();
            float zOffset = looped ? track.getTotalLength() : 0f;

            ProjectionMath.project(
                    seg.getP1(),
                    camBaseX - x,
                    camBaseY,
                    cameraZ - zOffset,
                    RaceWorld.CAMERA_DEPTH,
                    VIRTUAL_WIDTH,
                    VIRTUAL_HEIGHT,
                    roadWidth
            );
            ProjectionMath.project(
                    seg.getP2(),
                    camBaseX - x - dx,
                    camBaseY,
                    cameraZ - zOffset,
                    RaceWorld.CAMERA_DEPTH,
                    VIRTUAL_WIDTH,
                    VIRTUAL_HEIGHT,
                    roadWidth
            );

            x += dx;
            dx += seg.getCurve();

            seg.setClipY(maxY);
            float distRatio = (float) n / drawDistance;
            seg.setFogFactor(ProjectionMath.exponentialFog(distRatio, FOG_DENSITY));

            ProjectedPoint p1 = seg.getP1();
            ProjectedPoint p2 = seg.getP2();

            boolean inFront = p1.getCamZ() > RaceWorld.CAMERA_DEPTH && p2.getCamZ() > RaceWorld.CAMERA_DEPTH;
            boolean forwardFacing = p2.getScreenY() > p1.getScreenY();
            boolean aboveClip = p2.getScreenY() > maxY;

            if (inFront && forwardFacing && aboveClip) {
                seg.setVisible(true);
                maxY = p2.getScreenY();
            } else {
                seg.setVisible(false);
            }
        }

        // Step 1: Draw distant ground fill & twilight parallax sky/clouds/city skyline
        float hillParallaxShift = ProjectionMath.clamp(-playerSegment.getSlope() * 0.14f, -26f, 22f);
        drawBackdropAndParallax(world, hillParallaxShift, maxY);

        // Step 2: Draw 2.5D road trapezoids back-to-front with ShapeRenderer
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int n = drawDistance - 1; n >= 0; n--) {
            RoadSegment seg = track.getSegment(baseSegment.getIndex() + n);
            if (!seg.isVisible()) {
                continue;
            }
            drawRoadSegment(seg, track.getLanes());
        }
        shapeRenderer.end();

        // Step 3: Draw depth-sorted polymorphic RoadEntity sprites back-to-front with hill-crest clipping
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        for (int n = drawDistance - 1; n >= 1; n--) {
            RoadSegment seg = track.getSegment(baseSegment.getIndex() + n);
            world.collectEntitiesInSegment(seg, segmentEntityBuffer);
            if (segmentEntityBuffer.isEmpty()) {
                continue;
            }
            if (segmentEntityBuffer.size() > 1) {
                Collections.sort(segmentEntityBuffer, depthComparator);
            }
            for (int i = 0; i < segmentEntityBuffer.size(); i++) {
                drawProjectedEntity(segmentEntityBuffer.get(i), seg, segLen);
            }
        }

        // Step 4: Draw the player's 16-bit red sports car in the foreground
        drawPlayerCar(player, world.getCameraJitterX());
        spriteBatch.end();
    }

    private void drawBackdropAndParallax(RaceWorld world, float hillShiftY, float maxRoadY) {
        // Fill lower half with distant twilight forest ground so downhill valleys never reveal gaps
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(DISTANT_GROUND);
        shapeRenderer.rect(0f, 0f, VIRTUAL_WIDTH, Math.max(HORIZON_Y + 24f, maxRoadY + 4f));
        shapeRenderer.end();

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        spriteBatch.setColor(Color.WHITE);

        // Layer 1: Starry deep twilight sky
        Texture sky = assets.getSkyTexture();
        float skyU = world.getSkyOffset() / sky.getWidth();
        float skyY = HORIZON_Y - 12f + hillShiftY * 0.25f;
        spriteBatch.draw(sky, 0f, skyY, VIRTUAL_WIDTH, VIRTUAL_HEIGHT - skyY + 18f,
                skyU, 1f, skyU + 1f, 0f);

        // Layer 2: Fluffy stylized twilight clouds
        Texture clouds = assets.getCloudsTexture();
        float cloudU = world.getCloudOffset() / clouds.getWidth();
        float cloudY = HORIZON_Y + 62f + hillShiftY * 0.45f;
        spriteBatch.draw(clouds, 0f, cloudY, VIRTUAL_WIDTH, clouds.getHeight(),
                cloudU, 1f, cloudU + 1f, 0f);

        // Layer 3: Dark city skyline with skyscrapers, crane, water tower & horizon trees
        Texture skyline = assets.getSkylineTexture();
        float cityU = world.getCityOffset() / skyline.getWidth();
        float cityY = HORIZON_Y - 18f + hillShiftY * 0.75f;
        spriteBatch.draw(skyline, 0f, cityY, VIRTUAL_WIDTH, skyline.getHeight(),
                cityU, 1f, cityU + 1f, 0f);

        spriteBatch.end();
    }

    private void drawRoadSegment(RoadSegment seg, int lanes) {
        ProjectedPoint p1 = seg.getP1();
        ProjectedPoint p2 = seg.getP2();

        float x1 = p1.getScreenX();
        float y1 = p1.getScreenY();
        float w1 = p1.getScreenW();

        float x2 = p2.getScreenX();
        float y2 = p2.getScreenY();
        float w2 = p2.getScreenW();

        // Pad y2 slightly (+0.6f) to prevent sub-pixel horizontal hairline cracks between adjacent bands
        float y2Padded = y2 + 0.6f;

        SegmentColorScheme scheme = seg.getColorScheme();
        float fog = seg.getFogFactor();

        // 1. Alternating forest-green grass band spanning full viewport width
        applyFogColor(scheme.getGrass(), fog);
        shapeRenderer.rect(0f, y1, VIRTUAL_WIDTH, y2Padded - y1);

        // 2. Red-and-white alternating rumble strips along both asphalt edges
        float r1 = w1 / 6.5f;
        float r2 = w2 / 6.5f;
        applyFogColor(scheme.getRumble(), fog);
        drawTrapezoid(x1 - w1 - r1, y1, x1 - w1, y1, x2 - w2, y2Padded, x2 - w2 - r2, y2Padded);
        drawTrapezoid(x1 + w1, y1, x1 + w1 + r1, y1, x2 + w2 + r2, y2Padded, x2 + w2, y2Padded);

        // 3. Gray asphalt road surface (or checkered start/finish line)
        if (scheme.isStartFinish()) {
            drawCheckeredRoad(x1, y1, w1, x2, y2Padded, w2, seg.getIndex(), fog);
        } else {
            applyFogColor(scheme.getRoad(), fog);
            drawTrapezoid(x1 - w1, y1, x1 + w1, y1, x2 + w2, y2Padded, x2 - w2, y2Padded);

            // 4. Dashed white lane dividers separating the 3 lanes
            if (scheme.hasLaneMarkings() && lanes > 1) {
                float l1 = Math.max(1f, w1 / 38f);
                float l2 = Math.max(0.8f, w2 / 38f);
                float laneW1 = (w1 * 2f) / lanes;
                float laneW2 = (w2 * 2f) / lanes;

                applyFogColor(scheme.getLane(), fog);
                float laneX1 = x1 - w1 + laneW1;
                float laneX2 = x2 - w2 + laneW2;
                for (int lane = 1; lane < lanes; lane++) {
                    drawTrapezoid(
                            laneX1 - l1 * 0.5f, y1,
                            laneX1 + l1 * 0.5f, y1,
                            laneX2 + l2 * 0.5f, y2Padded,
                            laneX2 - l2 * 0.5f, y2Padded
                    );
                    laneX1 += laneW1;
                    laneX2 += laneW2;
                }
            }
        }
    }

    private void drawCheckeredRoad(
            float x1, float y1, float w1,
            float x2, float y2, float w2,
            int segmentIndex,
            float fog
    ) {
        int cells = 12;
        float step1 = (w1 * 2f) / cells;
        float step2 = (w2 * 2f) / cells;
        for (int c = 0; c < cells; c++) {
            boolean light = ((c + segmentIndex) % 2) == 0;
            applyFogColor(light ? CHECKER_LIGHT : CHECKER_DARK, fog);
            float bx1 = (x1 - w1) + c * step1;
            float bx2 = (x2 - w2) + c * step2;
            drawTrapezoid(bx1, y1, bx1 + step1, y1, bx2 + step2, y2, bx2, y2);
        }
    }

    private void drawTrapezoid(
            float blX, float blY,
            float brX, float brY,
            float trX, float trY,
            float tlX, float tlY
    ) {
        shapeRenderer.triangle(blX, blY, brX, brY, trX, trY);
        shapeRenderer.triangle(blX, blY, trX, trY, tlX, tlY);
    }

    private void applyFogColor(Color base, float fogFactor) {
        workColor.set(
                ProjectionMath.lerp(TWILIGHT_FOG.r, base.r, fogFactor),
                ProjectionMath.lerp(TWILIGHT_FOG.g, base.g, fogFactor),
                ProjectionMath.lerp(TWILIGHT_FOG.b, base.b, fogFactor),
                1f
        );
        shapeRenderer.setColor(workColor);
    }

    /**
     * Projects and draws any polymorphic {@link RoadEntity} (traffic car or roadside prop)
     * with exact vertical hill-crest clipping against {@code seg.getClipY()}.
     */
    private void drawProjectedEntity(RoadEntity entity, RoadSegment seg, float segmentLength) {
        ProjectedPoint p1 = seg.getP1();
        ProjectedPoint p2 = seg.getP2();
        if (p1.getCamZ() <= RaceWorld.CAMERA_DEPTH) {
            return;
        }

        TextureRegion region = entity.getSprite();
        if (region == null) {
            return;
        }

        // Smooth sub-segment interpolation for moving TrafficCars; props stay anchored at p1
        float t = (entity instanceof TrafficCar)
                ? ProjectionMath.clamp(ProjectionMath.percentRemaining(entity.getZ(), segmentLength), 0f, 1f)
                : 0f;

        float screenX = ProjectionMath.lerp(p1.getScreenX(), p2.getScreenX(), t);
        float screenY = ProjectionMath.lerp(p1.getScreenY(), p2.getScreenY(), t);
        float screenW = ProjectionMath.lerp(p1.getScreenW(), p2.getScreenW(), t);
        float scale = ProjectionMath.lerp(p1.getScreenScale(), p2.getScreenScale(), t);

        if (scale <= 0.00001f) {
            return;
        }

        // Convert sprite native pixel dimensions into world-projected screen size
        float worldScaleFactor = scale * (VIRTUAL_WIDTH * 4.8f) * entity.getSpriteScale();
        float destW = region.getRegionWidth() * worldScaleFactor;
        float destH = region.getRegionHeight() * worldScaleFactor;

        if (destW < 2f || destH < 2f) {
            return;
        }

        float destX = screenX + (screenW * entity.getOffsetX()) - (destW * entity.getAnchorX());
        float destY = screenY;

        // Hill-crest clipping: hide any bottom portion of the sprite below seg.getClipY()
        float clipY = seg.getClipY();
        float topY = destY + destH;
        if (topY <= clipY) {
            return;
        }

        // Subtle twilight distance atmospheric shading
        float tint = ProjectionMath.lerp(0.55f, 1.0f, seg.getFogFactor());
        spriteBatch.setColor(tint, tint * 0.98f, Math.min(1f, tint * 1.04f), 1f);

        Texture tex = region.getTexture();
        if (destY < clipY) {
            float visibleH = topY - clipY;
            float visibleRatio = ProjectionMath.clamp(visibleH / destH, 0f, 1f);
            int srcH = Math.max(1, Math.round(region.getRegionHeight() * visibleRatio));
            spriteBatch.draw(
                    tex,
                    destX,
                    clipY,
                    destW,
                    visibleH,
                    region.getRegionX(),
                    region.getRegionY(),
                    region.getRegionWidth(),
                    srcH,
                    entity.isFlipX(),
                    false
            );
        } else {
            spriteBatch.draw(
                    tex,
                    destX,
                    destY,
                    destW,
                    destH,
                    region.getRegionX(),
                    region.getRegionY(),
                    region.getRegionWidth(),
                    region.getRegionHeight(),
                    entity.isFlipX(),
                    false
            );
        }
    }

    private void drawPlayerCar(PlayerCar player, float cameraJitterX) {
        TextureRegion carSprite = player.getSprite();
        if (carSprite == null) {
            return;
        }
        spriteBatch.setColor(Color.WHITE);

        float drawW = carSprite.getRegionWidth() * PLAYER_DRAW_SCALE;
        float drawH = carSprite.getRegionHeight() * PLAYER_DRAW_SCALE;

        float centerX = (VIRTUAL_WIDTH * 0.5f)
                + player.getAnimator().getHorizontalShakePx()
                - cameraJitterX * 0.45f;
        float drawX = Math.round(centerX - drawW * 0.5f);
        float drawY = Math.round(PLAYER_BASE_Y + player.getAnimator().getVerticalBouncePx());

        spriteBatch.draw(carSprite, drawX, drawY, drawW, drawH);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
    }
}
