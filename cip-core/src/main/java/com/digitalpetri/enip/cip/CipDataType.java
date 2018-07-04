package com.digitalpetri.enip.cip;

import java.util.Optional;

/**
 * CIP elementary data types and their identification codes, as defined by Volume 1, Appendix C.
 */
public enum CipDataType {

    /**
     * Logical Boolean with values TRUE and FALSE.
     */
    BOOL(0xC1),

    /**
     * Signed 8–bit integer value.
     */
    SINT(0xC2),

    /**
     * Signed 16–bit integer value.
     */
    INT(0xC3),

    /**
     * Signed 32–bit integer value.
     */
    DINT(0xC4),

    /**
     * Signed 64–bit integer value.
     */
    LINT(0xC5),

    /**
     * Unsigned 8–bit integer value.
     */
    USINT(0xC6),

    /**
     * Unsigned 16–bit integer value.
     */
    UINT(0xC7),

    /**
     * Unsigned 32–bit integer value.
     */
    UDINT(0xC8),

    /**
     * Unsigned 64–bit integer value.
     */
    ULINT(0xC9),

    /**
     * 32-bit floating point value.
     */
    REAL(0xCA),

    /**
     * 64-bit floating point value.
     */
    LREAL(0xCB),

    /**
     * Synchronous time information.
     */
    STIME(0xCC),

    /**
     * Date information.
     */
    DATE(0xCD),

    /**
     * Time of day.
     */
    TIME_OF_DAY(0xCE),

    /**
     * Date and time of day.
     */
    DATE_AND_TIME(0xCF),

    /**
     * Character string (1 byte per character).
     */
    STRING(0xD0),

    /**
     * Bit string, 8-bit.
     */
    BYTE(0xD1),

    /**
     * Bit string, 16-bit.
     */
    WORD(0xD2),

    /**
     * Bit string, 32-bit.
     */
    DWORD(0xD3),

    /**
     * Bit string, 64-bit.
     */
    LWORD(0xD4),

    /**
     * Character string (2 bytes per character).
     */
    STRING2(0xD5),

    /**
     * Duration (high resolution).
     */
    FTIME(0xD6),

    /**
     * Duration (long).
     */
    LTIME(0xD7),

    /**
     * Duration (short).
     */
    ITIME(0xD8),

    /**
     * Character string (N bytes per character).
     */
    STRINGN(0xD9),

    /**
     * Character string (1 byte per character, 1 byte length indicator).
     */
    SHORT_STRING(0xDA),

    /**
     * Duration (milliseconds).
     */
    TIME(0xDB),

    /**
     * CIP path segments.
     */
    EPATH(0xDC),

    /**
     * Engineering Units.
     */
    ENGUNIT(0xDD),

    /**
     * International Character String.
     */
    STRINGI(0xDE);

    private final int code;

    CipDataType(int code) {
        this.code = code;
    }

    public final int getCode() {
        return code;
    }

    /**
     * Look up the elementary {@link CipDataType} for a given identification code.
     *
     * @param code the code to look up.
     * @return an {@link Optional} containing the {@link CipDataType}, if one exists.
     */
    public static Optional<CipDataType> fromCode(int code) {
        for (CipDataType dataType : values()) {
            if (dataType.getCode() == code) {
                return Optional.of(dataType);
            }
        }
        return Optional.empty();
    }

}
