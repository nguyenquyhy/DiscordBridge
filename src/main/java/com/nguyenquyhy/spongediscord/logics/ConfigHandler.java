package com.nguyenquyhy.spongediscord.logics;

import com.nguyenquyhy.spongediscord.SpongeDiscord;
import com.nguyenquyhy.spongediscord.database.InMemoryStorage;
import com.nguyenquyhy.spongediscord.database.JsonFileStorage;
import com.nguyenquyhy.spongediscord.utils.ConfigUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Hy on 8/6/2016.
 */
public class ConfigHandler {

    public static void loadConfiguration(Config config) {
        SpongeDiscord mod = SpongeDiscord.getInstance();
        Logger logger = mod.getLogger();
        Path configDir = mod.getConfigDir();

        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            Path configFile = Paths.get(configDir + "/config.conf");
            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(configFile).build();

            CommentedConfigurationNode configNode;

            if (!Files.exists(configFile)) {
                Files.createFile(configFile);
                logger.info("Created default configuration, ConfigDatabase will not run until you have edited this file!");
            }
            configNode = configLoader.load();

            config.DEBUG = configNode.getNode("Debug").getString();
            config.BOT_TOKEN = ConfigUtil.readString(configNode, "BotToken", "");
            config.CHANNEL_ID = ConfigUtil.readString(configNode, "Channel", "");
            config.INVITE_CODE = ConfigUtil.readString(configNode, "InviteCode", "");
            config.JOINED_MESSAGE = ConfigUtil.readString(configNode, "JoinedMessageTemplate", "_%s just joined the server_");
            config.LEFT_MESSAGE = ConfigUtil.readString(configNode, "LeftMessageTemplate", "_%s just left the server_");
            config.MESSAGE_DISCORD_TEMPLATE = ConfigUtil.readString(configNode, "MessageInDiscordTemplate", "%s");
            config.MESSAGE_DISCORD_ANONYMOUS_TEMPLATE = ConfigUtil.readString(configNode, "MessageInDiscordAnonymousTemplate", "_<%a>_ %s");
            config.MESSAGE_MINECRAFT_TEMPLATE = ConfigUtil.readString(configNode, "MessageInMinecraftTemplate", "&7<%a> &f%s");
            config.MESSAGE_DISCORD_SERVER_UP = ConfigUtil.readString(configNode, "MessageInDiscordServerUp", "Server has started.");
            config.MESSAGE_DISCORD_SERVER_DOWN = ConfigUtil.readString(configNode, "MessageInDiscordServerDown", "Server has stopped.");
            String tokenStore = ConfigUtil.readString(configNode, "TokenStore", "JSON");

            configLoader.save(configNode);

            // TODO: exit if channel is empty
            if (StringUtils.isBlank(config.CHANNEL_ID)) {
                logger.error("Channel ID is not set!");
            }

            switch (tokenStore) {
                case "InMemory":
                    mod.setStorage(new InMemoryStorage());
                    logger.info("Use InMemory storage.");
                    break;
                case "JSON":
                    mod.setStorage(new JsonFileStorage(configDir));
                    logger.info("Use JSON storage.");
                    break;
                default:
                    logger.warn("Invalid TokenStore config. JSON setting will be used!");
                    mod.setStorage(new JsonFileStorage(configDir));
                    break;
            }
            logger.info("Configuration loaded. Channel " + config.CHANNEL_ID);
        } catch (IOException e) {
            logger.error("Couldn't create default configuration file!", e);
        }
    }
}
