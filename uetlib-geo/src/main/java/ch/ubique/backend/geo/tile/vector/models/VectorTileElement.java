package ch.ubique.backend.geo.tile.vector.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.geotools.referencing.operation.builder.MissingInfoException;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


public interface VectorTileElement<T, M> {

    long id();

    /**
     * The layer this feature is exported to on the vector tile.
     */
    String layer();

    /**
     * Return a score for this element. The higher, the more important.
     * The score is used to determine which elements will be included in the
     * vector tile when maxElementsPerTile is set by the given {@link VectorTileConfig}.
     */
    @JsonIgnore
    double score();

    /**
     * This map will be available in the vector tile as tags.
     * The tags may include all properties needed during rendering.
     * Properties not needed during rendering can be returned by
     * getMetadata, as this makes parsing for the clients easier.
     */
    T tags();

    /**
     * The metadata will be encoded as json string in the vector tile.
     * This may include all properties that are not needed during rendering.
     * For properties needed during rendering, please use getTags()
     */
    M metadata();

    /**
     * Defines if this VectorTileElement is shown at the specified zoom level
     */
    @JsonIgnore
    boolean showElement(int zoomLevel);

    /**
     * The actual geometry.
     * Please make sure to set the correct Spatial Reference ID (SRID) on the geometry.
     * It is needed to infer the {@link CoordinateReferenceSystem} used for projecting the geometry onto the vector tile.
     * If not set a {@link MissingInfoException} will be thrown when generating vector tiles.
     */
    @JsonIgnore
    Geometry geometry();

    /**
     * Used to determine which vector tiles potentially contain this VectorTileElement
     */
    @JsonIgnore
    default Envelope boundingBox() {
        return geometry().getEnvelopeInternal();
    }
}
