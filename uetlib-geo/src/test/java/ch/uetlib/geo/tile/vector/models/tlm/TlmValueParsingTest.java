package ch.uetlib.geo.tile.vector.models.tlm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ch.uetlib.geo.graph.routing.GraphType;
import ch.uetlib.geo.tlm.models.WegType;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

class TlmValueParsingTest {

    private Locale defaultLocale;

    @BeforeEach
    void useTurkishDefaultLocale() {
        defaultLocale = Locale.getDefault();
        // Turkish lowercases "I" to a dotless "ı" and uppercases "i" to a dotted "İ".
        Locale.setDefault(Locale.forLanguageTag("tr-TR"));
    }

    @AfterEach
    void restoreDefaultLocale() {
        Locale.setDefault(defaultLocale);
    }

    @Test
    void structureParsesAllValues() {
        assertNull(Structure.parse("KEINE"));
        assertNull(Structure.parse("IN/AUF GEBAEUDE"));
        assertEquals(Structure.BRIDGE, Structure.parse("BRUECKE"));
        assertEquals(Structure.TUNNEL, Structure.parse("Tunnel"));
        assertEquals(Structure.UNDERPASS, Structure.parse("Unterfuehrung"));
        assertEquals(Structure.UNDERPASS_WITH_STAIRS, Structure.parse("Unterfuehrung mit Treppe"));
        assertEquals(Structure.ROCK_SHED, Structure.parse("GALERIE"));
        assertEquals(Structure.BRIDGE_WITH_ROCK_SHED, Structure.parse("Bruecke mit Galerie"));
        assertEquals(Structure.BRIDGE_WITH_COVER, Structure.parse("Gedeckte Bruecke"));
        assertEquals(Structure.BRIDGE_WITH_COVER, Structure.parse("Bruecke mit Treppe"));
        assertEquals(Structure.DAM, Structure.parse("Staudamm"));
        assertEquals(Structure.DAM_WEIR, Structure.parse("Staumauer, Wehr"));
        assertEquals(Structure.FOOTBRIDGE, Structure.parse("Steg"));
        assertEquals(Structure.STAIRS, Structure.parse("Treppe"));
        assertEquals(Structure.FORD, Structure.parse("Furt"));
        assertThrows(IllegalStateException.class, () -> Structure.parse("Turm"));
    }

    @Test
    void trafficRestrictionParsesAllValues() {
        assertEquals(TrafficRestriction.NONE, TrafficRestriction.parse("KEINE"));
        assertEquals(TrafficRestriction.CLOSED, TrafficRestriction.parse("Gesperrt"));
        assertEquals(
                TrafficRestriction.TRAFFIC_BAN, TrafficRestriction.parse("ALLGEMEINES FAHRVERBOT"));
        assertEquals(
                TrafficRestriction.GENERAL_TRAFFIC_RESTRICTION,
                TrafficRestriction.parse("Allgemeine Verkehrsbeschraenkung"));
        assertEquals(
                TrafficRestriction.GESICHERTE_KLETTERPARTIE,
                TrafficRestriction.parse("Gesicherte Kletterpartie"));
        assertEquals(TrafficRestriction.MILITARY_ROAD, TrafficRestriction.parse("MILITAERSTRASSE"));
        assertEquals(TrafficRestriction.PANZERPISTE, TrafficRestriction.parse("Panzerpiste"));
        assertEquals(TrafficRestriction.TIME_BASED, TrafficRestriction.parse("Zeitlich geregelt"));
        assertEquals(TrafficRestriction.TEST_TRACK, TrafficRestriction.parse("Teststrecke"));
        assertEquals(TrafficRestriction.RACING_TRACK, TrafficRestriction.parse("RENNSTRECKE"));
        assertThrows(IllegalStateException.class, () -> TrafficRestriction.parse("Einbahn"));
    }

    @Test
    void unstructuredInfoParsesBooleans() {
        final var info =
                new UnstructuredTlmInfo(
                        Map.of("yes", "WAHR", "no", "Falsch", "other", "vielleicht"));

        assertEquals(Optional.of(true), info.getBoolean("yes"));
        assertEquals(Optional.of(false), info.getBoolean("no"));
        assertThrows(IllegalStateException.class, () -> info.getBoolean("other"));
    }

    @Test
    void wegTypeAndGraphTypeIgnoreTheDefaultLocale() {
        assertEquals(WegType.PROVISORIUM, WegType.parse("provisorium"));
        assertEquals(WegType.MARKIERTE_SPUR, WegType.parse("markierte spur"));
        assertEquals("routing", GraphType.ROUTING.toString());
        assertEquals("highway_routing", GraphType.HIGHWAY_ROUTING.toString());
    }
}
