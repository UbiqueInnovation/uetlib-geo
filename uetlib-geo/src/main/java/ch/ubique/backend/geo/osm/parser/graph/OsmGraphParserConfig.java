package ch.ubique.backend.geo.osm.parser.graph;

public class OsmGraphParserConfig {

    private final boolean parseNodeTags;

    private final boolean parseWayTags;

    private final boolean useSourceIds;

    private OsmGraphParserConfig(boolean parseNodeTags, boolean parseWayTags, boolean useSourceIds) {
        this.parseNodeTags = parseNodeTags;
        this.parseWayTags = parseWayTags;
        this.useSourceIds = useSourceIds;
    }

    public boolean parseNodeTags() {
        return parseNodeTags;
    }

    public boolean parseWayTags() {
        return parseWayTags;
    }

    public boolean useSourceIds() {
        return useSourceIds;
    }

    public static OsmParserConfigBuilder builder() {
        return new OsmParserConfigBuilder();
    }

    public static class OsmParserConfigBuilder {
        private boolean parseNodeTags = true;
        private boolean parseWayTags = true;
        private boolean useSourceIds = true;

        private OsmParserConfigBuilder() {}

        public OsmParserConfigBuilder parseNodeTags(boolean parseNodeTags) {
            this.parseNodeTags = parseNodeTags;
            return this;
        }

        public OsmParserConfigBuilder parseWayTags(boolean parseWayTags) {
            this.parseWayTags = parseWayTags;
            return this;
        }

        public OsmParserConfigBuilder useSourceIds(boolean useSourceIds) {
            this.useSourceIds = useSourceIds;
            return this;
        }

        public OsmGraphParserConfig build() {
            return new OsmGraphParserConfig(parseNodeTags, parseWayTags, useSourceIds);
        }
    }
}
