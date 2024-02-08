package ch.ubique.backend.geojob;

import static ch.ubique.backend.geo.coordinate.constants.CoordinateSystems.LV95;

import ch.ubique.backend.core.service.DataAndInput;
import ch.ubique.backend.core.service.InputDataLoaderService;
import ch.ubique.backend.geo.graph.routing.GraphType;
import ch.ubique.backend.geo.graph.routing.RoutingGraphsWithLayer;
import ch.ubique.backend.geo.graph.routing.models.RoutingGraphTile.Vertex;
import ch.ubique.backend.geo.graph.routing.utils.RoutingGraphsToTileElementsUtil;
import ch.ubique.backend.geo.tile.common.models.TileOutputFormat;
import ch.ubique.backend.geo.tile.common.utils.TileGeneratorUtil;
import ch.ubique.backend.geo.tile.vector.models.BinaryTile;
import ch.ubique.backend.geo.tile.vector.models.VectorTileConfig;
import ch.ubique.backend.geo.tile.vector.models.VectorTileOutput;
import ch.ubique.backend.geo.tile.vector.utils.VectorTileElementSupplier;
import ch.ubique.backend.job.BaseInputJob;
import ch.ubique.backend.job.model.JobRunState;
import ch.ubique.backend.job.model.properties.JobPropertiesConfig;
import ch.ubique.backend.job.model.properties.JobPropertyDefinitions;
import ch.ubique.backend.job.model.properties.enums.JobRunnerKey;
import ch.ubique.backend.job.model.properties.enums.KubernetesSku;

import com.pivovarit.function.ThrowingConsumer;

import jakarta.validation.constraints.NotNull;

import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.stream.Stream;

public abstract class BaseRoutingGraphGeneratorJob<T> extends BaseInputJob<T> {

    private final InputDataLoaderService<T> service;

    protected BaseRoutingGraphGeneratorJob(
            final ApplicationContext applicationContext, final InputDataLoaderService<T> service) {
        super(applicationContext);
        this.service = service;
    }

    protected abstract Stream<RoutingGraphsWithLayer> getGraphs(T data);

    protected abstract void saveGraph(final RoutingGraphOutput output) throws IOException;

    @Override
    protected boolean isInputFileUploadEnabled() {
        return false;
    }

    @Override
    @NotNull
    public JobPropertiesConfig propertiesConfig() {
        return JobPropertiesConfig.initial(
                JobPropertyDefinitions.RUNNER.createProperty(JobRunnerKey.DEDICATED_K8S_JOB),
                JobPropertyDefinitions.K8S_SKU.createProperty(KubernetesSku.HIGH_MEMORY.value()),
                JobPropertyDefinitions.K8S_REQUESTS_CPU.createProperty("2"),
                JobPropertyDefinitions.K8S_REQUESTS_MEMORY.createProperty("40Gi"));
    }

    @Override
    protected final DataAndInput<T> loadInputs() throws Exception {
        return service.loadData();
    }

    @Override
    protected final JobRunState processInputs(final T data) throws Exception {
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
                "Creating outputs for complete graph {} and routing graph {}",
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
        final var hasHighwayVertices = completeVertices.stream().anyMatch(Vertex::isHighway);

        logger.info("Creating vertex supplier.");
        final var vertexSupplier = new VectorTileElementSupplier<>(completeVertices);

        logger.info("Returning graph outputs");
        return outputFormats()
                .flatMap(
                        format ->
                                vectorTileOutputs(
                                        graphsWithLayer.layer(),
                                        vertexSupplier,
                                        hasHighwayVertices,
                                        format));
    }

    private boolean isRoutingVertex(final Vertex vertex) {
        return !vertex.routingNeighbors().isEmpty();
    }

    @NotNull
    protected Stream<TileOutputFormat> outputFormats() {
        return Stream.of(TileOutputFormat.PBF);
    }

    @NotNull
    private Stream<VectorTileOutput> vectorTileOutputs(
            final String layer,
            final VectorTileElementSupplier<Vertex> vertexSupplier,
            final boolean hasHighwayVertices,
            final TileOutputFormat format) {

        return Stream.concat(
                regularGraphOutputs(layer, vertexSupplier, format),
                highwayGraphOutputs(layer, vertexSupplier, format, hasHighwayVertices));
    }

    private Stream<VectorTileOutput> regularGraphOutputs(
            final String layer,
            final VectorTileElementSupplier<Vertex> vertexSupplier,
            final TileOutputFormat format) {
        logger.info("Creating outputs for regular graphs");
        return Stream.of(
                new VectorTileOutput(
                        baseConfig().zoomLevels(21).outputFormat(format).build(),
                        vertexSupplier,
                        saveGraph(layer, GraphType.COMPLETE, format)),
                new VectorTileOutput(
                        baseConfig().zoomLevels(20).outputFormat(format).build(),
                        vertexSupplier.withFilter(this::isRoutingVertex),
                        saveGraph(layer, GraphType.ROUTING, format)));
    }

    private Stream<VectorTileOutput> highwayGraphOutputs(
            final String layer,
            final VectorTileElementSupplier<Vertex> vertexSupplier,
            final TileOutputFormat format,
            final boolean hasHighwayVertices) {

        if (!hasHighwayVertices) {
            return Stream.of();
        }

        logger.info("Creating outputs for highway graphs");
        return Stream.of(
                new VectorTileOutput(
                        baseConfig().zoomLevels(18).outputFormat(format).build(),
                        vertexSupplier.withFilter(Vertex::isHighway),
                        saveGraph(layer, GraphType.HIGHWAY_COMPLETE, format)),
                new VectorTileOutput(
                        baseConfig().zoomLevels(18).outputFormat(format).build(),
                        vertexSupplier.withFilter(v -> v.isHighway() && isRoutingVertex(v)),
                        saveGraph(layer, GraphType.HIGHWAY_ROUTING, format)));
    }

    protected VectorTileConfig.VectorTileConfigBuilder baseConfig() {
        return VectorTileConfig.lv95();
    }

    private ThrowingConsumer<BinaryTile, IOException> saveGraph(
            final String layer, final GraphType graphType, final TileOutputFormat format) {
        return tile -> saveGraph(new RoutingGraphOutput(tile, layer, graphType, format));
    }

    public record RoutingGraphOutput(
            BinaryTile tile, String layer, GraphType graphType, TileOutputFormat format) {}
}
