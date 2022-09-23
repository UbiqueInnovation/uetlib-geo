package ch.ubique.backend.geo.osm.parser.osmosis;

import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OsmUtils {

    private OsmUtils() {}

    public static Map<String, String> getTags(Entity entity) {
        return entity.getTags().stream().collect(Collectors.toMap(Tag::getKey, Tag::getValue));
    }

    public static WGS84Coordinate getCoordinate(Node node) {
        return new WGS84Coordinate(node.getLatitude(), node.getLongitude());
    }

    public static <T> Collection<T> parseNodes(InputStream inputStream, Function<Node, Optional<T>> nodeMapper) {
        final var results = new ArrayList<T>();
        OsmParser.from(inputStream)
                .onNode(node -> {
                    final var result = nodeMapper.apply(node);
                    result.ifPresent(results::add);
                })
                .parse();
        return results;
    }
}
