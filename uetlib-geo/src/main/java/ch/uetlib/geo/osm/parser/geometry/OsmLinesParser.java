package ch.uetlib.geo.osm.parser.geometry;

import ch.uetlib.geo.coordinate.constants.CoordinateSystems;
import ch.uetlib.geo.coordinate.util.JtsConverter;
import ch.uetlib.geo.graph.routing.models.LineWithInfo;
import ch.uetlib.geo.osm.parser.osmosis.OsmParser;
import ch.uetlib.geo.osm.parser.osmosis.OsmUtils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OsmLinesParser {

    private final GeometryFactory geometryFactory;
    private final InputStream inputStream;
    private final Map<Long, Coordinate> coordinates;
    private final List<LineWithInfo<Map<String, String>>> lines;

    private final JtsConverter<?> coordinateConverter;

    public OsmLinesParser(
            final InputStream inputStream, final JtsConverter<?> coordinateConverter) {
        this.inputStream = inputStream;
        this.coordinateConverter = coordinateConverter;
        this.geometryFactory = CoordinateSystems.WGS84.geometryFactory();
        this.coordinates = new HashMap<>();
        this.lines = new ArrayList<>();
    }

    private OsmLinesParser(final InputStream inputStream) {
        this(inputStream, JtsConverter.WGS84);
    }

    public static List<LineWithInfo<Map<String, String>>> parse(
            final InputStream inputStream, final JtsConverter<?> coordinateConverter) {
        final var osmParser = new OsmLinesParser(inputStream, coordinateConverter);
        return osmParser.parse();
    }

    public static List<LineWithInfo<Map<String, String>>> parse(final InputStream inputStream) {
        final var osmParser = new OsmLinesParser(inputStream);
        return osmParser.parse();
    }

    private List<LineWithInfo<Map<String, String>>> parse() {
        OsmParser.from(inputStream).onNode(this::onNode).onWay(this::onWay).parse();
        return lines;
    }

    private void onNode(final Node node) {
        coordinates.put(node.getId(), parseCoordinate(node));
    }

    private Coordinate parseCoordinate(final Node node) {
        final var wgs84Coordinate = OsmUtils.getCoordinate(node).toJts();
        return coordinateConverter.transform(wgs84Coordinate, CoordinateSystems.WGS84.srid());
    }

    private void onWay(final Way way) {

        final var lineCoordinates =
                way.getWayNodes().stream()
                        .map(WayNode::getNodeId)
                        .map(coordinates::get)
                        .distinct()
                        .toArray(Coordinate[]::new);

        if (lineCoordinates.length < 2) {
            return; // A non-empty and valid line needs at least two points
        }

        final var line = geometryFactory.createLineString(lineCoordinates);
        final var tags = OsmUtils.getTags(way);

        lines.add(new LineWithInfo<>(line, tags));
    }
}
