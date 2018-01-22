package com.nguyenquyhy.discordbridge;

import com.google.inject.Inject;
import com.nguyenquyhy.discordbridge.database.IStorage;
import com.nguyenquyhy.discordbridge.listeners.ChatListener;
import com.nguyenquyhy.discordbridge.listeners.ClientConnectionListener;
import com.nguyenquyhy.discordbridge.listeners.DeathListener;
import com.nguyenquyhy.discordbridge.logics.ConfigHandler;
import com.nguyenquyhy.discordbridge.logics.LoginHandler;
import com.nguyenquyhy.discordbridge.models.ChannelConfig;
import com.nguyenquyhy.discordbridge.models.GlobalConfig;
import com.nguyenquyhy.discordbridge.utils.ChannelUtil;
import com.nguyenquyhy.discordbridge.utils.ErrorMessages;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.*;
import org.spongepowered.api.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by Hy on 1/4/2016.
 */
@Plugin(id = "discordbridge", name = "Discord Bridge", version = "3.0.0",
        description = "A Sponge plugin to connect your Minecraft server with Discord", authors = {"Hy", "Mohron"})
public class DiscordBridge {

    private JDA consoleClient = null;
    private final Map<UUID, JDA> humanClients = new HashMap<>();
    private JDA botClient = null;

    private final Set<UUID> unauthenticatedPlayers = new HashSet<>(100);

    @Inject
    private Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    private GlobalConfig config;

    @Inject
    private Game game;

    private IStorage storage;

    private static DiscordBridge instance;

    @Listener
    public void onInitialization(GameInitializationEvent event) throws IOException, ObjectMappingException {
        instance = this;
        config = ConfigHandler.loadConfiguration();

        Sponge.getEventManager().registerListeners(this, new ChatListener());
        Sponge.getEventManager().registerListeners(this, new ClientConnectionListener());
        Sponge.getEventManager().registerListeners(this, new DeathListener());
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        CommandRegistry.register();
        LoginHandler.loginBotAccount();
    }

    @Listener
    public void onServerStop(GameStoppingServerEvent event) {
        if (botClient != null) {
            for (ChannelConfig channelConfig : config.channels) {
                if (StringUtils.isNotBlank(channelConfig.discordId)
                        && channelConfig.discord != null
                        && StringUtils.isNotBlank(channelConfig.discord.serverDownMessage)) {
                    TextChannel channel = botClient.getTextChannelById(channelConfig.discordId);
                    if (channel != null) {
                        ChannelUtil.sendMessage(channel, channelConfig.discord.serverDownMessage);
                    } else {
                        ErrorMessages.CHANNEL_NOT_FOUND.log(channelConfig.discordId);
                    }
                }
            }
        }
    }

    public static DiscordBridge getInstance() {
        return instance;
    }

    public Game getGame() {
        return game;
    }

    public Path getConfigDir() {
        return configDir;
    }

    public GlobalConfig getConfig() {
        return config;
    }

    public void setConfig(GlobalConfig config) {
        this.config = config;
    }

    public Logger getLogger() {
        return logger;
    }

    public IStorage getStorage() {
        return storage;
    }

    public void setStorage(IStorage storage) {
        this.storage = storage;
    }

    public JDA getBotClient() {
        return botClient;
    }

    public void setBotClient(JDA botClient) {
        this.botClient = botClient;
    }

    public Map<UUID, JDA> getHumanClients() {
        return humanClients;
    }

    public Set<UUID> getUnauthenticatedPlayers() {
        return unauthenticatedPlayers;
    }

    public void addClient(UUID player, JDA client) {
        if (player == null) {
            consoleClient = client;
        } else {
            humanClients.put(player, client);
        }
    }

    public void removeAndLogoutClient(UUID player) {
        if (player == null) {
            consoleClient.shutdown();
            consoleClient = null;
        } else {
            if (humanClients.containsKey(player)) {
                JDA client = humanClients.get(player);
                client.shutdown();
                humanClients.remove(player);
            }
        }
    }
}
