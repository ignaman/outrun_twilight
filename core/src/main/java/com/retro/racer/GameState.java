package com.retro.racer;

/**
 * Enum state machine governing the top-level race lifecycle.
 */
public enum GameState {
    START("OUTRUN TWILIGHT", "PRESS ENTER TO RACE", false, true),
    RACING("", "", true, false),
    PAUSED("PAUSED", "PRESS P OR ENTER TO RESUME", false, true),
    FINISHED("RACE COMPLETE!", "PRESS ENTER OR R TO RACE AGAIN", false, true);

    private final String overlayTitle;
    private final String overlaySubtitle;
    private final boolean simulationActive;
    private final boolean overlayVisible;

    GameState(String overlayTitle, String overlaySubtitle, boolean simulationActive, boolean overlayVisible) {
        this.overlayTitle = overlayTitle;
        this.overlaySubtitle = overlaySubtitle;
        this.simulationActive = simulationActive;
        this.overlayVisible = overlayVisible;
    }

    public String getOverlayTitle() {
        return overlayTitle;
    }

    public String getOverlaySubtitle() {
        return overlaySubtitle;
    }

    public boolean isSimulationActive() {
        return simulationActive;
    }

    public boolean isOverlayVisible() {
        return overlayVisible;
    }
}
