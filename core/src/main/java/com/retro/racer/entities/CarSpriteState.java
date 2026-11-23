package com.retro.racer.entities;

/**
 * State machine enum representing the visual orientation and slope/braking frame
 * of the player's retro sports car.
 */
public enum CarSpriteState {
    STRAIGHT(0, false, false),
    LEFT(-1, false, false),
    RIGHT(1, false, false),
    UPHILL_STRAIGHT(0, true, false),
    UPHILL_LEFT(-1, true, false),
    UPHILL_RIGHT(1, true, false),
    BRAKING_STRAIGHT(0, false, true),
    BRAKING_LEFT(-1, false, true),
    BRAKING_RIGHT(1, false, true),
    UPHILL_BRAKING_STRAIGHT(0, true, true),
    UPHILL_BRAKING_LEFT(-1, true, true),
    UPHILL_BRAKING_RIGHT(1, true, true);

    private final int steerDirection;
    private final boolean uphill;
    private final boolean braking;

    CarSpriteState(int steerDirection, boolean uphill, boolean braking) {
        this.steerDirection = steerDirection;
        this.uphill = uphill;
        this.braking = braking;
    }

    public int getSteerDirection() {
        return steerDirection;
    }

    public boolean isUphill() {
        return uphill;
    }

    public boolean isBraking() {
        return braking;
    }

    /**
     * Resolves the appropriate sprite state from discrete steering (-1, 0, +1),
     * uphill slope flag, and braking flag.
     */
    public static CarSpriteState resolve(int steerDir, boolean uphill, boolean braking) {
        if (uphill && braking) {
            if (steerDir < 0) return UPHILL_BRAKING_LEFT;
            if (steerDir > 0) return UPHILL_BRAKING_RIGHT;
            return UPHILL_BRAKING_STRAIGHT;
        }
        if (uphill) {
            if (steerDir < 0) return UPHILL_LEFT;
            if (steerDir > 0) return UPHILL_RIGHT;
            return UPHILL_STRAIGHT;
        }
        if (braking) {
            if (steerDir < 0) return BRAKING_LEFT;
            if (steerDir > 0) return BRAKING_RIGHT;
            return BRAKING_STRAIGHT;
        }
        if (steerDir < 0) return LEFT;
        if (steerDir > 0) return RIGHT;
        return STRAIGHT;
    }
}
