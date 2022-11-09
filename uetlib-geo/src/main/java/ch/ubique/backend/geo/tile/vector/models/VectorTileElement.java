package ch.ubique.backend.geo.tile.vector.models;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;


public interface VectorTileElement<T, M> {

    long getId();

    String getLayer();

    /**
     * Return a score for this element. The higher, the more important.
     * The score is used to determine which elements will be included in the
     * vector tile when maxElementsPerTile is set by the given {@link VectorTileConfig}.
     */
    double getScore();

    /**
     * This map will be available in the vector tile as tags.
     * The tags may include all properties needed during rendering.
     * Properties not needed during rendering can be returned by
     * getMetadata, as this makes parsing for the clients easier.
     */
    T getTags();

    /**
     * The metadata will be encoded as json string in the vector tile.
     * This may include all properties that are not needed during rendering.
     * For properties needed during rendering, please use getTags()
     */
    M getMetadata();

    /**
     * Defines if this VectorTileElement is shown at the specified zoom level
     */
    boolean showElement(int zoomLevel);

    /**
     * The actual geometry.
     */
    Geometry getGeometry();

    /**
     * Used to determine which vector tiles potentially contain this VectorTileElement
     */
    default Envelope getBoundingBox() {
        return getGeometry().getEnvelopeInternal();
    }
}
