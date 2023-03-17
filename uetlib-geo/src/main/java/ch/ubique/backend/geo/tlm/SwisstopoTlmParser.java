package ch.ubique.backend.geo.tlm;

import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.coordinate.util.JtsConverter;
import ch.ubique.backend.geo.tlm.models.BelagType;
import ch.ubique.backend.geo.tlm.models.TlmPath;
import ch.ubique.backend.geo.tlm.models.Verkehrsbedeutung;
import ch.ubique.backend.geo.tlm.models.WanderwegType;
import ch.ubique.backend.geo.tlm.models.WegType;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.LineString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class SwisstopoTlmParser {

    private static final String PATH_XML_TAG_NAME = "swissTLM3D_ili2_V2_1.TLM_STRASSEN.TLM_STRASSE";
    private static final Logger logger = LoggerFactory.getLogger(SwisstopoTlmParser.class);

    private SwisstopoTlmParser() {}

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
                    final var elementName = reader.getLocalName();
                    if (elementName.equals(PATH_XML_TAG_NAME)) {
                        final var path = parsePath(reader);
                        paths.add(path);
                    }
                }
            }
            logger.info("Parsing TLM finished");
            return paths;
        } catch (final XMLStreamException e) {
            throw new IOException(e);
        }
    }

    private static TlmPath parsePath(final XMLStreamReader reader) throws XMLStreamException {
        final var path = new TlmPath();
        while (reader.hasNext()) {
            reader.next();
            final var eventType = reader.getEventType();
            final String elementName;
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

    private static void readPathAttributes(final XMLStreamReader reader, final TlmPath path)
            throws XMLStreamException {
        final String value;
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

    private static LineString parsePolyline(final XMLStreamReader reader)
            throws XMLStreamException {
        final List<LV95Coordinate> polyline = new ArrayList<>();
        while (reader.hasNext()) {
            reader.next();
            final var type = reader.getEventType();
            if (type == XMLStreamConstants.START_ELEMENT) {
                final var elementName = reader.getLocalName();
                if (elementName.equals("COORD")) {
                    polyline.add(parseCoordinate(reader));
                } else {
                    logger.warn("Unexpected element in Polyline: {}", elementName);
                }
            }
            if (type == XMLStreamConstants.END_ELEMENT) {
                final var elementName = reader.getLocalName();
                if (elementName.equals("POLYLINE")) {
                    return JtsConverter.LV95.toJtsLineString(polyline);
                }
            }
        }
        return JtsConverter.LV95.toJtsLineString(polyline);
    }

    private static LV95Coordinate parseCoordinate(final XMLStreamReader reader)
            throws XMLStreamException {
        double east = -1;
        double north = -1;
        Double elevation = null;
        while (reader.hasNext()) {
            reader.next();
            final var type = reader.getEventType();
            if (type == XMLStreamConstants.START_ELEMENT) {
                final var elementName = reader.getLocalName();
                switch (elementName) {
                    case "C1" -> east = Double.parseDouble(reader.getElementText());
                    case "C2" -> north = Double.parseDouble(reader.getElementText());
                    case "C3" -> elevation = Double.parseDouble(reader.getElementText());
                    default -> logger.warn("Unexpected element in Coord {}", elementName);
                }
            }
            if (type == XMLStreamConstants.END_ELEMENT) {
                final var elementName = reader.getLocalName();
                if (elementName.equals("COORD")) {
                    return new LV95Coordinate(east, north, elevation);
                }
            }
        }
        return null;
    }

    private static Integer parseStufe(final String stufeString) {
        final var split = stufeString.split("_");
        var value = Integer.parseInt(split[split.length - 1]);
        if (stufeString.contains("minus")) {
            value = -value;
        }
        return value;
    }

    private static boolean isPresent(final String value) {
        return !isNull(value);
    }

    private static boolean isNull(final String value) {
        final var nullValues = List.of("k_W", "ub");
        return StringUtils.isBlank(value) || nullValues.contains(value);
    }
}
