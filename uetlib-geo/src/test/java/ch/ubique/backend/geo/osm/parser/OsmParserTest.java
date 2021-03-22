package ch.ubique.backend.geo.osm.parser;


import ch.ubique.backend.geo.graph.models.Graph;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class OsmParserTest {
    
    @Test
    @Disabled("Depends on local file")
    void testParsingOSM() throws FileNotFoundException {
        File file = new File("/home/daniel/Downloads/all.pbf");
        Graph graph = OsmParser.parse(new FileInputStream(file), true, false);
        Assertions.assertTrue(graph.getVertices().size() > 0);
        Assertions.assertTrue(graph.getEdges().size() > 0);
    }

}
