package com.supermart.dao;

/**
 * Builds a SQL {@code LIKE} substring pattern from raw user input.
 *
 * <p>{@code LIKE} treats {@code %} and {@code _} as wildcards even when they
 * arrive as bound parameters -- binding a value through a
 * {@link java.sql.PreparedStatement} protects against SQL injection, but it
 * does not stop those two characters from being interpreted as wildcards once
 * the pattern reaches the {@code LIKE} engine. A search for an employee whose
 * name genuinely contains an underscore would otherwise match any single
 * character instead. This escapes both characters, and the escape character
 * itself, so a {@code LIKE} search matches the term literally except where
 * {@code %} was added on purpose to mean "any substring".
 */
final class LikePatterns {

    private static final char ESCAPE = '\\';

    private LikePatterns() {
    }

    /** Wraps {@code term} for a {@code LIKE ? ESCAPE '\'} substring match. */
    static String substringMatch(String term) {
        String raw = term == null ? "" : term;
        StringBuilder escaped = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char ch = raw.charAt(i);
            if (ch == ESCAPE || ch == '%' || ch == '_') {
                escaped.append(ESCAPE);
            }
            escaped.append(ch);
        }
        return "%" + escaped + "%";
    }
}
