package me.pikashrey.glimzocore.utilities.general;

import java.util.concurrent.TimeUnit;

/**
 * Formats millisecond durations into human-readable strings.
 */
public final class TimeFormatUtils {

    private TimeFormatUtils() {}

    public static String format(long ms) {
        if (ms <= 0) return "0s";

        long seconds = ms / 1000;
        long minutes = seconds / 60; seconds %= 60;
        long hours   = minutes / 60; minutes %= 60;
        long days    = hours   / 24; hours   %= 24;

        StringBuilder sb = new StringBuilder();
        if (days    > 0) sb.append(days).append("d ");
        if (hours   > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0 || sb.length() == 0) sb.append(seconds).append("s");

        return sb.toString().trim();
    }

    public static String formatExpiry(long epochMs) {
        if (epochMs == -1) return "Permanent";
        long remaining = epochMs - System.currentTimeMillis();
        if (remaining <= 0) return "Expired";
        return format(remaining);
    }

    public static long parse(String input) {
        if (input == null) return 0;
        String s = input.trim().toLowerCase();
        if (s.equals("perm") || s.equals("permanent") || s.equals("-1")) return -1;

        long total = 0;
        StringBuilder num = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isDigit(c)) {
                num.append(c);
            } else {
                if (num.length() == 0) return 0;
                long value = Long.parseLong(num.toString());
                num.setLength(0);
                switch (c) {
                    case 's': total += value * 1000L;                      break;
                    case 'm': total += value * 60_000L;                    break;
                    case 'h': total += value * 3_600_000L;                 break;
                    case 'd': total += value * 86_400_000L;                break;
                    case 'w': total += value * 604_800_000L;               break;
                    default:  return 0;
                }
            }
        }
        return total;
    }

    /** Convert epoch ms to a formatted date string "dd/MM/yyyy HH:mm". */
    public static String formatDate(long epochMs) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        return sdf.format(new java.util.Date(epochMs));
    }
}

