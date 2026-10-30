package ch.ubique.uetlib.geo.tile.vector.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.operation.builder.MissingInfoException;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

public interface VectorTileElement {

    /** Optionally, set an id for the tile element. Zero is considered null. */
    @JsonProperty
    default long id() {
        return 0;
    }

    /** The layer this feature is exported to on the vector tile. */
    @JsonProperty
    String layer();

    /**
     * Return a score for this element. The higher, the more important. The score is used to
     * determine which elements will be included in the vector tile when maxElementsPerTile is set
     * by the given {@link VectorTileConfig}.
     */
    @JsonIgnore
    default double score() {
        return 0;
    }

    /** Defines if this VectorTileElement is shown at the specified zoom level */
    @JsonIgnore
    default boolean showElement(final int zoomLevel) {
        return true;
    }

    /**
     * Given the computed tile geometry, return true if this element should be included in the
     * output.
     *
     * <p>One use case of this method could be: we only want to output lines, during clipping, a
     * line collapses to a point, we do not want to include points.
     *
     * @param tileGeometry the geometry as computed to be outputted to the tile
     */
    @JsonIgnore
    default boolean includeForOutput(final Geometry tileGeometry) {
        return true;
    }

    /** Used to determine which vector tiles potentially contain this VectorTileElement */
    @JsonIgnore
    default Envelope boundingBox() {
        return geometry().getEnvelopeInternal();
    }

    /**
     * The actual geometry. Please make sure to set the correct Spatial Reference ID (SRID) on the
     * geometry. It is needed to infer the {@link CoordinateReferenceSystem} used for projecting the
     * geometry onto the vector tile. If not set a {@link MissingInfoException} will be thrown when
     * generating vector tiles.
     */
    @JsonIgnore
    Geometry geometry();
}
