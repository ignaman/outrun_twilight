package com.retro.racer.world;

/**
 * Static math utilities for 3D-to-2D pseudo-3D projection, easing curves,
 * interpolation, and distance fog calculations.
 */
public final class ProjectionMath {

    private ProjectionMath() {
        // Utility class
    }

    /**
     * Projects a 3D world point into 2D screen coordinates (libGDX Y-up convention:
     * y = 0 at bottom of viewport, y = screenHeight at top).
     */
    public static void project(
            ProjectedPoint point,
            float cameraX,
            float cameraY,
            float cameraZ,
            float cameraDepth,
            float screenWidth,
            float screenHeight,
            float roadWidth
    ) {
        float camX = point.getWorldX() - cameraX;
        float camY = point.getWorldY() - cameraY;
        float camZ = point.getWorldZ() - cameraZ;

        point.setCameraCoords(camX, camY, camZ);

        if (camZ <= 0.001f) {
            point.setScreenCoords(0f, 0f, 0f, 0f);
            return;
        }

        float scale = cameraDepth / camZ;
        float halfW = screenWidth * 0.5f;
        float halfH = screenHeight * 0.5f;

        float screenX = halfW + (scale * camX * halfW);
        float screenY = halfH + (scale * camY * halfH);
        float screenW = scale * roadWidth * halfW;

        point.setScreenCoords(screenX, screenY, screenW, scale);
    }

    public static float easeIn(float a, float b, float percent) {
        return a + (b - a) * (percent * percent);
    }

    public static float easeOut(float a, float b, float percent) {
        float inv = 1f - percent;
        return a + (b - a) * (1f - inv * inv);
    }

    public static float easeInOut(float a, float b, float percent) {
        return a + (b - a) * (float) ((-Math.cos(percent * Math.PI) * 0.5) + 0.5);
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float percentRemaining(float value, float total) {
        return (value % total) / total;
    }

    public static float wrap(float value, float max) {
        if (max <= 0f) {
            return 0f;
        }
        float result = value % max;
        return result < 0f ? result + max : result;
    }

    /**
     * Exponential fog density factor in [0, 1] where 1 means clear and 0 means full fog.
     */
    public static float exponentialFog(float distanceRatio, float density) {
        float x = distanceRatio * density;
        return (float) (1.0 / Math.exp(x * x));
    }
}
