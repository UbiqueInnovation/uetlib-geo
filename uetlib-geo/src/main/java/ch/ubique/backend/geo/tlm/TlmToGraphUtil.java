package ch.ubique.backend.geo.tlm;

import ch.ubique.backend.geo.graph.routing.RoutingGraphs;
import ch.ubique.backend.geo.graph.routing.models.LineWithInfo;
import ch.ubique.backend.geo.graph.routing.utils.LinesToGraphConfig;
import ch.ubique.backend.geo.graph.routing.utils.LinesToRoutingGraphUtil;
import ch.ubique.backend.geo.graph.routing.utils.WeightingFunction;
import ch.ubique.backend.geo.tile.vector.models.tlm.TlmPathInfo;
import ch.ubique.backend.geo.tlm.models.TlmPath;
import ch.ubique.backend.geo.tlm.models.WegType;

import java.util.Collection;
import java.util.Set;

public final class TlmToGraphUtil {

    private static final Set<WegType> forbiddenWays =
            Set.of(WegType.AUTOBAHN, WegType.AUTOZUG, WegType.FAEHRE);

    private TlmToGraphUtil() {}

    public static RoutingGraphs buildGraph(
            final Collection<TlmPath> paths,
            final WeightingFunction<TlmPathInfo> weightingFunction) {

        final var relevantPaths =
                paths.stream()
                        .filter(path -> !forbiddenWays.contains(path.info().type()))
                        .map(path -> new LineWithInfo<>(path.line(), path.info()))
                        .toList();

        return LinesToRoutingGraphUtil.buildGraphWithInfo(
                relevantPaths,
                LinesToGraphConfig.<TlmPathInfo>builder()
                        .weightingFunction(weightingFunction)
                        .build());
    }
}
