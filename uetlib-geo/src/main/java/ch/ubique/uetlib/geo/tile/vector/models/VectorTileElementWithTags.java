package ch.ubique.uetlib.geo.tile.vector.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface VectorTileElementWithTags<T> extends VectorTileElement {

    /**
     * This map will be available in the vector tile as tags. The tags may include all properties
     * needed during rendering. Properties not needed during rendering can be returned by
     * getMetadata, as this makes parsing for the clients easier.
     */
    @JsonProperty
    T tags();
}
