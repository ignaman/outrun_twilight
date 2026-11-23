package com.retro.racer.world;

import com.retro.racer.entities.RoadsideProp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single longitudinal slice of the pseudo-3D road, encapsulating
 * its start/end projected points, curvature, elevation, hill-crest clip horizon,
 * color palette, and attached roadside props.
 */
public final class RoadSegment {

    private final int index;
    private final ProjectedPoint p1;
    private final ProjectedPoint p2;
    private final float curve;
    private final SegmentColorScheme colorScheme;
    private final List<RoadsideProp> props;

    private float clipY;
    private float fogFactor;
    private boolean visible;

    public RoadSegment(
            int index,
            ProjectedPoint p1,
            ProjectedPoint p2,
            float curve,
            SegmentColorScheme colorScheme
    ) {
        this.index = index;
        this.p1 = p1;
        this.p2 = p2;
        this.curve = curve;
        this.colorScheme = colorScheme;
        this.props = new ArrayList<RoadsideProp>();
        this.clipY = 0f;
        this.fogFactor = 1f;
        this.visible = false;
    }

    public int getIndex() {
        return index;
    }

    public ProjectedPoint getP1() {
        return p1;
    }

    public ProjectedPoint getP2() {
        return p2;
    }

    public float getCurve() {
        return curve;
    }

    /**
     * Returns the average world elevation Y of this segment.
     */
    public float getElevationY() {
        return (p1.getWorldY() + p2.getWorldY()) * 0.5f;
    }

    /**
     * Returns the vertical elevation rise across this segment (positive = uphill).
     */
    public float getSlope() {
        return p2.getWorldY() - p1.getWorldY();
    }

    public SegmentColorScheme getColorScheme() {
        return colorScheme;
    }

    public void addProp(RoadsideProp prop) {
        if (prop != null) {
            props.add(prop);
        }
    }

    public List<RoadsideProp> getProps() {
        return Collections.unmodifiableList(props);
    }

    public float getClipY() {
        return clipY;
    }

    public void setClipY(float clipY) {
        this.clipY = clipY;
    }

    public float getFogFactor() {
        return fogFactor;
    }

    public void setFogFactor(float fogFactor) {
        this.fogFactor = fogFactor;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
