package ch.ubique.backend.geo.mbtile.data;

import ch.ubique.backend.geo.mbtile.TmsTile;
import ch.ubique.backend.geo.mbtile.metadata.constants.MbtileFormat;
import ch.ubique.backend.geo.mbtile.metadata.constants.MetadataKey;
import ch.ubique.backend.geo.mbtile.metadata.json.MetadataJson;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface MbtileDataService {
    void insertReplaceMetadata(MetadataKey key, String value);

    void insertReplaceMetadata(MetadataKey key, int value);

    void insertReplaceMetadataName(String name);

    void insertReplaceMetadataFormat(MbtileFormat format);

    void insertReplaceMetadataJson(MetadataJson json) throws JsonProcessingException;

    void insertTile(TmsTile tiles);

    void insertTiles(List<TmsTile> tiles);
}
