package com.supermart.web;

import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Small request-parameter helpers shared by the controllers. */
final class WebUtils {

    private WebUtils() {
    }

    static String string(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    static int integer(HttpServletRequest request, String name, int fallback) {
        try {
            return Integer.parseInt(string(request, name));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    static BigDecimal decimal(HttpServletRequest request, String name) {
        try {
            return new BigDecimal(string(request, name));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static LocalDate date(HttpServletRequest request, String name) {
        try {
            return LocalDate.parse(string(request, name));
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
