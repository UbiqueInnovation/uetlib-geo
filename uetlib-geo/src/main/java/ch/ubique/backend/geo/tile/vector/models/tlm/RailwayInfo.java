package ch.ubique.backend.geo.tile.vector.models.tlm;

import ch.ubique.backend.geo.tlm.models.Gauge;
import ch.ubique.backend.geo.tlm.models.RailServiceType;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

public record RailwayInfo(
        @NonNull UnstructuredTlmInfo unstructured,
        @NonNull String id,
        @NonNull String uuid,
        @NonNull Gauge gauge,
        @NonNull RailServiceType railServiceType,
        @NonNull Instant created,
        @NonNull Instant changeTimestamp,
        @NonNull String changeReason,
        @NonNull TlmSource source,
        @NonNull Instant sourceTimestamp,
        @NonNull Instant revisionTimestamp,
        @NonNull int level,
        @NonNull boolean siding,
        @NonNull boolean selectedForDigitalMap,
        @NonNull boolean museumTrain,
        @NonNull boolean onStreet,
        @NonNull boolean outOfOrder,
        @NonNull boolean rackRailway,
        @NonNull boolean funicular,
        @NonNull boolean serviceRailway,
        @Nullable String name,
        @Nullable Structure structure,
        @Nullable Integer numberOfTracks,
        @Nullable Instant openingTimestamp,
        @Nullable String revisionQuality,
        @Nullable String tlmPublicTransportNameUuid)
        implements TlmInfo {

    public RailwayInfo(final UnstructuredTlmInfo unstructured) {
        this(
                unstructured,
                unstructured.getString("id").orElseThrow(),
                unstructured.getString("uuid").orElseThrow(),
                unstructured.getString("objektart").map(Gauge::parse).orElseThrow(),
                unstructured.getString("verkehrsmittel").map(RailServiceType::parse).orElseThrow(),
                unstructured.getInstant("datum_erstellung").orElseThrow(),
                unstructured.getInstant("datum_aenderung").orElseThrow(),
                unstructured.getString("grund_aenderung").orElseThrow(),
                unstructured.getString("herkunft").map(TlmSource::parse).orElseThrow(),
                unstructured.getInstant("herkunft_jahr", "herkunft_monat").orElseThrow(),
                unstructured.getInstant("revision_jahr", "revision_monat").orElseThrow(),
                unstructured.getInt("stufe").orElseThrow(),
                unstructured.getBoolean("anschlussgleis").orElseThrow(),
                unstructured.getBoolean("achse_dkm").orElseThrow(),
                unstructured.getBoolean("museumsbahn").orElseThrow(),
                unstructured.getBoolean("auf_strasse").orElseThrow(),
                unstructured.getBoolean("ausser_betrieb").orElseThrow(),
                unstructured.getBoolean("zahnradbahn").orElseThrow(),
                unstructured.getBoolean("standseilbahn").orElseThrow(),
                unstructured.getBoolean("betriebsbahn").orElseThrow(),
                unstructured.getString("name").orElse(null),
                unstructured.getString("kunstbaute").map(Structure::parse).orElse(null),
                unstructured.getInt("anzahl_spuren").orElse(null),
                unstructured.getInstant("eroeffnungsdatum").orElse(null),
                unstructured.getString("revision_qualitaet").orElse(null),
                unstructured.getString("tlm_oev_name_uuid").orElse(null));
    }
}
