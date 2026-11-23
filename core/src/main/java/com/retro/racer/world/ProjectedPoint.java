package com.retro.racer.world;

/**
 * Encapsulates a point along a road segment across world space,
 * camera space, and projected 2D screen space.
 */
public final class ProjectedPoint {

    private float worldX;
    private float worldY;
    private float worldZ;

    private float camX;
    private float camY;
    private float camZ;

    private float screenX;
    private float screenY;
    private float screenW;
    private float screenScale;

    public ProjectedPoint(float worldX, float worldY, float worldZ) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.worldZ = worldZ;
    }

    public void setCameraCoords(float camX, float camY, float camZ) {
        this.camX = camX;
        this.camY = camY;
        this.camZ = camZ;
    }

    public void setScreenCoords(float screenX, float screenY, float screenW, float screenScale) {
        this.screenX = screenX;
        this.screenY = screenY;
        this.screenW = screenW;
        this.screenScale = screenScale;
    }

    public float getWorldX() {
        return worldX;
    }

    public float getWorldY() {
        return worldY;
    }

    public void setWorldY(float worldY) {
        this.worldY = worldY;
    }

    public float getWorldZ() {
        return worldZ;
    }

    public float getCamX() {
        return camX;
    }

    public float getCamY() {
        return camY;
    }

    public float getCamZ() {
        return camZ;
    }

    public float getScreenX() {
        return screenX;
    }

    public float getScreenY() {
        return screenY;
    }

    public float getScreenW() {
        return screenW;
    }

    public float getScreenScale() {
        return screenScale;
    }
}
