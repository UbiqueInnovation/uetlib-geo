package ch.uetlib.geo.kml.parser;

import ch.uetlib.geo.coordinate.constants.CoordinateSystems;
import ch.uetlib.geo.coordinate.models.WGS84Coordinate;
import ch.uetlib.geo.coordinate.util.JtsUtil;
import ch.uetlib.geo.kml.models.Icon;
import ch.uetlib.geo.kml.models.ItemStyle;
import ch.uetlib.geo.kml.models.KmlDocument;
import ch.uetlib.geo.kml.models.Placemark;
import ch.uetlib.geo.kml.models.Style;
import ch.uetlib.geo.kml.models.StyleMap;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class KmlParser {

    private static final Logger logger = Logger.getLogger(KmlParser.class.getName());

    private static final GeometryFactory GEOMETRY_FACTORY =
            CoordinateSystems.WGS84.geometryFactory();

    private static final String POINT = "Point";
    private static final String LINE_STRING = "LineString";
    private static final String POLYGON = "Polygon";
    private static final String LINEAR_RING = "LinearRing";
    private static final String MULTI_GEOMETRY = "MultiGeometry";

    private KmlParser() {}

    public static KmlDocument parse(final String path) throws IOException {
        return parse(new FileInputStream(path));
    }

    @SuppressWarnings("java:S2755")
    public static KmlDocument parse(final InputStream inputStream) throws IOException {

        final List<Placemark> placemarks = new ArrayList<>();
        final Map<String, Style> styles = new HashMap<>();
        final Map<String, StyleMap> styleMaps = new HashMap<>();

        final var inputFactory = XMLInputFactory.newInstance();
        try {
            final var reader = inputFactory.createXMLStreamReader(inputStream, "UTF-8");

            logger.info("Parsing KML");
            while (reader.hasNext()) {
                reader.next();
                final var type = reader.getEventType();
                if (type == XMLStreamConstants.START_ELEMENT) {
                    final var elementName = reader.getLocalName();
                    switch (elementName) {
                        case "Placemark" -> {
                            final var placemark = parsePlacemark(reader);
                            placemarks.add(placemark);
                        }
                        case "Style" -> {
                            final var styleId = reader.getAttributeValue(0);
                            final var style = parseStyle(reader);
                            styles.put(styleId, style);
                        }
                        case "StyleMap" -> {
                            final var id = reader.getAttributeValue(0);
                            final var styleMap = parseStyleMap(reader);
                            styleMaps.put(id, styleMap);
                        }
                        default -> {
                            /* NO-OP */
                        }
                    }
                }
            }

            logger.info("Parsing KML finished");
            return new KmlDocument(placemarks, styleMaps, styles);
        } catch (final XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @SuppressWarnings("java:S3776")
    private static Placemark parsePlacemark(final XMLStreamReader reader)
            throws XMLStreamException {
        String name = null;
        String description = null;
        String styleId = null;
        Geometry geometry = null;
        final Map<String, String> extendedData = new HashMap<>();
        while (!isEndElement(reader, "Placemark")) {
            reader.next();
            final var eventType = reader.getEventType();
            if (eventType != XMLStreamConstants.START_ELEMENT) {
                continue;
            }
            switch (reader.getLocalName()) {
                case "name" -> name = reader.getElementText();
                case "description" -> description = reader.getElementText();
                case "styleUrl" -> styleId = reader.getElementText().replace("#", "");
                case POINT, LINE_STRING, LINEAR_RING, POLYGON, MULTI_GEOMETRY ->
                        geometry = parseGeometry(reader);
                case "SimpleData" -> {
                    final var key = reader.getAttributeValue(0);
                    final var value = reader.getElementText();
                    extendedData.put(key, value);
                }
                default -> {
                    /* NO-OP */
                }
            }
        }
        return new Placemark(name, description, styleId, geometry, extendedData);
    }

    private static Geometry parseGeometry(final XMLStreamReader reader) throws XMLStreamException {
        final var geometryType = reader.getLocalName();
        final var geometry =
                switch (geometryType) {
                    case POINT -> GEOMETRY_FACTORY.createPoint(parseCoordinates(reader)[0]);
                    case LINE_STRING -> GEOMETRY_FACTORY.createLineString(parseCoordinates(reader));
                    case LINEAR_RING -> parseLinearRing(reader);
                    case POLYGON -> parsePolygon(reader);
                    case MULTI_GEOMETRY -> parseMultiGeometry(reader);
                    default ->
                            throw new XMLStreamException(
                                    "Unexpected geometry tag: " + geometryType);
                };
        readUntilEnd(reader, geometryType);
        return geometry;
    }

    private static Polygon parsePolygon(final XMLStreamReader reader) throws XMLStreamException {
        final var rings = new ArrayList<LinearRing>();
        while (!isEndElement(reader, POLYGON)) {
            reader.next();
            if (isStartElement(reader, LINEAR_RING)) {
                rings.add(parseLinearRing(reader));
            }
        }
        final var outerRing = rings.getFirst();
        final var innerRings = rings.stream().skip(1).toArray(LinearRing[]::new);
        return GEOMETRY_FACTORY.createPolygon(outerRing, innerRings);
    }

    private static GeometryCollection parseMultiGeometry(final XMLStreamReader reader)
            throws XMLStreamException {
        final var geometries = new ArrayList<Geometry>();
        while (!isEndElement(reader, MULTI_GEOMETRY)) {
            reader.next();
            if (isStartEvent(reader)) {
                geometries.add(parseGeometry(reader));
            }
        }
        return JtsUtil.toGeometryCollection(geometries);
    }

    private static LinearRing parseLinearRing(final XMLStreamReader reader)
            throws XMLStreamException {
        final var coordinates = parseCoordinates(reader);
        if (coordinates[0].equals2D(coordinates[coordinates.length - 1])) {
            return GEOMETRY_FACTORY.createLinearRing(coordinates);
        }
        // A linear ring must start and end with the same coordinate.
        // This is not the case here, so we insert the first coordinate at the end.
        final var closedRing = new Coordinate[coordinates.length + 1];
        System.arraycopy(coordinates, 0, closedRing, 0, coordinates.length);
        closedRing[coordinates.length] = coordinates[0];
        return GEOMETRY_FACTORY.createLinearRing(closedRing);
    }

    private static Coordinate[] parseCoordinates(final XMLStreamReader reader)
            throws XMLStreamException {
        while (reader.hasNext()) {
            reader.next();
            if (isStartElement(reader, "coordinates")) {
                return parseCoordinatesText(reader);
            }
        }
        throw new XMLStreamException("No coordinates found.");
    }

    private static Coordinate[] parseCoordinatesText(final XMLStreamReader reader)
            throws XMLStreamException {
        return Arrays.stream(reader.getElementText().split("\\s+"))
                .filter(StringUtils::isNotBlank)
                .map(KmlParser::toWgs84Coordinate)
                .map(WGS84Coordinate::toJts)
                .toArray(Coordinate[]::new);
    }

    private static boolean isStartElement(final XMLStreamReader reader, final String elementName) {
        return reader.getEventType() == XMLStreamConstants.START_ELEMENT
                && elementName.equals(reader.getLocalName());
    }

    private static void readUntilEnd(final XMLStreamReader reader, final String elementName)
            throws XMLStreamException {
        if (isEndElement(reader, elementName)) {
            return;
        }
        while (reader.hasNext()) {
            reader.next();
            if (isEndElement(reader, elementName)) {
                return;
            }
        }
        throw new XMLStreamException("Unexpected end of file");
    }

    private static boolean isEndElement(final XMLStreamReader reader, final String elementName) {
        return reader.getEventType() == XMLStreamConstants.END_ELEMENT
                && elementName.equals(reader.getLocalName());
    }

    private static Style parseStyle(final XMLStreamReader reader) throws XMLStreamException {
        ItemStyle iconStyle = null;
        ItemStyle labelStyle = null;
        ItemStyle lineStyle = null;
        while (!isEndElement(reader, "Style")) {
            reader.next();
            if (isStartEvent(reader)) {
                final var elementName = reader.getLocalName();
                switch (elementName) {
                    case "IconStyle" -> iconStyle = parseItemStyle(reader, elementName);
                    case "LabelStyle" -> labelStyle = parseItemStyle(reader, elementName);
                    case "LineStyle" -> lineStyle = parseItemStyle(reader, elementName);
                    default -> {
                        /* NO-OP */
                    }
                }
            }
        }
        return new Style(iconStyle, labelStyle, lineStyle);
    }

    private static ItemStyle parseItemStyle(final XMLStreamReader reader, final String endTag)
            throws XMLStreamException {
        String color = null;
        Double scale = null;
        Double width = null;
        Icon icon = null;
        while (!isEndElement(reader, endTag)) {
            reader.next();
            if (isStartEvent(reader)) {
                switch (reader.getLocalName()) {
                    case "color" -> color = reader.getElementText();
                    case "scale" -> scale = Double.parseDouble(reader.getElementText());
                    case "tileWidth" -> width = Double.parseDouble(reader.getElementText());
                    case "Icon" -> icon = parseIcon(reader);
                    default -> {
                        /* NO-OP */
                    }
                }
            }
        }
        return new ItemStyle(color, scale, width, icon);
    }

    private static boolean isStartEvent(final XMLStreamReader reader) {
        return reader.getEventType() == XMLStreamConstants.START_ELEMENT;
    }

    private static StyleMap parseStyleMap(final XMLStreamReader reader) throws XMLStreamException {
        final Map<String, String> styleMap = new HashMap<>();
        while (!isEndElement(reader, "StyleMap")) {
            reader.next();
            if (isStartElement(reader, "Pair")) {
                addPairToStyleMap(reader, styleMap);
            }
        }
        return new StyleMap(styleMap);
    }

    @SuppressWarnings("java:S3776")
    private static void addPairToStyleMap(
            final XMLStreamReader reader, final Map<String, String> styleMap)
            throws XMLStreamException {
        String key = null;
        String value = null;
        while (!isEndElement(reader, "Pair")) {
            reader.next();
            final var eventType = reader.getEventType();
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                final var elementName = reader.getLocalName();
                if ("key".equals(elementName)) {
                    key = reader.getElementText();
                } else if ("styleUrl".equals(elementName)) {
                    value = reader.getElementText().replace("#", "");
                }
            }
        }
        if (key != null && value != null) {
            styleMap.put(key, value);
        }
    }

    private static Icon parseIcon(final XMLStreamReader reader) throws XMLStreamException {
        String href = null;
        while (!isEndElement(reader, "Icon")) {
            reader.next();
            if (isStartElement(reader, "href")) {
                href = reader.getElementText();
            }
        }
        return new Icon(href);
    }

    private static WGS84Coordinate toWgs84Coordinate(final String value) {
        final var split = value.split(",", -1);
        final var lon = Double.parseDouble(split[0]);
        final var lat = Double.parseDouble(split[1]);
        final var wgs84Coordinate = new WGS84Coordinate(lat, lon);

        if (split.length > 2 && !split[2].isBlank()) {
            final var elevation = Double.parseDouble(split[2]);
            return wgs84Coordinate.withElevation(elevation);
        }
        return wgs84Coordinate;
    }
}
