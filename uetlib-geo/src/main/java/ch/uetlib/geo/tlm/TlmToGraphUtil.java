package ch.uetlib.geo.tlm;

import ch.uetlib.geo.graph.routing.RoutingGraphs;
import ch.uetlib.geo.graph.routing.models.LineWithInfo;
import ch.uetlib.geo.graph.routing.utils.LinesToGraphConfig;
import ch.uetlib.geo.graph.routing.utils.LinesToRoutingGraphUtil;
import ch.uetlib.geo.graph.routing.utils.RoutingEdgeFunction;
import ch.uetlib.geo.tile.vector.models.tlm.TlmPathInfo;
import ch.uetlib.geo.tlm.models.TlmPath;
import ch.uetlib.geo.tlm.models.WegType;

import java.util.Collection;
import java.util.Set;

public final class TlmToGraphUtil {

    private static final Set<WegType> forbiddenWays =
            Set.of(WegType.AUTOBAHN, WegType.AUTOZUG, WegType.FAEHRE);

    private TlmToGraphUtil() {}

    public static RoutingGraphs buildGraph(
            final Collection<TlmPath> paths,
            final RoutingEdgeFunction<TlmPathInfo> routingEdgeFunction) {

        final var relevantPaths =
                paths.stream()
                        .filter(path -> !forbiddenWays.contains(path.info().type()))
                        .map(path -> new LineWithInfo<>(path.line(), path.info()))
                        .toList();

        return LinesToRoutingGraphUtil.buildGraphWithInfo(
                relevantPaths,
                LinesToGraphConfig.<TlmPathInfo>builder()
                        .routingEdgeFunction(routingEdgeFunction)
                        .build());
    }
}
