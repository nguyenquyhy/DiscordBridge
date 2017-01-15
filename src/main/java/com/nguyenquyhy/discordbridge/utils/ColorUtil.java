package com.nguyenquyhy.discordbridge.utils;

import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ColorUtil {
    private static Map<Color, String> minecraftColors = new HashMap<>();
    private static Map<Color, TextColor> textColors = new HashMap<>();

    static {
        minecraftColors.put(new Color(0, 0, 0), "&0");
        minecraftColors.put(new Color(0, 0, 170), "&1");
        minecraftColors.put(new Color(0, 170, 0), "&2");
        minecraftColors.put(new Color(0, 170, 170), "&3");
        minecraftColors.put(new Color(170, 0, 0), "&4");
        minecraftColors.put(new Color(170, 0, 170), "&5");
        minecraftColors.put(new Color(255, 170, 0), "&6");
        minecraftColors.put(new Color(170, 170, 170), "&7");
        minecraftColors.put(new Color(85, 85, 85), "&8");
        minecraftColors.put(new Color(85, 85, 255), "&9");
        minecraftColors.put(new Color(85, 255, 85), "&a");
        minecraftColors.put(new Color(85, 255, 255), "&b");
        minecraftColors.put(new Color(255, 85, 85), "&c");
        minecraftColors.put(new Color(255, 85, 255), "&d");
        minecraftColors.put(new Color(255, 255, 85), "&e");
        minecraftColors.put(new Color(255, 255, 255), "&f");

        textColors.put(new Color(0, 0, 0), TextColors.BLACK);
        textColors.put(new Color(0, 0, 170), TextColors.DARK_BLUE);
        textColors.put(new Color(0, 170, 0), TextColors.DARK_GREEN);
        textColors.put(new Color(0, 170, 170), TextColors.DARK_AQUA);
        textColors.put(new Color(170, 0, 0), TextColors.DARK_RED);
        textColors.put(new Color(170, 0, 170), TextColors.DARK_PURPLE);
        textColors.put(new Color(255, 170, 0), TextColors.GOLD);
        textColors.put(new Color(170, 170, 170), TextColors.GRAY);
        textColors.put(new Color(85, 85, 85), TextColors.DARK_GRAY);
        textColors.put(new Color(85, 85, 255), TextColors.BLUE);
        textColors.put(new Color(85, 255, 85), TextColors.GREEN);
        textColors.put(new Color(85, 255, 255), TextColors.AQUA);
        textColors.put(new Color(255, 85, 85), TextColors.RED);
        textColors.put(new Color(255, 85, 255), TextColors.LIGHT_PURPLE);
        textColors.put(new Color(255, 255, 85), TextColors.YELLOW);
        textColors.put(new Color(255, 255, 255), TextColors.WHITE);
    }

    public static String getColorCode(Color color) {
        return minecraftColors.containsKey(color) ? minecraftColors.get(color) : "";
    }

    public static TextColor getColor(Color color) {
        TextColor result = null;
        double minDistance = 0;
        for (Color mcColor : textColors.keySet()) {
            double distance = (mcColor.getRed() - color.getRed()) * (mcColor.getRed() - color.getRed())
                    + (mcColor.getGreen() - color.getGreen()) * (mcColor.getGreen() - color.getGreen())
                    + (mcColor.getBlue() - color.getBlue()) * (mcColor.getBlue() - color.getBlue());
            if (result == null || minDistance > distance) {
                result = textColors.get(mcColor);
                minDistance = distance;
            }
        }
        return result;
    }
}