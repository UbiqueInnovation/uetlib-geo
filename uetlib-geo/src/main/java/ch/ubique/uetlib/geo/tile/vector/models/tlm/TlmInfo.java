package ch.ubique.uetlib.geo.tile.vector.models.tlm;

public sealed interface TlmInfo permits TlmPathInfo, RailwayInfo, UnstructuredTlmInfo {

    UnstructuredTlmInfo unstructured();

    default TlmPathInfo asPathInfo() {
        return new TlmPathInfo(unstructured());
    }

    default RailwayInfo asRailwayInfo() {
        return new RailwayInfo(unstructured());
    }
}
