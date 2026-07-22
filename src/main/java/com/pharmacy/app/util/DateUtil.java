package com.pharmacy.app.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Converts between the ISO format stored in the database (yyyy-MM-dd, sortable and unambiguous)
 * and the dd/MM/yyyy format shown to users on screen and in printouts.
 * ALWAYS store ISO in the database - only format for display.
 */
public class DateUtil {

    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DISPLAY_DATETIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Formats an ISO date (yyyy-MM-dd) or ISO datetime (yyyy-MM-dd HH:mm:ss) string for display. */
    public static String toDisplay(String isoValue) {
        if (isoValue == null || isoValue.isBlank()) return "";

        try {
            if (isoValue.length() > 10) {
                // Has a time component
                String normalized = isoValue.replace(" ", "T");
                LocalDateTime dt = LocalDateTime.parse(normalized.length() > 19 ? normalized.substring(0, 19) : normalized);
                return dt.format(DISPLAY_DATETIME);
            } else {
                LocalDate d = LocalDate.parse(isoValue);
                return d.format(DISPLAY_DATE);
            }
        } catch (DateTimeParseException e) {
            return isoValue; // fall back to raw value if it doesn't parse cleanly
        }
    }
}