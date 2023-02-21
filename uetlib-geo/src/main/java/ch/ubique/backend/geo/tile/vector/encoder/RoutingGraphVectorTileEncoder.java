package ch.ubique.backend.geo.tile.vector.encoder;

import ch.ubique.backend.geo.graph.routing.models.RoutingGraphTile.Neighbor;
import ch.ubique.backend.geo.graph.routing.models.RoutingGraphTile.RoutingNeighbor;
import ch.ubique.backend.geo.graph.routing.models.RoutingGraphTile.Vertex;
import ch.ubique.backend.geo.graph.routing.protobuf.RoutingGraphTile;
import ch.ubique.backend.geo.tile.common.encoder.TileEncoder;
import ch.ubique.backend.geo.tile.common.models.Tile;
import ch.ubique.backend.geo.tile.vector.models.BinaryTile;
import ch.ubique.backend.geo.tile.vector.models.VectorTile;
import ch.ubique.backend.geo.tile.vector.models.VectorTileConfig;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElement;
import org.locationtech.jts.geom.Coordinate;

public class RoutingGraphVectorTileEncoder implements TileEncoder {

    private final VectorTileConfig config;

    protected RoutingGraphVectorTileEncoder(final VectorTileConfig config) {
        this.config = config;
    }

    public static boolean isRoutingGraphTile(final Tile tile) {
        return tile instanceof VectorTile vectorTile
                && vectorTile.elements().stream().allMatch(Vertex.class::isInstance);
    }

    @Override
    public final BinaryTile encode(final Tile tile) {
        if (!isRoutingGraphTile(tile)) {
            throw new UnsupportedOperationException(
                    "This encoder can only encode routing graph tiles.");
        }

        final var vectorTile = (VectorTile) tile;

        final var pbfTile =
                RoutingGraphTile.newBuilder()
                        .addAllVertices(vectorTile.elements().stream().map(this::map).toList())
                        .build();

        return new BinaryTile(tile.tileInfo(), config.getOutputFormat(), pbfTile.toByteArray());
    }

    private RoutingGraphTile.Vertex map(final VectorTileElement element) {
        final var vertex = (Vertex) element;

        final var builder = RoutingGraphTile.Vertex.newBuilder().setIdentifier((int) vertex.id());

        // Set coordinate
        final var coordinate = vertex.coordinate();
        builder.setX(coordinate.x).setY(coordinate.y);
        if (coordinate.getZ() != Coordinate.NULL_ORDINATE) {
            builder.setElevation((float) coordinate.getZ());
        }

        // Set (routing) neighbors
        builder.addAllNeighbors(vertex.neighbors().stream().map(this::map).toList());
        builder.addAllRoutingNeighbors(vertex.routingNeighbors().stream().map(this::map).toList());

        return builder.build();
    }

    private RoutingGraphTile.Vertex.Neighbor map(final Neighbor neighbor) {
        final var coordinate = neighbor.coordinate();
        return RoutingGraphTile.Vertex.Neighbor.newBuilder()
                .setIdentifier((int) neighbor.id())
                .setX(coordinate.x)
                .setY(coordinate.y)
                .setWeight(neighbor.weight())
                .build();
    }

    private RoutingGraphTile.Vertex.RoutingNeighbor map(final RoutingNeighbor routingNeighbor) {
        final var coordinate = routingNeighbor.coordinate();
        return RoutingGraphTile.Vertex.RoutingNeighbor.newBuilder()
                .setIdentifier((int) routingNeighbor.id())
                .setX(coordinate.x)
                .setY(coordinate.y)
                .setWeight(routingNeighbor.weight())
                .setDirection((int) routingNeighbor.direction())
                .build();
    }
}
