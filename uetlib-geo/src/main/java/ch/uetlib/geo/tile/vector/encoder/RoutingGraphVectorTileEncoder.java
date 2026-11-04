package ch.uetlib.geo.tile.vector.encoder;

import static java.util.Objects.requireNonNullElse;

import ch.uetlib.geo.graph.routing.models.RoutingGraphTile.Neighbor;
import ch.uetlib.geo.graph.routing.models.RoutingGraphTile.RoutingNeighbor;
import ch.uetlib.geo.graph.routing.models.RoutingGraphTile.Vertex;
import ch.uetlib.geo.graph.routing.protobuf.RoutingGraphTile;
import ch.uetlib.geo.graph.routing.protobuf.RoutingGraphTile.SurfaceType;
import ch.uetlib.geo.tile.common.encoder.TileEncoder;
import ch.uetlib.geo.tile.common.models.Tile;
import ch.uetlib.geo.tile.vector.models.BinaryTile;
import ch.uetlib.geo.tile.vector.models.VectorTile;
import ch.uetlib.geo.tile.vector.models.VectorTileConfig;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class RoutingGraphVectorTileEncoder implements TileEncoder {

    private final VectorTileConfig config;

    protected RoutingGraphVectorTileEncoder(final VectorTileConfig config) {
        this.config = config;
    }

    public static boolean isRoutingGraphTile(final Tile tile) {
        return tile instanceof final VectorTile vectorTile
                && !vectorTile.elements().isEmpty()
                && vectorTile.elements().stream().allMatch(Vertex.class::isInstance);
    }

    @Override
    public final BinaryTile encode(final Tile tile) {
        if (!isRoutingGraphTile(tile)) {
            throw new UnsupportedOperationException(
                    "This encoder can only encode routing graph tiles.");
        }

        final var vectorTile = (VectorTile) tile;
        final var vertices = vectorTile.elements().stream().map(Vertex.class::cast).toList();

        final var pbfTile =
                RoutingGraphTile.newBuilder()
                        .addAllCoordinates(getAllCoordinates(vertices, tile.tileInfo().envelope()))
                        .addAllVertices(vertices.stream().map(this::map).toList())
                        .build();

        return new BinaryTile(tile.tileInfo(), config.outputFormat(), pbfTile.toByteArray());
    }

    private List<RoutingGraphTile.VertexCoordinate> getAllCoordinates(
            final List<Vertex> vertices, final Envelope envelope) {

        final var vertexCoordinates = vertices.stream().map(IdAndCoordinate::new);

        final var neighborCoordinates =
                vertices.stream().flatMap(Vertex::neighborStream).map(IdAndCoordinate::new);

        final var routingNeighborCoordinates =
                vertices.stream().flatMap(Vertex::routingNeighborStream).map(IdAndCoordinate::new);

        return Stream.of(vertexCoordinates, neighborCoordinates, routingNeighborCoordinates)
                .flatMap(Function.identity())
                .distinct()
                .map(idAndCoordinate -> map(idAndCoordinate, envelope))
                .toList();
    }

    private RoutingGraphTile.VertexCoordinate map(
            final IdAndCoordinate idAndCoordinate, final Envelope envelope) {

        final var coordinate = idAndCoordinate.coordinate();

        // Top left corner
        final var x0 =
                envelope.getMinX(); // All used coordinate systems increase towards east (right)
        final var y0 =
                envelope.getMaxY(); // All used coordinate systems increase towards north (up)

        final var x = coordinate.getX() - x0;
        final var y = coordinate.getY() - y0;

        final var builder =
                RoutingGraphTile.VertexCoordinate.newBuilder()
                        .setIdentifier((int) idAndCoordinate.id())
                        .setX((float) x)
                        .setY((float) y);

        if (coordinate.getZ() != Coordinate.NULL_ORDINATE) {
            builder.setElevation((float) coordinate.getZ());
        }

        return builder.build();
    }

    private RoutingGraphTile.Vertex map(final Vertex vertex) {
        final var builder =
                RoutingGraphTile.Vertex.newBuilder()
                        .setIdentifier((int) vertex.id())
                        .setIsHighway(vertex.isHighway());

        // Set (routing) neighbors
        builder.addAllNeighbors(vertex.neighbors().stream().map(this::map).toList());
        builder.addAllRoutingNeighbors(vertex.routingNeighbors().stream().map(this::map).toList());

        return builder.build();
    }

    private RoutingGraphTile.Neighbor map(final Neighbor neighbor) {
        return RoutingGraphTile.Neighbor.newBuilder()
                .setIdentifier((int) neighbor.id())
                .setIsHighway(neighbor.isHighway())
                .setWeight(neighbor.weight())
                .setSurfaceType(requireNonNullElse(neighbor.surfaceType(), SurfaceType.UNKNOWN))
                .build();
    }

    private RoutingGraphTile.RoutingNeighbor map(final RoutingNeighbor routingNeighbor) {
        return RoutingGraphTile.RoutingNeighbor.newBuilder()
                .setIdentifier((int) routingNeighbor.id())
                .setIsHighway(routingNeighbor.isHighway())
                .setWeight(routingNeighbor.weight())
                .setDirection((int) routingNeighbor.direction())
                .build();
    }

    private record IdAndCoordinate(long id, Coordinate coordinate) {

        IdAndCoordinate(final Vertex vertex) {
            this(vertex.id(), vertex.coordinate());
        }

        IdAndCoordinate(final Neighbor neighbor) {
            this(neighbor.id(), neighbor.coordinate());
        }

        IdAndCoordinate(final RoutingNeighbor routingNeighbor) {
            this(routingNeighbor.id(), routingNeighbor.coordinate());
        }
    }
}
