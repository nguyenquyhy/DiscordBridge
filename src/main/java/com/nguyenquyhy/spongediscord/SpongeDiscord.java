package com.nguyenquyhy.spongediscord;

import com.google.inject.Inject;
import com.nguyenquyhy.spongediscord.database.IStorage;
import com.nguyenquyhy.spongediscord.logics.Config;
import com.nguyenquyhy.spongediscord.logics.ConfigHandler;
import com.nguyenquyhy.spongediscord.logics.LoginHandler;
import com.nguyenquyhy.spongediscord.utils.TextUtil;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Channel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;

import java.nio.file.Path;
import java.util.*;

/**
 * Created by Hy on 1/4/2016.
 */
@Plugin(id = "com.nguyenquyhy.spongediscord", name = "Discord Bridge", version = "1.4.0")
public class SpongeDiscord {
    private DiscordAPI consoleClient = null;
    private final Map<UUID, DiscordAPI> discordClients = new HashMap<>();
    private DiscordAPI defaultDiscordClient = null;

    private final Set<UUID> unauthenticatedPlayers = new HashSet<>(100);

    @Inject
    private Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    private Config config;

    @Inject
    private Game game;

    private MessageProvider messageProvider = new MessageProvider();

    private IStorage storage;

    private static SpongeDiscord instance;

    @Listener
    public void onPreInitialization(GamePreInitializationEvent event) {
        instance = this;
        config = new Config();
        ConfigHandler.loadConfiguration(config);
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        CommandRegistry.register();
        LoginHandler.loginGlobalAccount();
    }

    @Listener
    public void onServerStop(GameStoppingServerEvent event) {
        if (defaultDiscordClient != null) {
            Channel channel = defaultDiscordClient.getChannelById(config.CHANNEL_ID);
            if (channel != null) {
                channel.sendMessage(config.MESSAGE_DISCORD_SERVER_DOWN, false);
            }
        }
    }

    public static SpongeDiscord getInstance() {
        return instance;
    }

    public Game getGame() {
        return game;
    }

    public Path getConfigDir() {
        return configDir;
    }

    public Config getConfig() {
        return config;
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

    public DiscordAPI getDefaultDiscordClient() {
        return defaultDiscordClient;
    }

    public void setDefaultDiscordClient(DiscordAPI defaultDiscordClient) {
        this.defaultDiscordClient = defaultDiscordClient;
    }

    public Map<UUID, DiscordAPI> getDiscordClients() {
        return discordClients;
    }

    public Set<UUID> getUnauthenticatedPlayers() {
        return unauthenticatedPlayers;
    }

    public void addClient(UUID player, DiscordAPI client) {
        if (player == null) {
            consoleClient = client;
        } else {
            discordClients.put(player, client);
        }
    }

    public void removeAndLogoutClient(UUID player) {
        if (player == null) {
            consoleClient.disconnect();
            consoleClient = null;
        } else {
            if (discordClients.containsKey(player)) {
                DiscordAPI client = discordClients.get(player);
                client.disconnect();
                discordClients.remove(player);
            }
        }
    }
}