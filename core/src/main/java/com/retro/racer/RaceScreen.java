package com.retro.racer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.retro.racer.audio.AudioController;
import com.retro.racer.graphics.AssetFactory;
import com.retro.racer.rendering.HudRenderer;
import com.retro.racer.rendering.WorldRenderer;
import com.retro.racer.ui.HudMetrics;
import com.retro.racer.world.RaceWorld;

/**
 * Active racing {@link Screen} orchestrating the frame lifecycle:
 * input polling -> {@link RaceWorld#update} -> {@link WorldRenderer#render}
 * -> {@link HudRenderer#render} -> {@link AudioController#updateState}.
 */
public final class RaceScreen implements Screen {

    private static final float MAX_FRAME_DELTA = 1f / 20f;

    private final RetroRacerGame game;
    private final AudioController audioController;
    private final RaceWorld world;
    private final WorldRenderer worldRenderer;
    private final HudRenderer hudRenderer;

    public RaceScreen(RetroRacerGame game, AssetFactory assets, AudioController audioController) {
        this.game = game;
        this.audioController = audioController;
        this.world = new RaceWorld(assets);
        this.worldRenderer = new WorldRenderer(assets);
        this.hudRenderer = new HudRenderer(assets);
    }

    @Override
    public void render(float delta) {
        float clampedDelta = Math.min(delta, MAX_FRAME_DELTA);
        handleStateInput();
        update(clampedDelta);
        draw();
    }

    private void handleStateInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (world.getGameState() != GameState.RACING) {
                world.startRace();
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (world.getGameState() == GameState.RACING || world.getGameState() == GameState.PAUSED) {
                world.togglePause();
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            GameState nextState = world.getGameState() == GameState.START
                    ? GameState.START
                    : GameState.RACING;
            world.reset(nextState);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            audioController.toggleMute();
        }
    }

    private void update(float delta) {
        boolean accelerate = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean brake = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN);
        boolean steerLeft = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean steerRight = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);

        world.update(delta, accelerate, brake, steerLeft, steerRight);
        hudRenderer.update(delta);

        audioController.updateState(
                world.getGameState().isSimulationActive(),
                world.getPlayerCar().getSpeedRatio(),
                world.getPlayerCar().isOffRoad()
        );
        if (world.hadCollisionThisFrame()) {
            audioController.triggerCollision();
        }
        if (world.hadLapCompletedThisFrame()) {
            audioController.triggerLapChime();
        }
    }

    private void draw() {
        worldRenderer.render(world);
        HudMetrics metrics = world.createHudMetrics(audioController.isMuted());
        hudRenderer.render(metrics);
    }

    @Override
    public void resize(int width, int height) {
        worldRenderer.resize(width, height);
        hudRenderer.resize(width, height);
    }

    @Override
    public void show() {
        // Screen initialized in constructor; no additional setup required.
    }

    @Override
    public void pause() {
        if (world.getGameState() == GameState.RACING) {
            world.togglePause();
        }
    }

    @Override
    public void resume() {
        // Remain paused until user explicitly resumes.
    }

    @Override
    public void hide() {
        audioController.updateState(false, 0f, false);
    }

    public RetroRacerGame getGame() {
        return game;
    }

    @Override
    public void dispose() {
        worldRenderer.dispose();
        hudRenderer.dispose();
    }
}
