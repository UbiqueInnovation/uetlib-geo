package ch.ubique.backend.geo.osm.parser.osmosis;

import ch.ubique.backend.core.io.InputStreamProvider;
import ch.ubique.backend.geo.coordinate.constants.CoordinateSystems;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.coordinate.util.JtsConverter;
import ch.ubique.backend.geo.coordinate.util.JtsUtil;

import com.google.common.base.Predicates;

import org.apache.commons.lang3.function.Consumers;
import org.jspecify.annotations.Nullable;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class OsmParserWithStore {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<Long, WGS84Coordinate> nodeCoordinates;
    private final Map<Long, List<Long>> wayNodeIds;

    private Consumer<Way> onWay = Consumers.nop();
    private Consumer<Node> onNode = Consumers.nop();
    private Consumer<Relation> onRelation = Consumers.nop();

    private Predicate<Node> needsNode = Predicates.alwaysFalse();
    private Predicate<Way> needsWay = Predicates.alwaysFalse();
    private Predicate<Relation> needsRelation = Predicates.alwaysFalse();

    private final Collection<Long> neededNodes;
    private final Collection<Long> neededWays;

    public OsmParserWithStore() {
        nodeCoordinates = new HashMap<>();
        wayNodeIds = new HashMap<>();
        neededWays = new HashSet<>();
        neededNodes = new HashSet<>();
    }

    public OsmParserWithStore needsNode(final Predicate<Node> needsNode) {
        this.needsNode = needsNode;
        return this;
    }

    public OsmParserWithStore needsWay(final Predicate<Way> needsWay) {
        this.needsWay = needsWay;
        return this;
    }

    public OsmParserWithStore needsRelation(final Predicate<Relation> needsRelation) {
        this.needsRelation = needsRelation;
        return this;
    }

    public OsmParserWithStore onNode(final Consumer<Node> onNode) {
        this.onNode = onNode;
        return this;
    }

    public OsmParserWithStore onWay(final Consumer<Way> onWay) {
        this.onWay = onWay;
        return this;
    }

    public OsmParserWithStore onRelation(final Consumer<Relation> onRelation) {
        this.onRelation = onRelation;
        return this;
    }

    public void parse(final InputStreamProvider inputStreamProvider) throws IOException {
        final var numWorkers = Runtime.getRuntime().availableProcessors();
        try (final var fileProvider = inputStreamProvider.toFileProvider()) {

            // 1st pass: save what is needed for relations
            logger.info("Checking requirements of relations");
            try (final var inputStream = fileProvider.inputStream()) {
                OsmParser.from(inputStream).onRelation(this::saveNeeded).parse(numWorkers);
            }

            // 2nd pass: save what is needed for ways
            logger.info("Checking requirements of ways");
            try (final var inputStream = fileProvider.inputStream()) {
                OsmParser.from(inputStream).onWay(this::saveNeeded).parse(numWorkers);
            }

            // 3rd pass: actual parsing
            logger.info("Starting parsing");
            try (final var inputStream = fileProvider.inputStream()) {
                OsmParser.from(inputStream)
                        .onNode(this::onNode)
                        .onWay(onWay)
                        .onRelation(onRelation)
                        .parse(numWorkers);
            }
        }
    }

    private void saveNeeded(final Relation relation) {
        if (!needsRelation.test(relation)) {
            return;
        }

        for (final var member : relation.getMembers()) {
            if (member.getMemberType() == EntityType.Node) {
                neededNodes.add(member.getMemberId());
            }
            if (member.getMemberType() == EntityType.Way) {
                neededWays.add(member.getMemberId());
            }
        }
    }

    private void saveNeeded(final Way way) {
        if (neededWays.contains(way.getId()) || needsWay.test(way)) {
            final var nodeIds = way.getWayNodes().stream().map(WayNode::getNodeId).toList();
            neededNodes.addAll(nodeIds);
            wayNodeIds.put(way.getId(), nodeIds);
        }
    }

    private void saveNeeded(final Node node) {
        if (needsNode.test(node) || neededNodes.contains(node.getId())) {
            nodeCoordinates.put(node.getId(), OsmUtils.getCoordinate(node));
        }
    }

    private void onNode(final Node node) {
        saveNeeded(node);
        onNode.accept(node);
    }

    public @Nullable WGS84Coordinate getNodeCoordinate(final long id) {
        return nodeCoordinates.get(id);
    }

    public @Nullable WGS84Coordinate getRepresentativeCoordinate(final Entity entity) {
        return switch (entity) {
            case final Node node -> OsmUtils.getCoordinate(node);
            case final Way way -> getRepresentativeCoordinate(way);
            case final Relation relation -> getRepresentativeCoordinate(relation);
            case null, default -> throw new IllegalStateException("Unexpected value: " + entity);
        };
    }

    private @Nullable WGS84Coordinate getRepresentativeCoordinate(final Way way) {
        final var lineString = createLineString(way.getId());
        if (lineString == null) {
            return null;
        }
        return getRepresentativeCoordinate(lineString);
    }

    private static WGS84Coordinate getRepresentativeCoordinate(final LineString lineString) {
        final var centroid = lineString.getCentroid().getCoordinate();
        // Project linestring onto the closest segment
        return JtsConverter.WGS84.toBackendLib(
                JtsUtil.segments(lineString).stream()
                        .min(Comparator.comparing(segment -> segment.distance(centroid)))
                        .orElseThrow()
                        .project(centroid));
    }

    private @Nullable WGS84Coordinate getRepresentativeCoordinate(final Relation relation) {
        final var geometry = getMultiPolygon(relation);
        if (geometry.isEmpty()) {
            return relation.getMembers().stream()
                    .map(this::getRepresentativeCoordinate)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }
        return JtsConverter.WGS84.toBackendLib(JtsUtil.representativeCoordinate(geometry));
    }

    private @Nullable WGS84Coordinate getRepresentativeCoordinate(final RelationMember member) {
        final var lineString = createLineString(member);
        if (lineString != null) {
            return getRepresentativeCoordinate(lineString);
        }
        final WGS84Coordinate coordinate;
        if (member.getMemberType() == EntityType.Node
                && (coordinate = getNodeCoordinate(member.getMemberId())) != null) {
            return coordinate;
        }
        return null;
    }

    public MultiPolygon getMultiPolygon(final Relation relation) {
        final var polygonizer = new Polygonizer();
        final var polygonRoles = Set.of("inner", "outer");
        for (final var member : relation.getMembers()) {
            final var memberRole = member.getMemberRole();
            final LineString lineString;
            if (member.getMemberType() == EntityType.Way
                    && polygonRoles.contains(memberRole)
                    && (lineString = createLineString(member.getMemberId())) != null) {
                polygonizer.add(lineString);
            }
        }
        //noinspection unchecked
        return JtsUtil.toJtsMultiPolygon(polygonizer.getPolygons());
    }

    public @Nullable LineString createLineString(final Way way) {
        return createLineString(way.getId());
    }

    public @Nullable LineString createLineString(final RelationMember member) {
        if (member.getMemberType() != EntityType.Way) {
            return null;
        }
        return createLineString(member.getMemberId());
    }

    private @Nullable LineString createLineString(final long wayId) {
        final var nodeIds = wayNodeIds.get(wayId);
        if (nodeIds == null) {
            return null;
        }
        final var coordinates = nodeIds.stream().map(this::getNodeCoordinate).toList();
        if (coordinates.isEmpty() || coordinates.stream().anyMatch(Objects::isNull)) {
            return null;
        }
        return new GeometryFactory(new PrecisionModel(), CoordinateSystems.WGS84.srid())
                .createLineString(
                        coordinates.stream().map(Objects::requireNonNull)
                                .map(WGS84Coordinate::toJts)
                                .toArray(Coordinate[]::new));
    }

    public boolean hasWayNodes(final Way way) {
        return hasWayNodes(way.getId());
    }

    public boolean hasWayNodes(final RelationMember member) {
        return member.getMemberType() == EntityType.Way && hasWayNodes(member.getMemberId());
    }

    private boolean hasWayNodes(final long wayId) {
        return wayNodeIds.containsKey(wayId);
    }
}
