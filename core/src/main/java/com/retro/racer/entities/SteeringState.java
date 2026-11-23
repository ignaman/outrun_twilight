package com.retro.racer.entities;

import com.retro.racer.world.ProjectionMath;

/**
 * Modular steering and visual-state component composed by {@link PlayerCar}.
 * Smooths raw digital steering inputs and resolves the active {@link CarSpriteState}.
 */
public final class SteeringState {

    private static final float STEER_ATTACK_RATE = 7.5f;
    private static final float STEER_RETURN_RATE = 9.0f;
    private static final float VISUAL_TURN_THRESHOLD = 0.22f;
    private static final float UPHILL_SLOPE_THRESHOLD = 18f;

    private float smoothedSteer;
    private boolean braking;
    private boolean uphill;
    private CarSpriteState currentSpriteState;

    public SteeringState() {
        reset();
    }

    public void reset() {
        this.smoothedSteer = 0f;
        this.braking = false;
        this.uphill = false;
        this.currentSpriteState = CarSpriteState.STRAIGHT;
    }

    /**
     * Updates smoothed steering input and resolves the visual {@link CarSpriteState}.
     *
     * @param delta        frame delta time in seconds
     * @param steerLeft    true when left key is held
     * @param steerRight   true when right key is held
     * @param brakingInput true when brake key is held
     * @param roadSlope    elevation delta (p2.y - p1.y) of the current road segment
     */
    public void update(
            float delta,
            boolean steerLeft,
            boolean steerRight,
            boolean brakingInput,
            float roadSlope
    ) {
        float target = 0f;
        if (steerLeft && !steerRight) {
            target = -1f;
        } else if (steerRight && !steerLeft) {
            target = 1f;
        }

        if (target != 0f) {
            if (smoothedSteer < target) {
                smoothedSteer = Math.min(target, smoothedSteer + STEER_ATTACK_RATE * delta);
            } else {
                smoothedSteer = Math.max(target, smoothedSteer - STEER_ATTACK_RATE * delta);
            }
        } else {
            if (smoothedSteer > 0f) {
                smoothedSteer = Math.max(0f, smoothedSteer - STEER_RETURN_RATE * delta);
            } else if (smoothedSteer < 0f) {
                smoothedSteer = Math.min(0f, smoothedSteer + STEER_RETURN_RATE * delta);
            }
        }

        smoothedSteer = ProjectionMath.clamp(smoothedSteer, -1f, 1f);
        this.braking = brakingInput;
        this.uphill = roadSlope > UPHILL_SLOPE_THRESHOLD;

        int discreteSteer = 0;
        if (smoothedSteer <= -VISUAL_TURN_THRESHOLD) {
            discreteSteer = -1;
        } else if (smoothedSteer >= VISUAL_TURN_THRESHOLD) {
            discreteSteer = 1;
        }

        this.currentSpriteState = CarSpriteState.resolve(discreteSteer, uphill, braking);
    }

    public float getSmoothedSteer() {
        return smoothedSteer;
    }

    public boolean isBraking() {
        return braking;
    }

    public boolean isUphill() {
        return uphill;
    }

    public CarSpriteState getCurrentSpriteState() {
        return currentSpriteState;
    }
}
