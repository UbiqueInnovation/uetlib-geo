package ch.ubique.backend.geo.osm.parser.osmosis;

import crosby.binary.osmosis.OsmosisReader;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class OsmParser {

    private Consumer<Way> onWay;
    private Consumer<Node> onNode;
    private Consumer<Relation> onRelation;
    private Consumer<Bound> onBound;

    private Consumer<Map<String, Object>> onMetadata;

    private Runnable onComplete;

    private Runnable onClose;

    private final InputStream inputStream;

    private OsmParser(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public static OsmParser from(InputStream inputStream) {
        return new OsmParser(inputStream);
    }

    public OsmParser onWay(Consumer<Way> onWay) {
        this.onWay = onWay;
        return this;
    }

    public OsmParser onNode(Consumer<Node> onNode) {
        this.onNode = onNode;
        return this;
    }

    public OsmParser onRelation(Consumer<Relation> onRelation) {
        this.onRelation = onRelation;
        return this;
    }

    public OsmParser onBound(Consumer<Bound> onBound) {
        this.onBound = onBound;
        return this;
    }

    public OsmParser onMetadata(Consumer<Map<String, Object>> onMetadata) {
        this.onMetadata = onMetadata;
        return this;
    }

    public OsmParser onComplete(Runnable onComplete) {
        this.onComplete = onComplete;
        return this;
    }

    public OsmParser onClose(Runnable onClose) {
        this.onClose = onClose;
        return this;
    }

    public void parse() {
        final var reader = new OsmosisReader(inputStream);
        reader.setSink(new Sink() {
            @Override
            public void process(EntityContainer entityContainer) {
                final var entity = entityContainer.getEntity();

                if (entity instanceof Way way) {
                    invoke(onWay, way);
                }
                else if (entity instanceof Node node) {
                    invoke(onNode, node);
                }
                else if (entity instanceof Relation relation) {
                    invoke(onRelation, relation);
                }
                else if (entity instanceof Bound bound) {
                    invoke(onBound, bound);
                }
            }

            @Override
            public void initialize(Map<String, Object> metaData) {
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

            private <T> void invoke(Consumer<T> consumer, T arg) {
                Optional.ofNullable(consumer).ifPresent(c -> c.accept(arg));
            }

            private void invoke(Runnable runnable) {
                Optional.ofNullable(runnable).ifPresent(Runnable::run);
            }
        });
        reader.run();
    }
}
