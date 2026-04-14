package me.pikashrey.glimzocore.utilities.chat;

import org.bukkit.Color;
import org.bukkit.DyeColor;

/**
 * Color conversion helpers - Bukkit Color ↔ chat codes, dye colors, hex parsing.
 */
public final class ColorUtil {

    private ColorUtil() {}

    /** Parse a hex color string (#RRGGBB or RRGGBB) into a Bukkit Color. */
    public static Color fromHex(String hex) {
        if (hex == null) return Color.WHITE;
        hex = hex.replace("#", "").trim();
        if (hex.length() != 6) return Color.WHITE;
        try {
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return Color.fromRGB(r, g, b);
        } catch (NumberFormatException e) {
            return Color.WHITE;
        }
    }

    /** Convert a Bukkit Color to a hex string (without #). */
    public static String toHex(Color color) {
        return String.format("%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static DyeColor fromCode(char code) {
        switch (code) {
            case '0': return DyeColor.BLACK;
            case '1': return DyeColor.BLUE;
            case '2': return DyeColor.GREEN;
            case '3': return DyeColor.CYAN;
            case '4': return DyeColor.RED;
            case '5': return DyeColor.PURPLE;
            case '6': return DyeColor.ORANGE;
            case '7': return DyeColor.SILVER;
            case '8': return DyeColor.GRAY;
            case '9': return DyeColor.LIGHT_BLUE;
            case 'a': return DyeColor.LIME;
            case 'b': return DyeColor.LIGHT_BLUE;
            case 'c': return DyeColor.RED;
            case 'd': return DyeColor.PINK;
            case 'e': return DyeColor.YELLOW;
            case 'f': return DyeColor.WHITE;
            default:  return DyeColor.WHITE;
        }
    }

    /**
     * Map a & color code character to the nearest RGB Color.
     */
    public static Color colorFromCode(char code) {
        switch (code) {
            case '0': return Color.fromRGB(0,   0,   0  );
            case '1': return Color.fromRGB(0,   0,   170);
            case '2': return Color.fromRGB(0,   170, 0  );
            case '3': return Color.fromRGB(0,   170, 170);
            case '4': return Color.fromRGB(170, 0,   0  );
            case '5': return Color.fromRGB(170, 0,   170);
            case '6': return Color.fromRGB(255, 170, 0  );
            case '7': return Color.fromRGB(170, 170, 170);
            case '8': return Color.fromRGB(85,  85,  85 );
            case '9': return Color.fromRGB(85,  85,  255);
            case 'a': return Color.fromRGB(85,  255, 85 );
            case 'b': return Color.fromRGB(85,  255, 255);
            case 'c': return Color.fromRGB(255, 85,  85 );
            case 'd': return Color.fromRGB(255, 85,  255);
            case 'e': return Color.fromRGB(255, 255, 85 );
            case 'f': return Color.fromRGB(255, 255, 255);
            default:  return Color.WHITE;
        }
    }
}

