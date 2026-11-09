package ch.uetlib.geo.osm.parser.osmosis;

import ch.uetlib.core.utils.IndeterminateProgressLogger;

import com.google.common.util.concurrent.Runnables;

import org.apache.commons.lang3.function.Consumers;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.pbf2.v0_6.PbfReader;
import org.slf4j.MDC;

import java.io.InputStream;
import java.util.Map;
import java.util.function.Consumer;

public class OsmParser {

    private final InputStream inputStream;

    private Consumer<Way> onWay = Consumers.nop();
    private Consumer<Node> onNode = Consumers.nop();
    private Consumer<Relation> onRelation = Consumers.nop();
    private Consumer<Bound> onBound = Consumers.nop();
    private Consumer<Map<String, Object>> onMetadata = Consumers.nop();

    private Runnable onComplete = Runnables.doNothing();
    private Runnable onClose = Runnables.doNothing();

    private final IndeterminateProgressLogger nodeProgressLogger;
    private final IndeterminateProgressLogger wayProgressLogger;
    private final IndeterminateProgressLogger relationProgressLogger;

    private OsmParser(final InputStream inputStream) {
        this.inputStream = inputStream;

        final var simpleName = getClass().getSimpleName();
        final var contextMap = MDC.getCopyOfContextMap();

        nodeProgressLogger =
                new IndeterminateProgressLogger(simpleName + " - Nodes: ", 1_000_000, contextMap);
        wayProgressLogger =
                new IndeterminateProgressLogger(simpleName + " - Ways: ", 100_000, contextMap);
        relationProgressLogger =
                new IndeterminateProgressLogger(simpleName + " - Relations: ", 10_000, contextMap);
    }

    public static OsmParser from(final InputStream inputStream) {
        return new OsmParser(inputStream);
    }

    public OsmParser onWay(final Consumer<Way> onWay) {
        this.onWay = onWay;
        return this;
    }

    public OsmParser onNode(final Consumer<Node> onNode) {
        this.onNode = onNode;
        return this;
    }

    public OsmParser onRelation(final Consumer<Relation> onRelation) {
        this.onRelation = onRelation;
        return this;
    }

    public OsmParser onBound(final Consumer<Bound> onBound) {
        this.onBound = onBound;
        return this;
    }

    public OsmParser onMetadata(final Consumer<Map<String, Object>> onMetadata) {
        this.onMetadata = onMetadata;
        return this;
    }

    public OsmParser onComplete(final Runnable onComplete) {
        this.onComplete = onComplete;
        return this;
    }

    public OsmParser onClose(final Runnable onClose) {
        this.onClose = onClose;
        return this;
    }

    public void parse() {
        parse(1);
    }

    public void parse(final int workers) {
        final var reader = new PbfReader(() -> inputStream, workers);
        reader.setSink(
                new Sink() {
                    @Override
                    public void process(final EntityContainer entityContainer) {
                        final var entity = entityContainer.getEntity();

                        if (entity instanceof final Way way) {
                            onWay.accept(way);
                            wayProgressLogger.step();
                        } else if (entity instanceof final Node node) {
                            onNode.accept(node);
                            nodeProgressLogger.step();
                        } else if (entity instanceof final Relation relation) {
                            onRelation.accept(relation);
                            relationProgressLogger.step();
                        } else if (entity instanceof final Bound bound) {
                            onBound.accept(bound);
                        }
                    }

                    @Override
                    public void initialize(final Map<String, Object> metaData) {
                        onMetadata.accept(metaData);
                    }

                    @Override
                    public void complete() {
                        onComplete.run();
                    }

                    @Override
                    public void close() {
                        onClose.run();
                    }
                });
        reader.run();
    }
}
