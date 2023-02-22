package ch.ubique.backend.geojob;

import static ch.ubique.backend.geo.coordinate.util.JtsConverter.LV95;

import ch.ubique.backend.geo.graph.routing.GraphType;
import ch.ubique.backend.geo.graph.routing.RoutingGraphs;
import ch.ubique.backend.geo.graph.routing.utils.RoutingGraphsToTileElementsUtil;
import ch.ubique.backend.geo.tile.common.models.TileOutputFormat;
import ch.ubique.backend.geo.tile.vector.models.BinaryTile;
import ch.ubique.backend.geo.tile.vector.models.VectorTileConfig;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElement;
import ch.ubique.backend.geo.tile.vector.models.VectorTileOutput;
import ch.ubique.backend.geo.tile.vector.utils.VectorTileElementSupplier;
import ch.ubique.backend.job.model.JobPropertyKey;
import ch.ubique.backend.job.model.JobRunnerKey;
import com.pivovarit.function.ThrowingConsumer;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.context.ApplicationContext;

public abstract class BaseRoutingGraphGeneratorJob<T> extends BaseVectorTileImporter<T> {

    protected BaseRoutingGraphGeneratorJob(final ApplicationContext context) {
        super(context);
    }

    public record TileOutput(BinaryTile tile, GraphType graphType, TileOutputFormat format) {}

    @Override
    protected boolean isInputFileUploadEnabled() {
        return false;
    }

    @Override
    public Map<JobPropertyKey, String> initialProperties() {
        return Map.ofEntries(
                Map.entry(JobPropertyKey.RUNNER, JobRunnerKey.DEDICATED_K8S_JOB.name()),
                Map.entry(JobPropertyKey.K8S_SKU, "highmem"),
                Map.entry(JobPropertyKey.K8S_REQUESTS_CPU, "2"),
                Map.entry(JobPropertyKey.K8S_REQUESTS_MEMORY, "40Gi"));
    }

    @NotNull
    protected Stream<VectorTileOutput> getOutputsForGraphs(
            final RoutingGraphs graphs,
            final ThrowingConsumer<TileOutput, IOException> tileConsumer) {

        logger.info(
                "Creating outputs for complete graph {} and routing graph {}.",
                graphs.completeGraph(),
                graphs.routingGraph());

        logger.info("Creating vector tile elements for each vertex.");
        final var completeVertices =
                RoutingGraphsToTileElementsUtil.mapToTileElements(graphs, LV95.geometryFactory());

        logger.info("Filtering routing vertices.");
        final var routingVertices =
                completeVertices.stream().filter(vertex -> !vertex.routingNeighbors().isEmpty());

        logger.info("Creating vertex supplier for routing graph.");
        final var routingGraphElementSupplier =
                new VectorTileElementSupplier<VectorTileElement>(routingVertices);

        logger.info("Creating vertex supplier for complete graph.");
        final var completeGraphVertexSupplier =
                new VectorTileElementSupplier<VectorTileElement>(completeVertices);

        logger.info("Returning graph outputs");
        return Stream.of(TileOutputFormat.JSON, TileOutputFormat.PBF)
                .flatMap(
                        format ->
                                vectorTileOutputs(
                                        tileConsumer,
                                        routingGraphElementSupplier,
                                        completeGraphVertexSupplier,
                                        format));
    }

    @NotNull
    private static Stream<VectorTileOutput> vectorTileOutputs(
            final ThrowingConsumer<TileOutput, IOException> tileConsumer,
            final VectorTileElementSupplier<VectorTileElement> routingGraphVertexSupplier,
            final VectorTileElementSupplier<VectorTileElement> completeGraphVertexSupplier,
            final TileOutputFormat format) {

        final var routingGraphOutput =
                new VectorTileOutput(
                        VectorTileConfig.lv95().setZoomLevels(20, 21).setOutputFormat(format),
                        routingGraphVertexSupplier,
                        t -> tileConsumer.accept(new TileOutput(t, GraphType.ROUTING, format)));

        final VectorTileOutput completeGraphOutput =
                new VectorTileOutput(
                        VectorTileConfig.lv95().setZoomLevels(21).setOutputFormat(format),
                        completeGraphVertexSupplier,
                        t -> tileConsumer.accept(new TileOutput(t, GraphType.COMPLETE, format)));

        return Stream.of(routingGraphOutput, completeGraphOutput);
    }
}
