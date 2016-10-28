package com.nguyenquyhy.discordbridge.utils;

import ninja.leaping.configurate.ConfigurationNode;

/**
 * Created by Hy on 5/31/2016.
 */
public class ConfigUtil {
    public static String readString(ConfigurationNode node, String name, String defaultValue) {
        if (node.getNode(name).getValue() == null)
            node.getNode(name).setValue(defaultValue);
        return node.getNode(name).getString();
    }

}
