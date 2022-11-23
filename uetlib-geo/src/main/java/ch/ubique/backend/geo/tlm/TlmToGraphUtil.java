package ch.ubique.backend.geo.tlm;

import ch.ubique.backend.geo.graph.models.Graph;
import ch.ubique.backend.geo.graph.routing.utils.LineWithInfo;
import ch.ubique.backend.geo.graph.routing.utils.LinesToRoutingGraphUtil;
import ch.ubique.backend.geo.tile.vector.models.tlm.TlmInfo;
import ch.ubique.backend.geo.tlm.models.TlmPath;
import ch.ubique.backend.geo.tlm.models.WegType;
import org.locationtech.jts.geom.Coordinate;

import java.util.Collection;
import java.util.Set;


public class TlmToGraphUtil {

    private TlmToGraphUtil() {}

    private static final Set<WegType> forbiddenWays = Set.of(WegType.AUTOBAHN, WegType.AUTOZUG, WegType.FAEHRE);

    public static Graph<Coordinate, TlmInfo> buildGraph(Collection<TlmPath> paths) {

        final var relevantPaths = paths.stream()
                .filter(path -> !forbiddenWays.contains(path.getType()))
                .map(path -> new LineWithInfo<>(path.getLine(), new TlmInfo(path)))
                .toList();

        return LinesToRoutingGraphUtil.buildGraphWithInfo(relevantPaths);
    }
}
