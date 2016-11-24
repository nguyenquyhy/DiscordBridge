package com.nguyenquyhy.discordbridge;

import com.google.inject.Inject;
import com.nguyenquyhy.discordbridge.database.IStorage;
import com.nguyenquyhy.discordbridge.listeners.ChatListener;
import com.nguyenquyhy.discordbridge.listeners.ClientConnectionListener;
import com.nguyenquyhy.discordbridge.logics.ConfigHandler;
import com.nguyenquyhy.discordbridge.logics.LoginHandler;
import com.nguyenquyhy.discordbridge.models.ChannelConfig;
import com.nguyenquyhy.discordbridge.models.GlobalConfig;
import com.nguyenquyhy.discordbridge.utils.ErrorMessages;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Channel;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by Hy on 1/4/2016.
 */
@Plugin(id = "discordbridge", name = "Discord Bridge", version = "2.1.0",
        description = "A Sponge plugin to connect your Minecraft server with Discord", authors = {"Hy"})
public class DiscordBridge {
            
    private DiscordAPI consoleClient = null;
    private final Map<UUID, DiscordAPI> humanClients = new HashMap<>();
    private DiscordAPI botClient = null;

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
    public void onPreInitialization(GamePreInitializationEvent event) throws IOException, ObjectMappingException {
        instance = this;
        config = ConfigHandler.loadConfiguration();

        Sponge.getEventManager().registerListeners(this, new ChatListener());
        Sponge.getEventManager().registerListeners(this, new ClientConnectionListener());
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
                    Channel channel = botClient.getChannelById(channelConfig.discordId);
                    if (channel != null) {
                        channel.sendMessage(channelConfig.discord.serverDownMessage, false);
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

    public DiscordAPI getBotClient() {
        return botClient;
    }

    public void setBotClient(DiscordAPI botClient) {
        this.botClient = botClient;
    }

    public Map<UUID, DiscordAPI> getHumanClients() {
        return humanClients;
    }

    public Set<UUID> getUnauthenticatedPlayers() {
        return unauthenticatedPlayers;
    }

    public void addClient(UUID player, DiscordAPI client) {
        if (player == null) {
            consoleClient = client;
        } else {
            humanClients.put(player, client);
        }
    }

    public void removeAndLogoutClient(UUID player) {
        if (player == null) {
            consoleClient.disconnect();
            consoleClient = null;
        } else {
            if (humanClients.containsKey(player)) {
                DiscordAPI client = humanClients.get(player);
                client.disconnect();
                humanClients.remove(player);
            }
        }
    }
}
