package ch.ubique.backend.geo.osm.parser.osmosis;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.pbf2.v0_6.PbfReader;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class OsmParser {

    private final InputStream inputStream;

    private Consumer<Way> onWay;
    private Consumer<Node> onNode;
    private Consumer<Relation> onRelation;
    private Consumer<Bound> onBound;
    private Consumer<Map<String, Object>> onMetadata;

    private Runnable onComplete;
    private Runnable onClose;

    private OsmParser(final InputStream inputStream) {
        this.inputStream = inputStream;
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
                            invoke(onWay, way);
                        } else if (entity instanceof final Node node) {
                            invoke(onNode, node);
                        } else if (entity instanceof final Relation relation) {
                            invoke(onRelation, relation);
                        } else if (entity instanceof final Bound bound) {
                            invoke(onBound, bound);
                        }
                    }

                    @Override
                    public void initialize(final Map<String, Object> metaData) {
                        invoke(onMetadata, metaData);
                    }

                    @Override
                    public void complete() {
                        invoke(onComplete);
                    }

                    @Override
                    public void close() {
                        invoke(onClose);
                    }

                    private <T> void invoke(final Consumer<T> consumer, final T arg) {
                        Optional.ofNullable(consumer).ifPresent(c -> c.accept(arg));
                    }

                    private void invoke(final Runnable runnable) {
                        Optional.ofNullable(runnable).ifPresent(Runnable::run);
                    }
                });
        reader.run();
    }
}
