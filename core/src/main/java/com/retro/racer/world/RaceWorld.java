package com.retro.racer.world;

import com.retro.racer.GameState;
import com.retro.racer.entities.PlayerCar;
import com.retro.racer.entities.RoadEntity;
import com.retro.racer.entities.RoadsideProp;
import com.retro.racer.entities.TrafficCar;
import com.retro.racer.graphics.AssetFactory;
import com.retro.racer.ui.HudMetrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Encapsulates the complete simulation state of a race: track geometry,
 * player vehicle, AI traffic cars, camera parameters, lap timing,
 * parallax scroll offsets, off-road camera jitter, and collision resolution.
 */
public final class RaceWorld {

    public static final float CAMERA_HEIGHT = 1000f;
    public static final float FIELD_OF_VIEW_DEG = 100f;
    public static final float CAMERA_DEPTH = (float) (1.0 / Math.tan(Math.toRadians(FIELD_OF_VIEW_DEG * 0.5)));
    public static final int DRAW_DISTANCE = 190;
    public static final int TOTAL_LAPS = 3;

    private static final float WORLD_UNITS_PER_KM = 151330f;
    private static final int TRAFFIC_COUNT = 26;

    private final AssetFactory assets;
    private final Track track;
    private final PlayerCar playerCar;
    private final List<TrafficCar> trafficCars;
    private final float playerZOffset;

    private GameState gameState;
    private float cameraZ;
    private float cameraJitterX;
    private float cameraJitterY;
    private float jitterPhase;

    private float skyOffset;
    private float cloudOffset;
    private float cityOffset;

    private int currentLap;
    private float lapTimeSeconds;
    private float bestLapSeconds;
    private float totalTimeSeconds;
    private float totalDistanceUnits;

    private boolean collisionThisFrame;
    private boolean lapCompletedThisFrame;

    public RaceWorld(AssetFactory assets) {
        this.assets = assets;
        this.track = TrackBuilder.buildDefaultCircuit(assets);
        this.playerZOffset = CAMERA_HEIGHT * CAMERA_DEPTH;
        this.playerCar = new PlayerCar(playerZOffset, track.getSegmentLength(), assets);
        this.trafficCars = new ArrayList<TrafficCar>();
        reset(GameState.START);
    }

    /**
     * Resets the race back to the starting grid with fresh AI traffic placement.
     */
    public void reset(GameState initialState) {
        this.gameState = initialState;
        this.cameraZ = 0f;
        this.cameraJitterX = 0f;
        this.cameraJitterY = 0f;
        this.jitterPhase = 0f;
        this.skyOffset = 0f;
        this.cloudOffset = 0f;
        this.cityOffset = 0f;

        this.currentLap = 1;
        this.lapTimeSeconds = 0f;
        this.bestLapSeconds = -1f;
        this.totalTimeSeconds = 0f;
        this.totalDistanceUnits = 0f;
        this.collisionThisFrame = false;
        this.lapCompletedThisFrame = false;

        this.playerCar.reset(track.wrapZ(cameraZ + playerZOffset));
        spawnTraffic();
    }

    private void spawnTraffic() {
        trafficCars.clear();
        Random rng = new Random(198604L);
        float totalLen = track.getTotalLength();
        float maxSpeed = playerCar.getPhysics().getMaxSpeed();
        int variants = assets.getTrafficVariantCount();

        for (int i = 0; i < TRAFFIC_COUNT; i++) {
            // Leave a clean runway near the starting grid so the player launches cleanly
            float startFraction = 0.04f + ((float) i / TRAFFIC_COUNT) * 0.93f;
            float z = track.wrapZ(startFraction * totalLen);
            int lane = (i + rng.nextInt(3)) % TrafficCar.LANE_CENTERS.length;
            float speedFactor = 0.30f + rng.nextFloat() * 0.28f;
            int variant = i % variants;

            trafficCars.add(new TrafficCar(
                    z,
                    lane,
                    maxSpeed * speedFactor,
                    assets.getTrafficCarSprite(variant)
            ));
        }
    }

    public void startRace() {
        if (gameState == GameState.START || gameState == GameState.PAUSED) {
            gameState = GameState.RACING;
        } else if (gameState == GameState.FINISHED) {
            reset(GameState.RACING);
        }
    }

    public void togglePause() {
        if (gameState == GameState.RACING) {
            gameState = GameState.PAUSED;
        } else if (gameState == GameState.PAUSED) {
            gameState = GameState.RACING;
        }
    }

    /**
     * Advances simulation by one frame.
     */
    public void update(float delta, boolean accel, boolean brake, boolean left, boolean right) {
        collisionThisFrame = false;
        lapCompletedThisFrame = false;

        // Gentle ambient cloud drift even while paused on the Start screen
        if (gameState == GameState.START) {
            cloudOffset += 4.5f * delta;
            playerCar.getAnimator().update(delta, 0f, false);
            return;
        }
        if (!gameState.isSimulationActive()) {
            return;
        }

        RoadSegment playerSegment = track.findSegment(cameraZ + playerZOffset);

        playerCar.setControlInputs(accel, brake, left, right);
        playerCar.setRoadContext(playerSegment.getCurve(), playerSegment.getSlope());
        playerCar.update(delta);

        float forwardDelta = playerCar.getSpeed() * delta;
        float prevCameraZ = cameraZ;
        cameraZ = track.wrapZ(cameraZ + forwardDelta);
        playerCar.setZ(track.wrapZ(cameraZ + playerZOffset));

        // Timers & distance
        totalDistanceUnits += forwardDelta;
        lapTimeSeconds += delta;
        totalTimeSeconds += delta;

        // Detect crossing the start/finish line
        if (forwardDelta > 0f && cameraZ < prevCameraZ) {
            onLapCompleted();
        }

        // Scroll parallax layers smoothly with road curvature and player speed
        float speedRatio = playerCar.getSpeedRatio();
        float curveScroll = playerSegment.getCurve() * speedRatio * delta;
        skyOffset += curveScroll * 18f;
        cloudOffset += curveScroll * 36f + 4.5f * delta;
        cityOffset += curveScroll * 72f;

        // Off-road grass camera jitter
        updateCameraJitter(delta, speedRatio, playerCar.isOffRoad());

        // Update AI traffic vehicles
        float totalLen = track.getTotalLength();
        for (int i = 0; i < trafficCars.size(); i++) {
            trafficCars.get(i).updateAI(delta, totalLen, trafficCars, playerCar);
        }

        // Resolve collisions against AI traffic and solid roadside props
        resolveCollisions();
    }

    private void onLapCompleted() {
        if (bestLapSeconds < 0f || lapTimeSeconds < bestLapSeconds) {
            bestLapSeconds = lapTimeSeconds;
        }
        lapCompletedThisFrame = true;

        if (currentLap >= TOTAL_LAPS) {
            gameState = GameState.FINISHED;
            playerCar.clearInputs();
        } else {
            currentLap++;
            lapTimeSeconds = 0f;
        }
    }

    private void updateCameraJitter(float delta, float speedRatio, boolean offRoad) {
        jitterPhase += delta * (30f + speedRatio * 42f);
        if (offRoad && speedRatio > 0.04f) {
            float intensity = speedRatio;
            cameraJitterX = (float) Math.sin(jitterPhase * 1.7f) * 3.8f * intensity;
            cameraJitterY = (float) Math.cos(jitterPhase * 2.3f) * 4.5f * intensity;
        } else {
            cameraJitterX = ProjectionMath.lerp(cameraJitterX, 0f, Math.min(1f, delta * 14f));
            cameraJitterY = ProjectionMath.lerp(cameraJitterY, 0f, Math.min(1f, delta * 14f));
        }
    }

    private void resolveCollisions() {
        if (playerCar.getPhysics().isInCollisionCooldown()) {
            return;
        }

        float trackLength = track.getTotalLength();
        float maxSpeed = playerCar.getPhysics().getMaxSpeed();

        // 1. Check collisions with AI traffic cars
        for (int i = 0; i < trafficCars.size(); i++) {
            TrafficCar car = trafficCars.get(i);
            if (playerCar.collidesWith(car, trackLength, 0.88f)) {
                playerCar.onTrafficCollision(car.getSpeed() / maxSpeed, car.getOffsetX());
                collisionThisFrame = true;
                return;
            }
        }

        // 2. Check collisions with solid roadside props when driving far off-road
        if (Math.abs(playerCar.getOffsetX()) > 0.95f) {
            RoadSegment playerSeg = track.findSegment(playerCar.getZ());
            int baseIdx = playerSeg.getIndex();
            for (int offset = -1; offset <= 1; offset++) {
                RoadSegment seg = track.getSegment(baseIdx + offset);
                List<RoadsideProp> props = seg.getProps();
                for (int i = 0; i < props.size(); i++) {
                    RoadsideProp prop = props.get(i);
                    if (prop.isSolid() && playerCar.collidesWith(prop, trackLength, 0.85f)) {
                        playerCar.onPropCollision();
                        collisionThisFrame = true;
                        return;
                    }
                }
            }
        }
    }

    /**
     * Collects all renderable {@link RoadEntity} instances (both static {@link RoadsideProp}s
     * and dynamic {@link TrafficCar}s) currently residing within {@code segment} into a single
     * polymorphic list for uniform back-to-front rendering.
     */
    public void collectEntitiesInSegment(RoadSegment segment, List<RoadEntity> out) {
        out.clear();
        List<RoadsideProp> props = segment.getProps();
        for (int i = 0; i < props.size(); i++) {
            out.add(props.get(i));
        }

        int segIndex = segment.getIndex();
        float segLen = track.getSegmentLength();
        int segCount = track.getSegmentCount();
        for (int i = 0; i < trafficCars.size(); i++) {
            TrafficCar car = trafficCars.get(i);
            int carSegIdx = ((int) Math.floor(car.getZ() / segLen)) % segCount;
            if (carSegIdx < 0) {
                carSegIdx += segCount;
            }
            if (carSegIdx == segIndex) {
                out.add(car);
            }
        }
    }

    /**
     * Builds an immutable {@link HudMetrics} telemetry snapshot for the HUD renderer.
     */
    public HudMetrics createHudMetrics(boolean muted) {
        return new HudMetrics(
                playerCar.getDisplayKmh(),
                playerCar.getSpeedRatio(),
                lapTimeSeconds,
                bestLapSeconds,
                totalTimeSeconds,
                totalDistanceUnits / WORLD_UNITS_PER_KM,
                currentLap,
                TOTAL_LAPS,
                playerCar.isOffRoad(),
                muted,
                gameState
        );
    }

    public Track getTrack() {
        return track;
    }

    public PlayerCar getPlayerCar() {
        return playerCar;
    }

    public List<TrafficCar> getTrafficCars() {
        return Collections.unmodifiableList(trafficCars);
    }

    public GameState getGameState() {
        return gameState;
    }

    public float getCameraZ() {
        return cameraZ;
    }

    public float getPlayerZOffset() {
        return playerZOffset;
    }

    public float getCameraJitterX() {
        return cameraJitterX;
    }

    public float getCameraJitterY() {
        return cameraJitterY;
    }

    public float getSkyOffset() {
        return skyOffset;
    }

    public float getCloudOffset() {
        return cloudOffset;
    }

    public float getCityOffset() {
        return cityOffset;
    }

    public boolean hadCollisionThisFrame() {
        return collisionThisFrame;
    }

    public boolean hadLapCompletedThisFrame() {
        return lapCompletedThisFrame;
    }
}
