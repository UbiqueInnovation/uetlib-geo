package ch.ubique.backend.geo.osm.parser.geometry;

import static ch.ubique.backend.geo.coordinate.util.JtsConverter.WGS84;

import ch.ubique.backend.geo.graph.routing.models.LineWithInfo;
import ch.ubique.backend.geo.osm.parser.osmosis.OsmParser;
import ch.ubique.backend.geo.osm.parser.osmosis.OsmUtils;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

public class OsmLinesParser {

    public static List<LineWithInfo<Map<String, String>>> parse(InputStream inputStream) {
        var osmParser = new OsmLinesParser(inputStream);
        return osmParser.parse();
    }

    private final GeometryFactory geometryFactory;

    private final InputStream inputStream;

    private final Map<Long, Coordinate> nodes;

    private final List<LineWithInfo<Map<String, String>>> ways;

    private OsmLinesParser(InputStream inputStream) {
        this.inputStream = inputStream;
        this.geometryFactory = WGS84.geometryFactory();
        this.nodes = new HashMap<>();
        this.ways = new ArrayList<>();
    }

    private List<LineWithInfo<Map<String, String>>> parse() {
        OsmParser.from(inputStream).onNode(this::onNode).onWay(this::onWay).parse();
        return ways;
    }

    private void onNode(Node node) {
        nodes.put(node.getId(), OsmUtils.getCoordinate(node).toJts());
    }

    private void onWay(Way way) {
        final var tags = OsmUtils.getTags(way);
        final var coordinates =
                way.getWayNodes().stream()
                        .map(WayNode::getNodeId)
                        .map(nodes::get)
                        .toArray(Coordinate[]::new);

        final var line = geometryFactory.createLineString(coordinates);
        ways.add(new LineWithInfo<>(line, tags));
    }
}
