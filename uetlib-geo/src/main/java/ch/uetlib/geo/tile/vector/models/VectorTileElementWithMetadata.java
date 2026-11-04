package ch.uetlib.geo.tile.vector.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface VectorTileElementWithMetadata<M> extends VectorTileElement {

    /**
     * The metadata will be encoded as json string in the vector tile. This may include all
     * properties that are not needed during rendering. For properties needed during rendering,
     * please use getTags()
     */
    @JsonProperty
    M metadata();
}
