package ch.ubique.backend.geo.tlm;

import ch.ubique.backend.geo.graph.routing.RoutingGraphs;
import ch.ubique.backend.geo.graph.routing.models.LineWithInfo;
import ch.ubique.backend.geo.graph.routing.utils.LinesToRoutingGraphUtil;
import ch.ubique.backend.geo.graph.routing.utils.WeightingFunction;
import ch.ubique.backend.geo.tile.vector.models.tlm.TlmInfo;
import ch.ubique.backend.geo.tlm.models.TlmPath;
import ch.ubique.backend.geo.tlm.models.WegType;
import java.util.Collection;
import java.util.Set;

public class TlmToGraphUtil {

    private TlmToGraphUtil() {}

    private static final Set<WegType> forbiddenWays =
            Set.of(WegType.AUTOBAHN, WegType.AUTOZUG, WegType.FAEHRE);

    public static RoutingGraphs buildGraph(
            final Collection<TlmPath> paths, final WeightingFunction<TlmInfo> weightingFunction) {

        final var relevantPaths =
                paths.stream()
                        .filter(path -> !forbiddenWays.contains(path.getType()))
                        .map(path -> new LineWithInfo<>(path.getLine(), new TlmInfo(path)))
                        .toList();

        return LinesToRoutingGraphUtil.withInfo(relevantPaths, weightingFunction).buildGraph();
    }
}
