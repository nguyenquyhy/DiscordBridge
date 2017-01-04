package com.nguyenquyhy.discordbridge.utils;

import ninja.leaping.configurate.ConfigurationNode;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by Hy on 5/31/2016.
 */
public class ConfigUtil {
    public static String readString(ConfigurationNode node, String name, String defaultValue) {
        if (node.getNode(name).getValue() == null)
            node.getNode(name).setValue(defaultValue);
        return node.getNode(name).getString();
    }

    /**
     * @param config       the config value to be read
     * @param defaultValue the value to use if config is null or empty
     * @return non-null non-empty config value or default
     */
    public static String get(String config, String defaultValue) {
        return (StringUtils.isBlank(config)) ? defaultValue : config;
    }
}
