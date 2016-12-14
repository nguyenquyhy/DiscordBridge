package com.nguyenquyhy.discordbridge.utils;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ColorUtil {
    private static Map<Color, String> minecraftColors = new HashMap<>();

    static {
        minecraftColors.put(new Color(0,0,0), "&0");
        minecraftColors.put(new Color(0,0,170), "&1");
        minecraftColors.put(new Color(0,170,0), "&2");
        minecraftColors.put(new Color(0,170,170), "&3");
        minecraftColors.put(new Color(170,0,0), "&4");
        minecraftColors.put(new Color(233,30,99), "&4");
        minecraftColors.put(new Color(170,0,170), "&5");
        minecraftColors.put(new Color(255,170,0), "&6");
        minecraftColors.put(new Color(170,170,170), "&7");
        minecraftColors.put(new Color(85,85,85), "&8");
        minecraftColors.put(new Color(85,85,255), "&9");
        minecraftColors.put(new Color(85,255,85), "&a");
        minecraftColors.put(new Color(85,255,255), "&b");
        minecraftColors.put(new Color(255,85,85), "&c");
        minecraftColors.put(new Color(255,85,255), "&d");
        minecraftColors.put(new Color(255,255,85), "&e");
        minecraftColors.put(new Color(255,255,255), "&f");
    }

    public static String getColorCode(Color color) {
        return minecraftColors.containsKey(color) ? minecraftColors.get(color) : "";
    }
}