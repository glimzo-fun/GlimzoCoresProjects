package me.pikashrey.glimzocore.utilities.general;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utilities for formatting timestamps and computing relative time strings.
 */
public final class DateUtils {

    private static final SimpleDateFormat FULL    = new SimpleDateFormat("dd MMM yyyy, HH:mm");
    private static final SimpleDateFormat DATE    = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat TIME    = new SimpleDateFormat("HH:mm:ss");

    private DateUtils() {}

    public static String formatFull(long epochMs)  { return FULL.format(new Date(epochMs)); }
    public static String formatDate(long epochMs)   { return DATE.format(new Date(epochMs)); }
    public static String formatTime(long epochMs)   { return TIME.format(new Date(epochMs)); }

    public static String timeAgo(long epochMs) {
        long diff = System.currentTimeMillis() - epochMs;
        if (diff < 60_000)            return "just now";
        if (diff < 3_600_000)         return (diff / 60_000)      + " minute"  + plural(diff / 60_000) + " ago";
        if (diff < 86_400_000)        return (diff / 3_600_000)   + " hour"    + plural(diff / 3_600_000) + " ago";
        if (diff < 2_592_000_000L)    return (diff / 86_400_000)  + " day"     + plural(diff / 86_400_000) + " ago";
        if (diff < 31_536_000_000L)   return (diff / 2_592_000_000L) + " month" + plural(diff / 2_592_000_000L) + " ago";
        return (diff / 31_536_000_000L) + " year" + plural(diff / 31_536_000_000L) + " ago";
    }

    private static String plural(long v) { return v == 1 ? "" : "s"; }

    public static long nowMs() { return System.currentTimeMillis(); }
}

