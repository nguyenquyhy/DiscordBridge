package com.nguyenquyhy.discordbridge.utils;

import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

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

    public static TextColor getTextColor(String s) {
        switch (s.toUpperCase()){
            case "§0":
            case "&0":
                return TextColors.BLACK;
            case "§1":
            case "&1":
                return TextColors.DARK_BLUE;
            case "§2":
            case "&2":
                return TextColors.DARK_GREEN;
            case "§3":
            case "&3":
                return TextColors.DARK_AQUA;
            case "§4":
            case "&4":
                return TextColors.DARK_RED;
            case "§5":
            case "&5":
                return TextColors.DARK_PURPLE;
            case "§6":
            case "&6":
                return TextColors.GOLD;
            case "§7":
            case "&7":
                return TextColors.GRAY;
            case "§8":
            case "&8":
                return TextColors.DARK_GRAY;
            case "§9":
            case "&9":
                return TextColors.BLUE;
            case "§A":
            case "&A":
                return TextColors.GREEN;
            case "§B":
            case "&B":
                return TextColors.AQUA;
            case "§C":
            case "&C":
                return TextColors.RED;
            case "§D":
            case "&D":
                return TextColors.LIGHT_PURPLE;
            case "§E":
            case "&E":
                return TextColors.YELLOW;
            case "§F":
            case "&F":
                return TextColors.WHITE;
            default: return TextColors.RESET;
        }
    }
}
