package com.retro.racer.entities;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.retro.racer.world.ProjectionMath;

import java.util.List;

/**
 * AI-controlled rival vehicle driving along the 3-lane highway with smooth
 * lane-keeping and basic forward obstacle / traffic avoidance.
 */
public final class TrafficCar extends RoadEntity {

    /** Canonical normalized centers for the 3 highway lanes: left, center, right. */
    public static final float[] LANE_CENTERS = new float[] { -0.64f, 0.0f, 0.64f };

    private static final float COLLISION_WIDTH = 0.42f;
    private static final float WORLD_HEIGHT = 400f;
    private static final float COLLISION_LENGTH = 260f;
    private static final float SPRITE_SCALE = 0.94f;
    private static final float LANE_SWITCH_SPEED = 0.95f;
    private static final float LOOKAHEAD_DISTANCE = 1500f;

    private final TextureRegion sprite;
    private final float baseSpeed;

    private float speed;
    private int targetLaneIndex;
    private float laneChangeCooldown;

    public TrafficCar(float startZ, int laneIndex, float baseSpeed, TextureRegion sprite) {
        super(
                startZ,
                LANE_CENTERS[clampLane(laneIndex)],
                COLLISION_WIDTH,
                WORLD_HEIGHT,
                COLLISION_LENGTH,
                SPRITE_SCALE
        );
        this.targetLaneIndex = clampLane(laneIndex);
        this.baseSpeed = baseSpeed;
        this.speed = baseSpeed;
        this.sprite = sprite;
        this.laneChangeCooldown = 0f;
    }

    private static int clampLane(int lane) {
        if (lane < 0) return 0;
        if (lane >= LANE_CENTERS.length) return LANE_CENTERS.length - 1;
        return lane;
    }

    @Override
    public void update(float delta) {
        setZ(getZ() + speed * delta);

        if (laneChangeCooldown > 0f) {
            laneChangeCooldown = Math.max(0f, laneChangeCooldown - delta);
        }

        float targetX = LANE_CENTERS[targetLaneIndex];
        float diff = targetX - getOffsetX();
        float maxStep = LANE_SWITCH_SPEED * delta;
        if (Math.abs(diff) <= maxStep) {
            setOffsetX(targetX);
        } else {
            setOffsetX(getOffsetX() + Math.signum(diff) * maxStep);
        }
    }

    /**
     * Advances this AI car while checking for slower cars or the player ahead
     * and smoothly steering into a clearer adjacent lane when available.
     */
    public void updateAI(float delta, float trackLength, List<TrafficCar> allTraffic, PlayerCar player) {
        if (laneChangeCooldown <= 0f) {
            boolean blockedAhead = isBlockedInLane(targetLaneIndex, trackLength, allTraffic, player);
            if (blockedAhead) {
                int altLane = findOpenAdjacentLane(trackLength, allTraffic, player);
                if (altLane != targetLaneIndex) {
                    targetLaneIndex = altLane;
                    laneChangeCooldown = 1.6f;
                } else {
                    // Gently ease off speed if boxed in
                    speed = ProjectionMath.lerp(speed, baseSpeed * 0.78f, Math.min(1f, delta * 3f));
                }
            } else {
                speed = ProjectionMath.lerp(speed, baseSpeed, Math.min(1f, delta * 2f));
            }
        }

        update(delta);
        setZ(ProjectionMath.wrap(getZ(), trackLength));
    }

    private boolean isBlockedInLane(int lane, float trackLength, List<TrafficCar> allTraffic, PlayerCar player) {
        float laneX = LANE_CENTERS[lane];

        for (int i = 0; i < allTraffic.size(); i++) {
            TrafficCar other = allTraffic.get(i);
            if (other == this) {
                continue;
            }
            float aheadDist = forwardDistance(getZ(), other.getZ(), trackLength);
            if (aheadDist > 0f && aheadDist < LOOKAHEAD_DISTANCE
                    && Math.abs(other.getOffsetX() - laneX) < 0.42f
                    && other.getSpeed() <= this.speed + 200f) {
                return true;
            }
        }

        if (player != null) {
            float playerAhead = forwardDistance(getZ(), player.getZ(), trackLength);
            if (playerAhead > 0f && playerAhead < LOOKAHEAD_DISTANCE * 0.85f
                    && Math.abs(player.getOffsetX() - laneX) < 0.44f
                    && player.getSpeed() < this.speed) {
                return true;
            }
        }
        return false;
    }

    private int findOpenAdjacentLane(float trackLength, List<TrafficCar> allTraffic, PlayerCar player) {
        int leftLane = targetLaneIndex - 1;
        int rightLane = targetLaneIndex + 1;

        if (leftLane >= 0 && !isBlockedInLane(leftLane, trackLength, allTraffic, player)
                && !isOccupiedNearby(leftLane, trackLength, allTraffic)) {
            return leftLane;
        }
        if (rightLane < LANE_CENTERS.length && !isBlockedInLane(rightLane, trackLength, allTraffic, player)
                && !isOccupiedNearby(rightLane, trackLength, allTraffic)) {
            return rightLane;
        }
        return targetLaneIndex;
    }

    private boolean isOccupiedNearby(int lane, float trackLength, List<TrafficCar> allTraffic) {
        float laneX = LANE_CENTERS[lane];
        for (int i = 0; i < allTraffic.size(); i++) {
            TrafficCar other = allTraffic.get(i);
            if (other == this) {
                continue;
            }
            float dz = Math.abs(getZ() - other.getZ());
            dz = Math.min(dz, trackLength - dz);
            if (dz < 520f && Math.abs(other.getOffsetX() - laneX) < 0.42f) {
                return true;
            }
        }
        return false;
    }

    private static float forwardDistance(float fromZ, float toZ, float trackLength) {
        float diff = toZ - fromZ;
        if (diff < 0f) {
            diff += trackLength;
        }
        return diff;
    }

    @Override
    public TextureRegion getSprite() {
        return sprite;
    }

    public float getSpeed() {
        return speed;
    }

    public float getBaseSpeed() {
        return baseSpeed;
    }

    public int getTargetLaneIndex() {
        return targetLaneIndex;
    }
}
