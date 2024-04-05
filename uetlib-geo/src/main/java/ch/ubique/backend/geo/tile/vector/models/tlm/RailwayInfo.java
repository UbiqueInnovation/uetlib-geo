package ch.ubique.backend.geo.tile.vector.models.tlm;

import ch.ubique.backend.geo.tlm.models.Gauge;
import ch.ubique.backend.geo.tlm.models.RailServiceType;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record RailwayInfo(
        @NotNull int id,
        @NotNull String uuid,
        @NotNull Gauge gauge,
        @NotNull RailServiceType railServiceType,
        @NotNull Instant created,
        @NotNull Instant changeTimestamp,
        @NotNull String changeReason,
        @NotNull TlmSource source,
        @NotNull Instant sourceTimestamp,
        @NotNull Instant revisionTimestamp,
        @NotNull int level,
        @NotNull boolean siding,
        @NotNull boolean selectedForDigitalMap,
        @NotNull boolean museumTrain,
        @NotNull boolean onStreet,
        @NotNull boolean outOfOrder,
        @NotNull boolean rackRailway,
        @NotNull boolean funicular,
        @NotNull boolean serviceRailway,
        @Nullable String name,
        @Nullable Structure structure,
        @Nullable Integer numberOfTracks,
        @Nullable Instant openingTimestamp,
        @Nullable String revisionQuality,
        @Nullable String tlmPublicTransportNameUuid)
        implements TlmInfo {

    public RailwayInfo(final UnstructuredTlmInfo unstructured) {
        this(
                unstructured.getInt("id").orElseThrow(),
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
