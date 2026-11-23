package com.retro.racer;

import com.badlogic.gdx.Game;
import com.retro.racer.audio.AudioController;
import com.retro.racer.graphics.AssetFactory;
import com.retro.racer.graphics.ProceduralTextureFactory;

/**
 * Main libGDX {@link Game} entry point for the 16-bit pseudo-3D retro arcade racer.
 * Owns the shared {@link AssetFactory} and {@link AudioController} lifecycles and
 * delegates active gameplay to {@link RaceScreen}.
 */
public class RetroRacerGame extends Game {

    private AssetFactory assetFactory;
    private AudioController audioController;
    private RaceScreen raceScreen;

    @Override
    public void create() {
        this.assetFactory = new ProceduralTextureFactory();
        this.audioController = new AudioController();
        this.raceScreen = new RaceScreen(this, assetFactory, audioController);
        setScreen(raceScreen);
    }

    public AssetFactory getAssetFactory() {
        return assetFactory;
    }

    public AudioController getAudioController() {
        return audioController;
    }

    @Override
    public void dispose() {
        if (raceScreen != null) {
            raceScreen.dispose();
            raceScreen = null;
        }
        if (audioController != null) {
            audioController.dispose();
            audioController = null;
        }
        if (assetFactory != null) {
            assetFactory.dispose();
            assetFactory = null;
        }
        super.dispose();
    }
}
