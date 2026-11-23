package com.retro.racer.entities;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.retro.racer.graphics.AssetFactory;

/**
 * The player-controlled 16-bit retro Ferrari-style sports car.
 * Demonstrates Composition over Inheritance by delegating motion, steering state,
 * and sprite animation to {@link CarPhysics}, {@link SteeringState}, and {@link CarAnimator}.
 */
public final class PlayerCar extends RoadEntity {

    private static final float COLLISION_WIDTH = 0.44f;
    private static final float WORLD_HEIGHT = 420f;
    private static final float COLLISION_LENGTH = 260f;
    private static final float SPRITE_SCALE = 1.0f;

    private final CarPhysics physics;
    private final SteeringState steering;
    private final CarAnimator animator;

    private boolean inputAccelerate;
    private boolean inputBrake;
    private boolean inputLeft;
    private boolean inputRight;
    private float currentRoadCurve;
    private float currentRoadSlope;

    public PlayerCar(float initialZ, float segmentLength, AssetFactory assets) {
        super(initialZ, 0f, COLLISION_WIDTH, WORLD_HEIGHT, COLLISION_LENGTH, SPRITE_SCALE);
        this.physics = new CarPhysics(segmentLength);
        this.steering = new SteeringState();
        this.animator = new CarAnimator(assets);
    }

    public void reset(float startZ) {
        setZ(startZ);
        setOffsetX(0f);
        physics.reset();
        steering.reset();
        animator.reset();
        clearInputs();
        currentRoadCurve = 0f;
        currentRoadSlope = 0f;
    }

    public void clearInputs() {
        this.inputAccelerate = false;
        this.inputBrake = false;
        this.inputLeft = false;
        this.inputRight = false;
    }

    public void setControlInputs(boolean accelerate, boolean brake, boolean steerLeft, boolean steerRight) {
        this.inputAccelerate = accelerate;
        this.inputBrake = brake;
        this.inputLeft = steerLeft;
        this.inputRight = steerRight;
    }

    public void setRoadContext(float segmentCurve, float segmentSlope) {
        this.currentRoadCurve = segmentCurve;
        this.currentRoadSlope = segmentSlope;
    }

    @Override
    public void update(float delta) {
        steering.update(delta, inputLeft, inputRight, inputBrake, currentRoadSlope);

        float nextX = physics.step(
                delta,
                inputAccelerate,
                inputBrake,
                steering.getSmoothedSteer(),
                getOffsetX(),
                currentRoadCurve
        );
        setOffsetX(nextX);

        boolean offRoad = physics.isOffRoad(nextX);
        animator.update(delta, physics.getSpeedRatio(), offRoad);
    }

    @Override
    public TextureRegion getSprite() {
        return animator.getSpriteFor(steering.getCurrentSpriteState());
    }

    public void onTrafficCollision(float otherSpeedRatio, float otherOffsetX) {
        physics.applyCollisionImpact(Math.max(0.18f, otherSpeedRatio * 0.8f));
        // Gentle lateral deflection away from the rival car
        float push = getOffsetX() < otherOffsetX ? -0.14f : 0.14f;
        setOffsetX(Math.max(-CarPhysics.MAX_LATERAL_OFFSET,
                Math.min(CarPhysics.MAX_LATERAL_OFFSET, getOffsetX() + push)));
    }

    public void onPropCollision() {
        physics.applyCollisionImpact(0.12f);
        // Nudge back toward the road shoulder
        if (getOffsetX() > 0f) {
            setOffsetX(Math.max(0.75f, getOffsetX() - 0.25f));
        } else {
            setOffsetX(Math.min(-0.75f, getOffsetX() + 0.25f));
        }
    }

    public CarPhysics getPhysics() {
        return physics;
    }

    public SteeringState getSteering() {
        return steering;
    }

    public CarAnimator getAnimator() {
        return animator;
    }

    public boolean isOffRoad() {
        return physics.isOffRoad(getOffsetX());
    }

    public float getSpeed() {
        return physics.getSpeed();
    }

    public float getSpeedRatio() {
        return physics.getSpeedRatio();
    }

    public float getDisplayKmh() {
        return physics.getDisplayKmh();
    }

    public CarSpriteState getSpriteState() {
        return steering.getCurrentSpriteState();
    }
}
