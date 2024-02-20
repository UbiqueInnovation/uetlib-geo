package ch.ubique.backend.geo.kml.parser;

import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.coordinate.util.JtsConverter;
import ch.ubique.backend.geo.kml.models.Icon;
import ch.ubique.backend.geo.kml.models.ItemStyle;
import ch.ubique.backend.geo.kml.models.KmlDocument;
import ch.ubique.backend.geo.kml.models.Placemark;
import ch.ubique.backend.geo.kml.models.Style;
import ch.ubique.backend.geo.kml.models.StyleMap;

import jakarta.validation.constraints.NotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class KmlParser {

    private static final Logger logger = Logger.getLogger(KmlParser.class.getName());

    private KmlParser() {}

    public static KmlDocument parse(final String path) throws IOException {
        return parse(new FileInputStream(path));
    }

    public static KmlDocument parse(final InputStream inputStream) throws IOException {

        final List<Placemark> placemarks = new LinkedList<>();
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
            return new KmlDocument()
                    .setPlacemarks(placemarks)
                    .setStyleMaps(styleMaps)
                    .setStyles(styles);
        } catch (final XMLStreamException e) {
            throw new IOException(e);
        }
    }

    private static Placemark parsePlacemark(final XMLStreamReader reader)
            throws XMLStreamException {
        final var placemark = new Placemark();
        var isPolygon = false;
        while (reader.hasNext()) {
            reader.next();
            final var eventType = reader.getEventType();
            final String elementName;
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                elementName = reader.getLocalName();
                switch (elementName) {
                    case "name" -> {
                        placemark.setName(reader.getElementText());
                        if ("286-TI-40015".equals(placemark.getName())) {
                            logger.info("debug");
                        }
                    }
                    case "description" -> placemark.setDescription(reader.getElementText());
                    case "styleUrl" ->
                            placemark.setStyleId(reader.getElementText().replace("#", ""));
                    case "coordinates" -> {
                        final var coordinateList = reader.getElementText();
                        final var coordinates =
                                Arrays.stream(coordinateList.split("\\s"))
                                        .map(String::trim)
                                        .filter(v -> !v.isEmpty())
                                        .map(KmlParser::toSwissCoordinate)
                                        .collect(Collectors.toList());
                        if (isPolygon && !coordinates.isEmpty()) {
                            // make sure first and last coordinate are the same. If not, replace
                            // last with first.
                            final var first = coordinates.getFirst();
                            final var last = coordinates.getLast();
                            if (!first.equals(last)) {
                                coordinates.set(
                                        coordinates.size() - 1,
                                        new LV95Coordinate(
                                                first.east(), first.north(), first.elevation()));
                            }
                        }
                        placemark.addCoordinates(coordinates);
                    }
                    case "SimpleData" -> {
                        final var key = reader.getAttributeValue(0);
                        final var value = reader.getElementText();
                        placemark.getExtendedData().put(key, value);
                    }
                    case "Polygon" -> isPolygon = true;
                    default -> {
                        /* NO-OP */
                    }
                }
            } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                elementName = reader.getLocalName();
                if ("Placemark".equals(elementName)) {
                    return placemark;
                } else if ("Polygon".equals(elementName)) {
                    isPolygon = false;
                }
            }
        }
        return null;
    }

    private static Style parseStyle(final XMLStreamReader reader) throws XMLStreamException {
        final var style = new Style();
        while (reader.hasNext()) {
            reader.next();
            final var eventType = reader.getEventType();
            final String elementName;
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                elementName = reader.getLocalName();
                switch (elementName) {
                    case "IconStyle" -> style.setIconStyle(parseItemStyle(reader, elementName));
                    case "LabelStyle" -> style.setLabelStyle(parseItemStyle(reader, elementName));
                    case "LineStyle" -> style.setLineStyle(parseItemStyle(reader, elementName));
                    default -> {
                        /* NO-OP */
                    }
                }
            } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                elementName = reader.getLocalName();
                if ("Style".equals(elementName)) {
                    return style;
                }
            }
        }
        return null;
    }

    private static ItemStyle parseItemStyle(final XMLStreamReader reader, final String endTag)
            throws XMLStreamException {
        final var style = new ItemStyle();
        while (reader.hasNext()) {
            reader.next();
            final var eventType = reader.getEventType();
            final String elementName;
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                elementName = reader.getLocalName();
                switch (elementName) {
                    case "color" -> style.setColor(reader.getElementText());
                    case "scale" -> style.setScale(Double.parseDouble(reader.getElementText()));
                    case "tileWidth" -> style.setWidth(Double.parseDouble(reader.getElementText()));
                    case "Icon" -> style.setIcon(parseIcon(reader));
                    default -> {
                        /* NO-OP */
                    }
                }
            } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                elementName = reader.getLocalName();
                if (elementName.equals(endTag)) {
                    return style;
                }
            }
        }
        return null;
    }

    private static StyleMap parseStyleMap(final XMLStreamReader reader) throws XMLStreamException {
        final var styleMap = new StyleMap();
        while (reader.hasNext()) {
            reader.next();
            final var eventType = reader.getEventType();
            final String elementName;
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                elementName = reader.getLocalName();
                if ("Pair".equals(elementName)) {
                    addPairToStyleMap(reader, styleMap);
                }
            } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                elementName = reader.getLocalName();
                if ("StyleMap".equals(elementName)) {
                    return styleMap;
                }
            }
        }
        return null;
    }

    private static void addPairToStyleMap(final XMLStreamReader reader, final StyleMap styleMap)
            throws XMLStreamException {
        String key = null;
        String value = null;
        while (reader.hasNext()) {
            reader.next();
            final var eventType = reader.getEventType();
            final String elementName;
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                elementName = reader.getLocalName();
                if ("key".equals(elementName)) {
                    key = reader.getElementText();
                } else if ("styleUrl".equals(elementName)) {
                    value = reader.getElementText().replace("#", "");
                }
            } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                elementName = reader.getLocalName();
                if ("Pair".equals(elementName)) {
                    if (key != null && value != null) {
                        styleMap.getMap().put(key, value);
                    }
                    return;
                }
            }
        }
    }

    private static Icon parseIcon(final XMLStreamReader reader) throws XMLStreamException {
        final var icon = new Icon();
        while (reader.hasNext()) {
            reader.next();
            final var eventType = reader.getEventType();
            final String elementName;
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                elementName = reader.getLocalName();
                if ("href".equals(elementName)) {
                    icon.setHref(reader.getElementText());
                }
            } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                elementName = reader.getLocalName();
                if ("Icon".equals(elementName)) {
                    return icon;
                }
            }
        }
        return null;
    }

    private static LV95Coordinate toSwissCoordinate(final String value) {
        return JtsConverter.LV95.transform(toWgs84Coordinate(value));
    }

    @NotNull
    private static WGS84Coordinate toWgs84Coordinate(final String value) {
        final var split = value.split(",");
        final var lon = Double.parseDouble(split[0]);
        final var lat = Double.parseDouble(split[1]);
        final var wgs84Coordinate = new WGS84Coordinate(lat, lon);

        if (split.length > 2) {
            final var elevation = Double.parseDouble(split[2]);
            return wgs84Coordinate.withElevation(elevation);
        }
        return wgs84Coordinate;
    }
}
