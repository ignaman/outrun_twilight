package com.retro.racer.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.utils.Disposable;

/**
 * Zero-asset procedural 16-bit retro sound synthesizer built on libGDX {@link AudioDevice}.
 * Synthesizes an OutRun-style engine rev hum whose pitch tracks speed, off-road gravel rumble,
 * collision impact thuds, and lap completion chimes on a lightweight daemon thread.
 */
public final class AudioController implements Disposable {

    private static final int SAMPLE_RATE = 22050;
    private static final int BUFFER_SAMPLES = 512;

    private AudioDevice audioDevice;
    private Thread synthThread;

    private volatile boolean running;
    private volatile boolean muted;
    private volatile boolean active;
    private volatile float speedRatio;
    private volatile boolean offRoad;
    private volatile float collisionPulse;
    private volatile float chimeTimer;

    public AudioController() {
        this.muted = false;
        this.active = false;
        try {
            this.audioDevice = Gdx.audio.newAudioDevice(SAMPLE_RATE, true);
            this.running = true;
            this.synthThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    runSynthLoop();
                }
            }, "RetroRacer-AudioSynth");
            this.synthThread.setDaemon(true);
            this.synthThread.start();
        } catch (Throwable ignored) {
            // Gracefully disable audio on headless or unsupported sound environments.
            this.audioDevice = null;
            this.running = false;
        }
    }

    public void updateState(boolean racingActive, float speedRatio, boolean offRoad) {
        this.active = racingActive;
        this.speedRatio = Math.max(0f, Math.min(1f, speedRatio));
        this.offRoad = offRoad;
    }

    public void triggerCollision() {
        this.collisionPulse = 1.0f;
    }

    public void triggerLapChime() {
        this.chimeTimer = 0.42f;
    }

    public void toggleMute() {
        this.muted = !this.muted;
    }

    public boolean isMuted() {
        return muted;
    }

    private void runSynthLoop() {
        short[] buffer = new short[BUFFER_SAMPLES];
        double enginePhase = 0.0;
        double subPhase = 0.0;
        double chimePhase = 0.0;
        long noiseSeed = 0x5DEECE66DL;

        while (running && audioDevice != null) {
            boolean silent = muted || !active;
            float sr = speedRatio;
            boolean grass = offRoad && sr > 0.05f;

            // Classic 16-bit sports car engine pitch curve (55 Hz idle -> 215 Hz redline)
            double baseFreq = 54.0 + sr * 165.0;
            double subFreq = baseFreq * 0.5;

            for (int i = 0; i < BUFFER_SAMPLES; i++) {
                float sample = 0f;

                if (!silent) {
                    // Warm retro sawtooth + sub-square harmonic blend
                    double saw = (enginePhase * 2.0) - 1.0;
                    double sq = subPhase < 0.5 ? 1.0 : -1.0;
                    float engineVol = 0.065f + sr * 0.085f;
                    sample += (float) ((saw * 0.65 + sq * 0.35) * engineVol);

                    enginePhase += baseFreq / SAMPLE_RATE;
                    if (enginePhase >= 1.0) enginePhase -= 1.0;
                    subPhase += subFreq / SAMPLE_RATE;
                    if (subPhase >= 1.0) subPhase -= 1.0;

                    // Off-road grass rumble noise
                    if (grass) {
                        noiseSeed = (noiseSeed * 1103515245L + 12345L) & 0x7fffffffL;
                        float white = ((noiseSeed / (float) 0x7fffffffL) * 2f) - 1f;
                        sample += white * 0.075f * sr;
                    }

                    // Collision crunch impulse
                    if (collisionPulse > 0.001f) {
                        noiseSeed = (noiseSeed * 1103515245L + 12345L) & 0x7fffffffL;
                        float white = ((noiseSeed / (float) 0x7fffffffL) * 2f) - 1f;
                        sample += white * 0.24f * collisionPulse;
                        collisionPulse *= 0.9992f;
                    }

                    // Retro FM lap chime arpeggio
                    if (chimeTimer > 0f) {
                        double noteFreq = chimeTimer > 0.28f ? 523.25 : (chimeTimer > 0.14f ? 659.25 : 783.99);
                        double tone = chimePhase < 0.5 ? 1.0 : -1.0;
                        sample += (float) (tone * 0.11 * Math.min(1f, chimeTimer * 4f));
                        chimePhase += noteFreq / SAMPLE_RATE;
                        if (chimePhase >= 1.0) chimePhase -= 1.0;
                        chimeTimer = Math.max(0f, chimeTimer - (1f / SAMPLE_RATE));
                    }
                }

                float clamped = Math.max(-0.95f, Math.min(0.95f, sample));
                buffer[i] = (short) (clamped * 32767f);
            }

            try {
                audioDevice.writeSamples(buffer, 0, BUFFER_SAMPLES);
            } catch (Throwable t) {
                break;
            }
        }
    }

    @Override
    public void dispose() {
        running = false;
        if (synthThread != null) {
            try {
                synthThread.join(250L);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
        if (audioDevice != null) {
            try {
                audioDevice.dispose();
            } catch (Throwable ignored) {
            }
            audioDevice = null;
        }
    }
}
