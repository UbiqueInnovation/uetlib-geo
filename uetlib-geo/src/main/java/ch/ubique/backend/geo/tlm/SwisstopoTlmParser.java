package ch.ubique.backend.geo.tlm;

import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.coordinate.util.JtsConverter;
import ch.ubique.backend.geo.tlm.models.*;
import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.LineString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


public class SwisstopoTlmParser {

    private SwisstopoTlmParser() {}

    private static final String PATH_XML_TAG_NAME = "swissTLM3D_ili2_V2_0.TLM_STRASSEN.TLM_STRASSE";

    private static final Logger logger = LoggerFactory.getLogger(SwisstopoTlmParser.class);


    public static Collection<TlmPath> parseData(final InputStream inputStream) throws IOException {
        final var paths = new LinkedList<TlmPath>();
        final var inputFactory = XMLInputFactory.newInstance();

        try {
            final var reader = inputFactory.createXMLStreamReader(inputStream, "UTF-8");

            logger.info("Parsing TLM");
            while (reader.hasNext()) {
                reader.next();
                final var type = reader.getEventType();
                if (type == XMLStreamConstants.START_ELEMENT) {
                    String elementName = reader.getLocalName();
                    if (elementName.equals(PATH_XML_TAG_NAME)) {
                        final var path = parsePath(reader);
                        paths.add(path);
                    }
                }
            }
            logger.info("Parsing TLM finished");
            return paths;
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    private static TlmPath parsePath(XMLStreamReader reader) throws XMLStreamException {
        var path = new TlmPath();
        while (reader.hasNext()) {
            reader.next();
            int eventType = reader.getEventType();
            String elementName;
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                elementName = reader.getLocalName();
                if (elementName.equals("POLYLINE")) {
                    path.setLine(parsePolyline(reader));
                }
                readPathAttributes(reader, path);
            } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                elementName = reader.getLocalName();
                if (elementName.equals(PATH_XML_TAG_NAME)) {
                    return path;
                }
            }
        }
        return null;
    }

    private static void readPathAttributes(XMLStreamReader reader, TlmPath path) throws XMLStreamException {
        String value;
        switch (reader.getLocalName()) {
            case "Objektart" -> {
                value = reader.getElementText();
                if (isPresent(value)) {
                    path.setType(WegType.valueOf(value.toUpperCase()));
                }
            }
            case "UUID" -> {
                value = reader.getElementText();
                if (isPresent(value)) {
                    path.setUuid(value);
                }
            }
            case "Stufe" -> {
                value = reader.getElementText();
                if (isPresent(value)) {
                    path.setStufe(parseStufe(value));
                }
            }
            case "Belagsart" -> {
                value = reader.getElementText();
                if (isPresent(value)) {
                    path.setBelagsart(BelagType.valueOf(value.toUpperCase()));
                }
            }
            case "Verkehrsbedeutung" -> {
                value = reader.getElementText();
                if (isPresent(value)) {
                    path.setVerkehrsbedeutung(Verkehrsbedeutung.valueOf(value.toUpperCase()));
                }
            }
            case "Wanderwege" -> {
                value = reader.getElementText();
                if (isPresent(value)) {
                    path.setWanderwegType(WanderwegType.valueOf(value.toUpperCase()));
                }
            }
        }
    }

    private static LineString parsePolyline(XMLStreamReader reader) throws XMLStreamException {
        List<LV95Coordinate> polyline = new ArrayList<>();
        while (reader.hasNext()) {
            reader.next();
            int type = reader.getEventType();
            if (type == XMLStreamConstants.START_ELEMENT) {
                String elementName = reader.getLocalName();
                if (elementName.equals("COORD")) {
                    polyline.add(parseCoordinate(reader));
                } else {
                    logger.warn("Unexpected element in Polyline: {}", elementName);
                }
            }
            if (type == XMLStreamConstants.END_ELEMENT) {
                String elementName = reader.getLocalName();
                if (elementName.equals("POLYLINE")) {
                    return JtsConverter.LV95.toJtsLineString(polyline);
                }
            }
        }
        return JtsConverter.LV95.toJtsLineString(polyline);
    }

    private static LV95Coordinate parseCoordinate(XMLStreamReader reader) throws XMLStreamException {
        double east = -1;
        double north = -1;
        Double elevation = null;
        while (reader.hasNext()) {
            reader.next();
            int type = reader.getEventType();
            if (type == XMLStreamConstants.START_ELEMENT) {
                String elementName = reader.getLocalName();
                switch (elementName) {
                    case "C1" -> east = Double.parseDouble(reader.getElementText());
                    case "C2" -> north = Double.parseDouble(reader.getElementText());
                    case "C3" -> elevation = Double.parseDouble(reader.getElementText());
                    default -> logger.warn("Unexpected element in Coord {}", elementName);
                }
            }
            if (type == XMLStreamConstants.END_ELEMENT) {
                String elementName = reader.getLocalName();
                if (elementName.equals("COORD")) {
                    return new LV95Coordinate(east, north, elevation);
                }
            }
        }
        return null;
    }

    private static Integer parseStufe(String stufeString) {
        String[] split = stufeString.split("_");
        var value = Integer.parseInt(split[split.length - 1]);
        if (stufeString.contains("minus")) {
            value = -value;
        }
        return value;
    }

    private static boolean isPresent(String value) {
        return !isNull(value);
    }

    private static boolean isNull(String value) {
        var nullValues = List.of("k_W", "ub");
        return StringUtils.isBlank(value) || nullValues.contains(value);
    }
}
