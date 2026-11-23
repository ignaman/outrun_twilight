package com.retro.racer.ui;

import com.retro.racer.GameState;

/**
 * Encapsulates retro arcade overlay animation state (blinking prompts & state messaging).
 */
public final class OverlayBanner {

    private static final float BLINK_PERIOD = 0.85f;
    private float blinkTimer;

    public void update(float delta) {
        blinkTimer = (blinkTimer + delta) % BLINK_PERIOD;
    }

    public boolean isPromptVisible() {
        return blinkTimer < (BLINK_PERIOD * 0.65f);
    }

    public String getTitle(GameState state) {
        return state.getOverlayTitle();
    }

    public String getSubtitle(GameState state) {
        return state.getOverlaySubtitle();
    }

    public String getControlsHint() {
        return "W/UP: GAS   S/DOWN: BRAKE   A/D: STEER   P: PAUSE   R: RESET   M: MUTE";
    }
}
