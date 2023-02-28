package ch.ubique.backend.geojob;

import static ch.ubique.backend.geo.coordinate.util.JtsConverter.LV95;

import ch.ubique.backend.core.service.DataAndInput;
import ch.ubique.backend.core.service.InputDataLoaderService;
import ch.ubique.backend.geo.graph.routing.GraphType;
import ch.ubique.backend.geo.graph.routing.RoutingGraphsWithLayer;
import ch.ubique.backend.geo.graph.routing.utils.RoutingGraphsToTileElementsUtil;
import ch.ubique.backend.geo.tile.common.models.TileOutputFormat;
import ch.ubique.backend.geo.tile.common.utils.TileGeneratorUtil;
import ch.ubique.backend.geo.tile.vector.models.BinaryTile;
import ch.ubique.backend.geo.tile.vector.models.VectorTileConfig;
import ch.ubique.backend.geo.tile.vector.models.VectorTileElement;
import ch.ubique.backend.geo.tile.vector.models.VectorTileOutput;
import ch.ubique.backend.geo.tile.vector.utils.VectorTileElementSupplier;
import ch.ubique.backend.job.BaseInputJob;
import ch.ubique.backend.job.model.JobPropertyKey;
import ch.ubique.backend.job.model.JobRunState;
import ch.ubique.backend.job.model.JobRunnerKey;
import com.pivovarit.function.ThrowingConsumer;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.context.ApplicationContext;

public abstract class BaseRoutingGraphGeneratorJob<T> extends BaseInputJob<T> {

    private final InputDataLoaderService<T> service;

    protected BaseRoutingGraphGeneratorJob(
            final ApplicationContext context, final InputDataLoaderService<T> service) {
        super(context);
        this.service = service;
    }

    public record RoutingGraphOutput(
            BinaryTile tile, String layer, GraphType graphType, TileOutputFormat format) {}

    protected abstract Stream<RoutingGraphsWithLayer> getGraphs(T data);

    protected abstract void saveGraph(final RoutingGraphOutput output) throws IOException;

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

    @Override
    protected final DataAndInput<T> loadInputs() throws Exception {
        return service.loadData();
    }

    @Override
    protected final JobRunState processInputs(T data) throws Exception {
        logger.info("Getting graphs.");
        final var graphs = getGraphs(data);

        logger.info("Mapping graphs to outputs");
        final var outputs = graphs.flatMap(this::getVectorTileOutputs);

        logger.info("Generating tiles");
        TileGeneratorUtil.generateTiles(outputs);

        return JobRunState.DONE;
    }

    @NotNull
    private Stream<VectorTileOutput> getVectorTileOutputs(
            final RoutingGraphsWithLayer graphsWithLayer) {

        final var graphs = graphsWithLayer.graphs();

        final var completeGraph = graphs.completeGraph();
        final var routingGraph = graphs.routingGraph();

        logger.info(
                "Creating outputs for complete graph {} and routing graph {}.",
                completeGraph,
                routingGraph);

        if (completeGraph.isEmpty()) {
            throw new IllegalArgumentException("Complete graph must not be empty.");
        }
        if (routingGraph.isEmpty()) {
            throw new IllegalArgumentException("Routing graph must not be empty.");
        }

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
        return outputFormats()
                .flatMap(
                        format ->
                                vectorTileOutputs(
                                        graphsWithLayer.layer(),
                                        routingGraphElementSupplier,
                                        completeGraphVertexSupplier,
                                        format));
    }

    @NotNull
    protected Stream<TileOutputFormat> outputFormats() {
        return Stream.of(TileOutputFormat.PBF);
    }

    @NotNull
    private Stream<VectorTileOutput> vectorTileOutputs(
            final String layer,
            final VectorTileElementSupplier<VectorTileElement> routingGraphVertexSupplier,
            final VectorTileElementSupplier<VectorTileElement> completeGraphVertexSupplier,
            final TileOutputFormat format) {

        final var routingGraphOutput =
                new VectorTileOutput(
                        VectorTileConfig.lv95().setZoomLevels(19, 20).setOutputFormat(format),
                        routingGraphVertexSupplier,
                        saveGraph(layer, GraphType.ROUTING, format));

        final VectorTileOutput completeGraphOutput =
                new VectorTileOutput(
                        VectorTileConfig.lv95().setZoomLevels(20, 21).setOutputFormat(format),
                        completeGraphVertexSupplier,
                        saveGraph(layer, GraphType.COMPLETE, format));

        return Stream.of(routingGraphOutput, completeGraphOutput);
    }

    private ThrowingConsumer<BinaryTile, IOException> saveGraph(
            final String layer, final GraphType graphType, final TileOutputFormat format) {
        return tile -> saveGraph(new RoutingGraphOutput(tile, layer, graphType, format));
    }
}
