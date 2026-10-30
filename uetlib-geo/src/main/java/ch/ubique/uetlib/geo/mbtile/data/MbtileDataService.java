package ch.ubique.uetlib.geo.mbtile.data;

import ch.ubique.uetlib.geo.mbtile.TmsTile;
import ch.ubique.uetlib.geo.mbtile.metadata.constants.MbtileFormat;
import ch.ubique.uetlib.geo.mbtile.metadata.constants.MetadataKey;
import ch.ubique.uetlib.geo.mbtile.metadata.json.MetadataJson;
import java.util.List;
import tools.jackson.core.JacksonException;

public interface MbtileDataService {
    void insertReplaceMetadata(MetadataKey key, String value);

    void insertReplaceMetadata(MetadataKey key, int value);

    void insertReplaceMetadataName(String name);

    void insertReplaceMetadataFormat(MbtileFormat format);

    void insertReplaceMetadataJson(MetadataJson json) throws JacksonException;

    void insertTile(TmsTile tiles);

    void insertTiles(List<TmsTile> tiles);
}
