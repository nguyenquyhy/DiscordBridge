package com.nguyenquyhy.spongediscord;

import com.google.inject.Inject;
import com.nguyenquyhy.spongediscord.database.IStorage;
import com.nguyenquyhy.spongediscord.logics.Config;
import com.nguyenquyhy.spongediscord.logics.ConfigHandler;
import com.nguyenquyhy.spongediscord.logics.LoginHandler;
import com.nguyenquyhy.spongediscord.utils.TextUtil;
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
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.nio.file.Path;
import java.util.*;

/**
 * Created by Hy on 1/4/2016.
 */
@Plugin(id = "com.nguyenquyhy.spongediscord", name = "Discord Bridge", version = "1.4.0")
public class SpongeDiscord {
    private IDiscordClient consoleClient = null;
    private final Map<UUID, IDiscordClient> discordClients = new HashMap<>();
    private IDiscordClient defaultDiscordClient = null;

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
        if (defaultDiscordClient != null && defaultDiscordClient.isReady()) {
            IChannel channel = defaultDiscordClient.getChannelByID(config.CHANNEL_ID);
            if (channel != null) {
                try {
                    channel.sendMessage(config.MESSAGE_DISCORD_SERVER_DOWN, false, config.NONCE);
                } catch (MissingPermissionsException | DiscordException | RateLimitException e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
            }
        }

    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join event) {
        Optional<Player> player = event.getCause().first(Player.class);
        if (player.isPresent()) {
            UUID playerId = player.get().getUniqueId();
            boolean loggedIn = false;
            if (!discordClients.containsKey(playerId)) {
                loggedIn = LoginHandler.loginNormalAccount(player.get());
            }

            if (!loggedIn) {
                // User is not logged in => use global client
                unauthenticatedPlayers.add(playerId);

                if (StringUtils.isNotBlank(config.JOINED_MESSAGE) && defaultDiscordClient != null && defaultDiscordClient.isReady()) {
                    try {
                        IChannel channel = defaultDiscordClient.getChannelByID(config.CHANNEL_ID);
                        channel.sendMessage(String.format(config.JOINED_MESSAGE, getNameInDiscord(player.get())), false, config.NONCE);
                    } catch (DiscordException | MissingPermissionsException | RateLimitException e) {
                        logger.error(e.getLocalizedMessage(), e);
                    }
                }
            }
        }
    }

    @Listener
    public void onDisconnect(ClientConnectionEvent.Disconnect event) {
        Optional<Player> player = event.getCause().first(Player.class);
        if (player.isPresent()) {
            UUID playerId = player.get().getUniqueId();
            if (config.CHANNEL_ID != null && !config.CHANNEL_ID.isEmpty() && StringUtils.isNotBlank(config.LEFT_MESSAGE)) {
                IDiscordClient client = discordClients.get(playerId);
                IChannel channel = null;
                if (client != null && client.isReady()) {
                    channel = client.getChannelByID(config.CHANNEL_ID);
                } else if (defaultDiscordClient != null && defaultDiscordClient.isReady()) {
                    channel = defaultDiscordClient.getChannelByID(config.CHANNEL_ID);
                }
                if (channel != null) {
                    try {
                        channel.sendMessage(String.format(config.LEFT_MESSAGE, getNameInDiscord(player.get())), false, config.NONCE);
                    } catch (MissingPermissionsException | RateLimitException | DiscordException e) {
                    }
                }
            }
            removeAndLogoutClient(playerId);
            unauthenticatedPlayers.remove(playerId);
            getLogger().info(player.get().getName() + " has disconnected!");
        }
    }

    @Listener(order = Order.POST)
    public void onChat(MessageChannelEvent.Chat event) {
        if (event.isCancelled() || event.isMessageCancelled()) return;
        if (StringUtils.isBlank(config.CHANNEL_ID)) return;

        String plainString = event.getRawMessage().toPlain().trim();
        if (StringUtils.isBlank(plainString) || plainString.startsWith("/")) return;

        Optional<Player> player = event.getCause().first(Player.class);
        if (player.isPresent()) {
            UUID playerId = player.get().getUniqueId();

            plainString = TextUtil.formatMinecraftEmoji(plainString);

            if (discordClients.containsKey(playerId)) {
                IChannel channel = discordClients.get(playerId).getChannelByID(config.CHANNEL_ID);
                if (channel == null) {
                    LoginHandler.loginNormalAccount(player.get());
                }
                if (channel != null) {
                    try {
                        channel.sendMessage(String.format(config.MESSAGE_DISCORD_TEMPLATE, plainString), false, config.NONCE);
                    } catch (MissingPermissionsException | RateLimitException | DiscordException e) {
                        logger.warn("Cannot send message! " + e.getLocalizedMessage());
                    }
                } else {
                    logger.warn("Cannot re-login user account!");
                }
            } else if (defaultDiscordClient != null) {
                IChannel channel = defaultDiscordClient.getChannelByID(config.CHANNEL_ID);
                if (channel == null) {
                    LoginHandler.loginGlobalAccount();
                }
                if (channel != null) {
                    try {
                        channel.sendMessage(
                                String.format(config.MESSAGE_DISCORD_ANONYMOUS_TEMPLATE.replace("%a", getNameInDiscord(player.get())), plainString),
                                false, config.NONCE);
                    } catch (MissingPermissionsException | RateLimitException | DiscordException e) {
                        logger.warn("Cannot send message! " + e.getLocalizedMessage());
                    }
                } else {
                    logger.warn("Cannot re-login default account!");
                }
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

    public IDiscordClient getDefaultDiscordClient() {
        return defaultDiscordClient;
    }

    public void setDefaultDiscordClient(IDiscordClient defaultDiscordClient) {
        this.defaultDiscordClient = defaultDiscordClient;
    }

    public Set<UUID> getUnauthenticatedPlayers() {
        return unauthenticatedPlayers;
    }

    public void addClient(UUID player, IDiscordClient client) {
        if (player == null) {
            consoleClient = client;
        } else {
            discordClients.put(player, client);
        }
    }

    public void removeAndLogoutClient(UUID player) {
        if (player == null) {
            try {
                consoleClient.logout();
            } catch (DiscordException | RateLimitException e) {
                e.printStackTrace();
            }
            consoleClient = null;
        } else {
            if (discordClients.containsKey(player)) {
                IDiscordClient client = discordClients.get(player);
                if (client.isReady()) {
                    try {
                        client.logout();
                    } catch (DiscordException | RateLimitException e) {
                        e.printStackTrace();
                    }
                }
                discordClients.remove(player);
            }
        }
    }

    private static String getNameInDiscord(Player player) {
        return player.getName().replace("_", "\\_");
    }
}