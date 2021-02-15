package ch.ubique.backend.geo.kml.parser;


import static ch.ubique.backend.geo.coordinate.util.CoordinateConverter.toLV95;

import ch.ubique.backend.geo.coordinate.models.LV95Coordinate;
import ch.ubique.backend.geo.coordinate.models.WGS84Coordinate;
import ch.ubique.backend.geo.kml.models.Icon;
import ch.ubique.backend.geo.kml.models.ItemStyle;
import ch.ubique.backend.geo.kml.models.KmlDocument;
import ch.ubique.backend.geo.kml.models.Placemark;
import ch.ubique.backend.geo.kml.models.Style;
import ch.ubique.backend.geo.kml.models.StyleMap;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

    public static KmlDocument parse(String path) throws XMLStreamException, FileNotFoundException {
        return parse(new FileInputStream(path));
    }


    public static KmlDocument parse(InputStream inputStream) throws XMLStreamException {

        List<Placemark> placemarks = new LinkedList<>();
        Map<String, Style> styles = new HashMap<>();
        Map<String, StyleMap> styleMaps = new HashMap<>();

        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader reader = inputFactory.createXMLStreamReader(inputStream, "UTF-8");

        logger.info("Parsing KML");
        while (reader.hasNext()) {
            reader.next();
            int type = reader.getEventType();
            if (type == XMLStreamConstants.START_ELEMENT) {
                String elementName = reader.getLocalName();
                switch (elementName) {
                    case "Placemark":
                        Placemark placemark = parsePlacemark(reader);
                        placemarks.add(placemark);
                        break;
                    case "Style":
                        String styleId = reader.getAttributeValue(0);
                        Style style = parseStyle(reader);
                        styles.put(styleId, style);
                        break;
                    case "StyleMap":
                        String id = reader.getAttributeValue(0);
                        StyleMap styleMap = parseStyleMap(reader);
                        styleMaps.put(id, styleMap);
                        break;
                }
            }
        }

        logger.info("Parsing KML finished");
        return new KmlDocument()
                .setPlacemarks(placemarks)
                .setStyleMaps(styleMaps)
                .setStyles(styles);
    }

    private static Placemark parsePlacemark(XMLStreamReader reader) throws XMLStreamException {
        Placemark placemark = new Placemark();
        while (reader.hasNext()) {
            reader.next();
            int eventType = reader.getEventType();
            String elementName;
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                elementName = reader.getLocalName();
                switch (elementName) {
                    case "name":
                        placemark.setName(reader.getElementText());
                        break;
                    case "description":
                        placemark.setDescription(reader.getElementText());
                        break;
                    case "styleUrl":
                        placemark.setStyleId(reader.getElementText().replace("#", ""));
                        break;
                    case "coordinates":
                        String coordinateList = reader.getElementText();
                        List<LV95Coordinate> coordinates = Arrays
                                .stream(coordinateList.split("\\s"))
                                .map(String::trim)
                                .filter(v -> !v.isEmpty())
                                .map(KmlParser::toSwissCoordinate)
                                .collect(Collectors.toList());
                        placemark.setCoordinates(coordinates);
                        break;
                    case "SimpleData":
                        String key = reader.getAttributeValue(0);
                        String value = reader.getElementText();
                        placemark.getExtendedData().put(key, value);
                        break;
                }
            } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                elementName = reader.getLocalName();
                if (elementName.equals("Placemark")) {
                    return placemark;
                }
            }
        }
        return null;
    }

    private static Style parseStyle(XMLStreamReader reader) throws XMLStreamException {
        Style style = new Style();
        while (reader.hasNext()) {
            reader.next();
            int eventType = reader.getEventType();
            String elementName;
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                elementName = reader.getLocalName();
                switch (elementName) {
                    case "IconStyle":
                        style.setIconStyle(parseItemStyle(reader, elementName));
                        break;
                    case "LabelStyle":
                        style.setLabelStyle(parseItemStyle(reader, elementName));
                        break;
                    case "LineStyle":
                        style.setLineStyle(parseItemStyle(reader, elementName));
                        break;
                }
            } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                elementName = reader.getLocalName();
                if (elementName.equals("Style")) {
                    return style;
                }
            }
        }
        return null;
    }

    private static ItemStyle parseItemStyle(XMLStreamReader reader, String endTag) throws XMLStreamException {
        ItemStyle style = new ItemStyle();
        while (reader.hasNext()) {
            reader.next();
            int eventType = reader.getEventType();
            String elementName;
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                elementName = reader.getLocalName();
                switch (elementName) {
                    case "color":
                        style.setColor(reader.getElementText());
                        break;
                    case "scale":
                        style.setScale(Double.parseDouble(reader.getElementText()));
                        break;
                    case "width":
                        style.setWidth(Double.parseDouble(reader.getElementText()));
                        break;
                    case "Icon":
                        style.setIcon(parseIcon(reader));
                        break;
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

    private static StyleMap parseStyleMap(XMLStreamReader reader) throws XMLStreamException {
        StyleMap styleMap = new StyleMap();
        while (reader.hasNext()) {
            reader.next();
            int eventType = reader.getEventType();
            String elementName;
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                elementName = reader.getLocalName();
                if (elementName.equals("Pair")) {
                    addPairToStyleMap(reader, styleMap);
                }
            } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                elementName = reader.getLocalName();
                if (elementName.equals("StyleMap")) {
                    return styleMap;
                }
            }
        }
        return null;
    }

    private static void addPairToStyleMap(XMLStreamReader reader, StyleMap styleMap) throws XMLStreamException {
        String key = null;
        String value = null;
        while (reader.hasNext()) {
            reader.next();
            int eventType = reader.getEventType();
            String elementName;
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                elementName = reader.getLocalName();
                if ("key".equals(elementName)) {
                    key = reader.getElementText();
                } else if ("styleUrl".equals(elementName)) {
                    value = reader.getElementText().replace("#", "");
                }
            } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                elementName = reader.getLocalName();
                if (elementName.equals("Pair")) {
                    if (key != null && value != null) {
                        styleMap.getMap().put(key, value);
                    }
                    return;
                }
            }
        }
    }

    private static Icon parseIcon(XMLStreamReader reader) throws XMLStreamException {
        Icon icon = new Icon();
        while (reader.hasNext()) {
            reader.next();
            int eventType = reader.getEventType();
            String elementName;
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                elementName = reader.getLocalName();
                if ("href".equals(elementName)) {
                    icon.setHref(reader.getElementText());
                }
            } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                elementName = reader.getLocalName();
                if (elementName.equals("Icon")) {
                    return icon;
                }
            }
        }
        return null;
    }

    private static LV95Coordinate toSwissCoordinate(String value) {
        String[] split = value.split(",");
        double lon = Double.parseDouble(split[0]);
        double lat = Double.parseDouble(split[1]);
        WGS84Coordinate wgs84Coordinate = new WGS84Coordinate(lat, lon);

        if (split.length > 2) {
            double elevation = Double.parseDouble(split[2]);
            wgs84Coordinate.setElevation(elevation);
        }

        return toLV95(wgs84Coordinate);
    }
}
