package com.retro.racer.ui;

import java.util.Locale;

/**
 * Formatting helpers for arcade HUD lap timers, speeds, and distance counters.
 */
public final class TimeFormatter {

    private TimeFormatter() {
    }

    /**
     * Formats elapsed seconds as {@code MM:SS.cc} (e.g. {@code 01:14.38}).
     */
    public static String formatLapTime(float seconds) {
        if (seconds < 0f || Float.isNaN(seconds)) {
            return "--:--.--";
        }
        int totalCentis = (int) Math.floor(seconds * 100f);
        int mins = totalCentis / 6000;
        int secs = (totalCentis / 100) % 60;
        int centis = totalCentis % 100;
        return String.format(Locale.US, "%02d:%02d.%02d", mins, secs, centis);
    }

    /**
     * Formats distance in kilometers with two decimal places (e.g. {@code 1.45 KM}).
     */
    public static String formatDistanceKm(float distanceKm) {
        return String.format(Locale.US, "%.2f KM", Math.max(0f, distanceKm));
    }

    /**
     * Formats vehicle speed as integer KM/H (e.g. {@code 248 KM/H}).
     */
    public static String formatSpeedKmh(float speedKmh) {
        return String.format(Locale.US, "%3d KM/H", Math.round(Math.max(0f, speedKmh)));
    }
}
