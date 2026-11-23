package com.retro.racer.entities;

import com.retro.racer.world.ProjectionMath;

/**
 * Modular physics component composed by {@link PlayerCar}.
 * Encapsulates longitudinal velocity, acceleration, braking, off-road grass drag,
 * centrifugal curve drift, and collision speed impulses.
 */
public final class CarPhysics {

    public static final float MAX_DISPLAY_KMH = 295f;
    public static final float OFF_ROAD_THRESHOLD = 1.0f;
    public static final float MAX_LATERAL_OFFSET = 2.15f;

    private final float maxSpeed;
    private final float acceleration;
    private final float braking;
    private final float coastDecel;
    private final float offRoadDecel;
    private final float offRoadMaxSpeed;
    private final float centrifugalStrength;
    private final float steerResponsiveness;

    private float speed;
    private float lateralDrift;
    private float collisionCooldown;

    public CarPhysics(float segmentLength) {
        this.maxSpeed = segmentLength * 62f;
        this.acceleration = maxSpeed / 4.2f;
        this.braking = -maxSpeed * 1.15f;
        this.coastDecel = -maxSpeed / 6.5f;
        this.offRoadDecel = -maxSpeed / 1.85f;
        this.offRoadMaxSpeed = maxSpeed * 0.42f;
        this.centrifugalStrength = 0.33f;
        this.steerResponsiveness = 2.15f;
        reset();
    }

    public void reset() {
        this.speed = 0f;
        this.lateralDrift = 0f;
        this.collisionCooldown = 0f;
    }

    /**
     * Advances longitudinal speed and computes lateral displacement for one frame.
     *
     * @param delta        frame delta time in seconds
     * @param accelerating true when gas is pressed
     * @param brakingInput true when brake is pressed
     * @param steerInput   smoothed steering value in [-1, +1]
     * @param currentX     player's current normalized lateral offset
     * @param segmentCurve curvature of the road segment under the player
     * @return new clamped normalized lateral offset X
     */
    public float step(
            float delta,
            boolean accelerating,
            boolean brakingInput,
            float steerInput,
            float currentX,
            float segmentCurve
    ) {
        if (collisionCooldown > 0f) {
            collisionCooldown = Math.max(0f, collisionCooldown - delta);
        }

        if (accelerating) {
            // Slight arcade torque curve: stronger launch at lower speeds
            float torqueBoost = 1.25f - 0.35f * getSpeedRatio();
            speed += acceleration * torqueBoost * delta;
        } else if (brakingInput) {
            speed += braking * delta;
        } else {
            speed += coastDecel * delta;
        }

        boolean offRoad = Math.abs(currentX) > OFF_ROAD_THRESHOLD;
        if (offRoad && speed > offRoadMaxSpeed) {
            speed += offRoadDecel * delta;
            if (speed < offRoadMaxSpeed) {
                speed = offRoadMaxSpeed;
            }
        }

        speed = ProjectionMath.clamp(speed, 0f, maxSpeed);

        float speedRatio = getSpeedRatio();
        // Lateral movement scales with speed so the car cannot slide sideways while stopped
        float effectiveSteerScale = speed > 50f ? (0.35f + 0.65f * speedRatio) : 0f;
        float steerDelta = steerInput * steerResponsiveness * effectiveSteerScale * delta;

        // Centrifugal force pushes outward on curves proportional to v^2
        float centrifugal = segmentCurve * speedRatio * speedRatio * centrifugalStrength * delta * 2.2f;
        this.lateralDrift = centrifugal;

        float nextX = currentX + steerDelta - centrifugal;
        return ProjectionMath.clamp(nextX, -MAX_LATERAL_OFFSET, MAX_LATERAL_OFFSET);
    }

    /**
     * Applies a collision slowdown impulse and bounce cooldown.
     *
     * @param targetSpeedRatio fraction of current/max speed to clamp down to
     */
    public void applyCollisionImpact(float targetSpeedRatio) {
        float capped = maxSpeed * ProjectionMath.clamp(targetSpeedRatio, 0.08f, 0.75f);
        if (speed > capped) {
            speed = capped;
        } else {
            speed *= 0.55f;
        }
        collisionCooldown = 0.25f;
    }

    public boolean isOffRoad(float offsetX) {
        return Math.abs(offsetX) > OFF_ROAD_THRESHOLD;
    }

    public float getSpeed() {
        return speed;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public float getSpeedRatio() {
        return maxSpeed > 0f ? ProjectionMath.clamp(speed / maxSpeed, 0f, 1f) : 0f;
    }

    public float getDisplayKmh() {
        return getSpeedRatio() * MAX_DISPLAY_KMH;
    }

    public float getLateralDrift() {
        return lateralDrift;
    }

    public boolean isInCollisionCooldown() {
        return collisionCooldown > 0f;
    }
}
