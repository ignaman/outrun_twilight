package com.retro.racer.world;

import com.retro.racer.entities.ChevronSign;
import com.retro.racer.entities.Streetlight;
import com.retro.racer.graphics.AssetFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent Builder for assembling looping pseudo-3D tracks from straights,
 * curves, hills, S-curves, and automatically placed roadside props.
 */
public final class TrackBuilder {

    public static final float DEFAULT_SEGMENT_LENGTH = 200f;
    public static final float DEFAULT_ROAD_WIDTH = 1180f;
    public static final int DEFAULT_RUMBLE_LENGTH = 3;
    public static final int DEFAULT_LANES = 3;

    private final List<RoadSegment> segments = new ArrayList<RoadSegment>();
    private float segmentLength = DEFAULT_SEGMENT_LENGTH;
    private float roadWidth = DEFAULT_ROAD_WIDTH;
    private int rumbleLength = DEFAULT_RUMBLE_LENGTH;
    private int lanes = DEFAULT_LANES;
    private float currentY = 0f;
    private AssetFactory propAssetFactory;

    public TrackBuilder segmentLength(float segmentLength) {
        this.segmentLength = segmentLength;
        return this;
    }

    public TrackBuilder roadWidth(float roadWidth) {
        this.roadWidth = roadWidth;
        return this;
    }

    public TrackBuilder rumbleLength(int rumbleLength) {
        this.rumbleLength = Math.max(1, rumbleLength);
        return this;
    }

    public TrackBuilder lanes(int lanes) {
        this.lanes = Math.max(1, lanes);
        return this;
    }

    /**
     * Appends a flat straight stretch of the given segment count.
     */
    public TrackBuilder addStraight(int length) {
        int safeLen = Math.max(1, length);
        addRoad(safeLen / 4, safeLen / 2, safeLen - (safeLen / 4) - (safeLen / 2), 0f, 0f);
        return this;
    }

    /**
     * Appends a smooth curved section (positive curve = right turn, negative = left turn).
     */
    public TrackBuilder addCurve(int length, float curve) {
        int safeLen = Math.max(4, length);
        int enter = Math.max(2, safeLen / 4);
        int leave = Math.max(2, safeLen / 4);
        int hold = Math.max(1, safeLen - enter - leave);
        addRoad(enter, hold, leave, curve, 0f);
        return this;
    }

    /**
     * Appends a smooth hill that rises by {@code height} world units (positive = uphill, negative = downhill).
     */
    public TrackBuilder addHill(int length, float height) {
        int safeLen = Math.max(4, length);
        int enter = Math.max(2, safeLen / 4);
        int leave = Math.max(2, safeLen / 4);
        int hold = Math.max(1, safeLen - enter - leave);
        addRoad(enter, hold, leave, 0f, height);
        return this;
    }

    /**
     * Appends a combined curved hill section.
     */
    public TrackBuilder addHillCurve(int length, float curve, float height) {
        int safeLen = Math.max(4, length);
        int enter = Math.max(2, safeLen / 4);
        int leave = Math.max(2, safeLen / 4);
        int hold = Math.max(1, safeLen - enter - leave);
        addRoad(enter, hold, leave, curve, height);
        return this;
    }

    /**
     * Appends an OutRun-style S-curve (left then right, or right then left depending on sign).
     */
    public TrackBuilder addSCurve(int sectionLength, float curve) {
        int part = Math.max(12, sectionLength / 3);
        addRoad(part / 3, part, part / 3, -curve, 800f);
        addRoad(part / 3, part, part / 3, curve * 1.15f, -1200f);
        addRoad(part / 3, part, part / 3, -curve * 0.85f, 400f);
        return this;
    }

    /**
     * Appends a sequence of rolling hills for classic OutRun cresting moments.
     */
    public TrackBuilder addRollingHills(int count, int hillLength, float amplitude) {
        int half = Math.max(10, hillLength / 2);
        for (int i = 0; i < count; i++) {
            addHill(half, amplitude);
            addHill(half, -amplitude);
        }
        return this;
    }

    /**
     * Core eased road section generator with enter, hold, and leave transitions.
     */
    public TrackBuilder addRoad(int enter, int hold, int leave, float curve, float deltaY) {
        float startY = currentY;
        float endY = startY + deltaY;
        int total = Math.max(1, enter + hold + leave);

        for (int n = 0; n < enter; n++) {
            float c = ProjectionMath.easeIn(0f, curve, (float) n / Math.max(1, enter));
            float y = ProjectionMath.easeInOut(startY, endY, (float) n / total);
            appendSegment(c, y);
        }
        for (int n = 0; n < hold; n++) {
            float y = ProjectionMath.easeInOut(startY, endY, (float) (enter + n) / total);
            appendSegment(curve, y);
        }
        for (int n = 0; n < leave; n++) {
            float c = ProjectionMath.easeOut(curve, 0f, (float) n / Math.max(1, leave));
            float y = ProjectionMath.easeInOut(startY, endY, (float) (enter + hold + n) / total);
            appendSegment(c, y);
        }
        currentY = endY;
        return this;
    }

    private void appendSegment(float curve, float nextY) {
        int index = segments.size();
        float prevY = index == 0 ? 0f : segments.get(index - 1).getP2().getWorldY();
        float z1 = index * segmentLength;
        float z2 = (index + 1) * segmentLength;

        ProjectedPoint p1 = new ProjectedPoint(0f, prevY, z1);
        ProjectedPoint p2 = new ProjectedPoint(0f, nextY, z2);

        boolean darkBand = ((index / rumbleLength) % 2) == 1;
        SegmentColorScheme scheme;
        if (index >= 5 && index <= 7) {
            scheme = SegmentColorScheme.START_FINISH;
        } else {
            scheme = darkBand ? SegmentColorScheme.DARK : SegmentColorScheme.LIGHT;
        }

        segments.add(new RoadSegment(index, p1, p2, curve, scheme));
    }

    /**
     * Configures the builder to automatically populate streetlights along both edges
     * and reflective red-and-white chevron signs along the outside of curves upon {@link #build()}.
     */
    public TrackBuilder populateProps(AssetFactory assets) {
        this.propAssetFactory = assets;
        return this;
    }

    private void attachProps(AssetFactory assets) {
        if (assets == null) {
            return;
        }
        int total = segments.size();
        for (int i = 0; i < total; i++) {
            RoadSegment seg = segments.get(i);
            float segZ = seg.getP1().getWorldZ();

            // Place paired twilight streetlights rhythmically along the highway shoulders
            if (i % 14 == 4) {
                seg.addProp(new Streetlight(segZ, -1.24f, true, assets.getStreetlightSprite(true)));
                seg.addProp(new Streetlight(segZ, 1.24f, false, assets.getStreetlightSprite(false)));
            }

            // Place chevron warning signs on the outside of sharp curves
            float absCurve = Math.abs(seg.getCurve());
            if (absCurve >= 2.2f && (i % 6 == 0)) {
                if (seg.getCurve() > 0f) {
                    // Right turn -> place right-pointing chevrons on the outer LEFT edge
                    seg.addProp(new ChevronSign(
                            segZ,
                            -1.36f,
                            ChevronSign.Direction.RIGHT,
                            assets.getChevronSignSprite(true)
                    ));
                } else {
                    // Left turn -> place left-pointing chevrons on the outer RIGHT edge
                    seg.addProp(new ChevronSign(
                            segZ,
                            1.36f,
                            ChevronSign.Direction.LEFT,
                            assets.getChevronSignSprite(false)
                    ));
                }
            }
        }
    }

    /**
     * Finalizes the looping track, gently closing any residual elevation delta back to y = 0
     * so the finish-line loop has zero vertical seam.
     */
    public Track build() {
        if (segments.isEmpty()) {
            addStraight(60);
        }
        if (Math.abs(currentY) > 1f) {
            addHill(40, -currentY);
        }
        // Ensure exact closure to y = 0 on the very last segment
        RoadSegment last = segments.get(segments.size() - 1);
        last.getP2().setWorldY(0f);

        if (propAssetFactory != null) {
            attachProps(propAssetFactory);
        }

        return new Track(new ArrayList<RoadSegment>(segments), segmentLength, roadWidth, lanes);
    }

    /**
     * Convenience factory creating the default OutRun Twilight Circuit.
     */
    public static Track buildDefaultCircuit(AssetFactory assets) {
        return new TrackBuilder()
                .segmentLength(DEFAULT_SEGMENT_LENGTH)
                .roadWidth(DEFAULT_ROAD_WIDTH)
                .rumbleLength(DEFAULT_RUMBLE_LENGTH)
                .lanes(DEFAULT_LANES)
                .addStraight(45)
                .addCurve(55, 2.8f)
                .addHill(45, 2400f)
                .addHillCurve(65, -3.4f, -1400f)
                .addStraight(30)
                .addRollingHills(2, 50, 1800f)
                .addSCurve(90, 3.8f)
                .addHillCurve(70, 4.2f, 2200f)
                .addStraight(35)
                .addHillCurve(75, -3.6f, -3200f)
                .addCurve(50, 2.5f)
                .addStraight(50)
                .populateProps(assets)
                .build();
    }
}
