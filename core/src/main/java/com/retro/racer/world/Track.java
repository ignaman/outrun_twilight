package com.retro.racer.world;

import java.util.Collections;
import java.util.List;

/**
 * Encapsulates a complete, looping pseudo-3D racing track composed of
 * ordered {@link RoadSegment} slices and provides Z-wrapping lookups.
 */
public final class Track {

    private final List<RoadSegment> segments;
    private final float segmentLength;
    private final float roadWidth;
    private final float totalLength;
    private final int lanes;

    public Track(List<RoadSegment> segments, float segmentLength, float roadWidth, int lanes) {
        if (segments == null || segments.isEmpty()) {
            throw new IllegalArgumentException("Track must contain at least one road segment.");
        }
        this.segments = Collections.unmodifiableList(segments);
        this.segmentLength = segmentLength;
        this.roadWidth = roadWidth;
        this.lanes = lanes;
        this.totalLength = segments.size() * segmentLength;
    }

    /**
     * Returns the {@link RoadSegment} containing the specified world Z coordinate,
     * wrapping seamlessly around the looping track.
     */
    public RoadSegment findSegment(float z) {
        float wrapped = wrapZ(z);
        int idx = (int) Math.floor(wrapped / segmentLength);
        idx = ((idx % segments.size()) + segments.size()) % segments.size();
        return segments.get(idx);
    }

    /**
     * Returns the segment at the given index with cyclic wrap-around.
     */
    public RoadSegment getSegment(int index) {
        int size = segments.size();
        int wrapped = ((index % size) + size) % size;
        return segments.get(wrapped);
    }

    public float wrapZ(float z) {
        return ProjectionMath.wrap(z, totalLength);
    }

    public List<RoadSegment> getSegments() {
        return segments;
    }

    public int getSegmentCount() {
        return segments.size();
    }

    public float getSegmentLength() {
        return segmentLength;
    }

    public float getRoadWidth() {
        return roadWidth;
    }

    public float getTotalLength() {
        return totalLength;
    }

    public int getLanes() {
        return lanes;
    }
}
