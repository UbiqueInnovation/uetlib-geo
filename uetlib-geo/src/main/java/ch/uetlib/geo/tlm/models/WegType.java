package ch.uetlib.geo.tlm.models;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.NoSuchElementException;

public enum WegType {
    AUSFAHRT,
    AUTOBAHN,
    AUTOSTRASSE,
    AUTOZUG,
    DIENSTZUFAHRT,
    EINFAHRT,
    FAEHRE,
    KLETTERSTEIG,
    MARKIERTE_SPUR,
    PLATZ,
    RASTSTAETTE,
    STRASSE_3M,
    STRASSE_4M,
    STRASSE_6M,
    STRASSE_8M,
    STRASSE_10M,
    VERBINDUNG,
    WEG_1M,
    WEG_2M,
    WEGFRAGMENT_1M,
    WEGFRAGMENT_2M,
    ZUFAHRT,
    PROVISORIUM;

    public static WegType parse(final String value) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("value must not be blank");
        }
        final var uppercaseValue = value.toUpperCase().replace(' ', '_');
        final var parsed = EnumUtils.getEnumIgnoreCase(WegType.class, uppercaseValue);
        if (parsed != null) {
            return parsed;
        }

        return switch (uppercaseValue) {
            case "1M_WEG" -> WEG_1M;
            case "1M_WEGFRAGMENT" -> WEGFRAGMENT_1M;
            case "2M_WEG" -> WEG_2M;
            case "2M_WEGFRAGMENT" -> WEGFRAGMENT_2M;
            case "3M_STRASSE" -> STRASSE_3M;
            case "4M_STRASSE" -> STRASSE_4M;
            case "6M_STRASSE" -> STRASSE_6M;
            case "8M_STRASSE" -> STRASSE_8M;
            case "10M_STRASSE" -> STRASSE_10M;
            default ->
                    throw new NoSuchElementException(
                            "No enum constant "
                                    + WegType.class.getCanonicalName()
                                    + '.'
                                    + uppercaseValue);
        };
    }
}
