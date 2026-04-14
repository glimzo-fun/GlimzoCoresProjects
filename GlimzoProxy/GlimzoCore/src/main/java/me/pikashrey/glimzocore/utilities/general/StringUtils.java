package me.pikashrey.glimzocore.utilities.general;

import java.util.List;

/**
 * General-purpose string helpers used across commands and managers.
 */
public final class StringUtils {

    private StringUtils() {}

    /** Join a string array from index start to end (inclusive) with a space. */
    public static String join(String[] args, int start) {
        return join(args, start, args.length - 1, " ");
    }

    public static String join(String[] args, int start, int end, String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i <= Math.min(end, args.length - 1); i++) {
            if (sb.length() > 0) sb.append(separator);
            sb.append(args[i]);
        }
        return sb.toString();
    }

    /** Join a list of strings with a separator. */
    public static String join(List<String> list, String separator) {
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            if (sb.length() > 0) sb.append(separator);
            sb.append(s);
        }
        return sb.toString();
    }

    public static String titleCase(String s) {
        if (s == null || s.isEmpty()) return s;
        String[] words = s.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (sb.length() > 0) sb.append(" ");
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)));
                if (w.length() > 1) sb.append(w.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    /** Repeat a string n times. */
    public static String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(s);
        return sb.toString();
    }

    /** Returns true if the string is null, empty, or only whitespace. */
    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static String progressBar(double progress, int length,
                                      char symbol, String filledColor, String emptyColor) {
        progress = Math.max(0, Math.min(1, progress));
        int filled = (int) Math.round(progress * length);
        String bar = filledColor + repeat(String.valueOf(symbol), filled)
                   + emptyColor  + repeat(String.valueOf(symbol), length - filled);
        return me.pikashrey.glimzocore.utilities.chat.CC.translate(bar);
    }

    /** Truncate a string to max characters, appending "…" if truncated. */
    public static String truncate(String s, int max) {
        if (s == null || s.length() <= max) return s;
        return s.substring(0, max - 1) + "…";
    }

    /** Convert a UUID to a compact 8-char hex prefix for display. */
    public static String shortUuid(java.util.UUID uuid) {
        return uuid.toString().replace("-", "").substring(0, 8);
    }
}

