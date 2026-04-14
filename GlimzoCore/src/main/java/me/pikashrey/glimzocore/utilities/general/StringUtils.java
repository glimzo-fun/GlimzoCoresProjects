package me.pikashrey.glimzocore.utilities.general;

import java.util.List;

public final class StringUtils {

    private StringUtils() {}

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

    public static String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(s);
        return sb.toString();
    }

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

    public static String truncate(String s, int max) {
        if (s == null || s.length() <= max) return s;
        return s.substring(0, max - 1) + "...";
    }

    public static String shortUuid(java.util.UUID uuid) {
        return uuid.toString().replace("-", "").substring(0, 8);
    }
}
