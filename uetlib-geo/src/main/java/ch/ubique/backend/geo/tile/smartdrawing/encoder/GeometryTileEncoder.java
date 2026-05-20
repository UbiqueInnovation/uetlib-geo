package ch.ubique.backend.geo.tile.smartdrawing.encoder;

import static ch.ubique.backend.geo.tile.vector.mapbox.encoding.CommandsEncoder.COMMANDS_ENCODER_2D;
import static ch.ubique.backend.geo.tile.vector.mapbox.encoding.CommandsEncoder.COMMANDS_ENCODER_3D;

import ch.ubique.backend.geo.tile.smartdrawing.models.SmartDrawingEdge;
import ch.ubique.backend.geo.tile.smartdrawing.protobuf.GeometryTile;
import ch.ubique.backend.geo.tile.smartdrawing.protobuf.GeometryTile.Value;
import ch.ubique.backend.geo.tile.smartdrawing.protobuf.GeometryTile.Way;
import ch.ubique.backend.geo.tile.vector.mapbox.encoding.CommandsEncoder;
import ch.ubique.backend.geo.tile.vector.mapbox.encoding.DeltaEncoder;
import ch.ubique.backend.geo.tile.vector.mapbox.encoding.TagsEncoder;

import org.apache.commons.lang3.exception.UncheckedException;
import org.geotools.api.referencing.operation.TransformException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.locationtech.jts.geom.LineString;

import java.util.*;

public class GeometryTileEncoder {

    private int extent = 4096;
    private boolean encodeElevation = true;

    private final ReferencedEnvelope tileEnvelope;
    private final List<SmartDrawingEdge> edges;

    public GeometryTileEncoder(final ReferencedEnvelope tileEnvelope) {
        this.tileEnvelope = tileEnvelope;
        this.edges = new ArrayList<>();
    }

    public GeometryTileEncoder extent(final int extent) {
        this.extent = extent;
        return this;
    }

    public GeometryTileEncoder encodeElevation(final boolean encodeElevation) {
        this.encodeElevation = encodeElevation;
        return this;
    }

    public GeometryTileEncoder add(final SmartDrawingEdge edge) {
        edges.add(edge);
        return this;
    }

    public GeometryTileEncoder addAll(final Collection<SmartDrawingEdge> edges) {
        this.edges.addAll(edges);
        return this;
    }

    public GeometryTile encode() {
        try {
            return new Encoder().encode();
        } catch (final TransformException e) {
            throw new UncheckedException(e);
        }
    }

    private class Encoder {
        private final TagsEncoder tagsEncoder;
        private final TileCoordinateEncoder tileCoordinateEncoder;
        private final CommandsEncoder commandsEncoder;

        private Encoder() throws TransformException {
            tagsEncoder = new TagsEncoder();
            commandsEncoder = encodeElevation ? COMMANDS_ENCODER_3D : COMMANDS_ENCODER_2D;
            tileCoordinateEncoder = new TileCoordinateEncoder(extent, tileEnvelope);
        }

        private GeometryTile encode() {
            final var builder =
                    GeometryTile.newBuilder().setExtent(extent).setHasElevation(encodeElevation);

            for (final var edge : edges) {
                builder.addWays(encodeWay(edge.canonicalDirection(), tagsEncoder));
            }
            builder.addAllKeys(tagsEncoder.keys());
            builder.addAllValues(tagsEncoder.values().stream().map(this::encodeValue).toList());
            return builder.build();
        }

        private Way encodeWay(final SmartDrawingEdge edge, final TagsEncoder tagsEncoder) {
            final var wayBuilder = Way.newBuilder().setU(edge.u().id()).setV(edge.v().id());

            final var multiEdgeVariant = edge.multiEdgeVariant();
            if (multiEdgeVariant != null) {
                wayBuilder.setMultiEdgeVariant(multiEdgeVariant);
            }

            for (final var entry : edge.tags().entrySet()) {
                wayBuilder.addTags(tagsEncoder.indexOfKey(entry.getKey()));
                wayBuilder.addTags(tagsEncoder.indexOfValue(entry.getValue()));
            }

            wayBuilder.addAllEncodedCoordinates(encodeGeometry(edge.geometry()));

            return wayBuilder.build();
        }

        private List<Integer> encodeGeometry(final LineString lineString) {
            final var tileCoordinates = tileCoordinateEncoder.computeTileCoordinates(lineString);
            final var deltas = new DeltaEncoder(encodeElevation).visitCoordinates(tileCoordinates);
            return commandsEncoder.encodeDeltas(deltas);
        }

        private Value encodeValue(final Object value) {
            final var builder = Value.newBuilder();
            switch (value) {
                case final String s -> builder.setStringValue(s);
                case final Float f -> builder.setFloatValue(f);
                case final Double d -> builder.setDoubleValue(d);
                case final Integer i -> builder.setIntValue(i.longValue());
                case final Long l -> builder.setIntValue(l);
                case final Boolean b -> builder.setBoolValue(b);
                default -> throw new IllegalArgumentException("Unsupported value type: " + value);
            }
            return builder.build();
        }
    }
}
