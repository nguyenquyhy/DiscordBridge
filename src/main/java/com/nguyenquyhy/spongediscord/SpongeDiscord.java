package com.nguyenquyhy.spongediscord;

import com.google.inject.Inject;
import com.nguyenquyhy.spongediscord.commands.*;
import com.nguyenquyhy.spongediscord.database.*;
import com.nguyenquyhy.spongediscord.logics.Config;
import com.nguyenquyhy.spongediscord.logics.ConfigHandler;
import com.nguyenquyhy.spongediscord.logics.LoginHandler;
import com.nguyenquyhy.spongediscord.utils.ConfigUtil;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.source.CommandBlockSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.events.Event;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.MessageSendEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.obj.Invite;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Hy on 1/4/2016.
 */
@Plugin(id = "com.nguyenquyhy.spongediscord", name = "Discord Bridge", version = "1.3.0")
public class SpongeDiscord {
    private IDiscordClient consoleClient = null;
    private final Map<UUID, IDiscordClient> clients = new HashMap<UUID, IDiscordClient>();
    private IDiscordClient defaultClient = null;

    private Set<UUID> unauthenticatedPlayers = new HashSet<>(100);

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
        if (defaultClient != null && defaultClient.isReady()) {
            IChannel channel = defaultClient.getChannelByID(config.CHANNEL_ID);
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
    public void onJoin(ClientConnectionEvent.Join event) throws URISyntaxException {
        Optional<Player> player = event.getCause().first(Player.class);
        if (player.isPresent()) {
            UUID playerId = player.get().getUniqueId();
            boolean loggedIn = false;
            if (!clients.containsKey(playerId)) {
                loggedIn = LoginHandler.loginNormalAccount(player.get());
            }

            if (!loggedIn) {
                // User is not logged in => use global client
                unauthenticatedPlayers.add(playerId);

                if (StringUtils.isNotBlank(config.JOINED_MESSAGE) && defaultClient != null && defaultClient.isReady()) {
                    try {
                        IChannel channel = defaultClient.getChannelByID(config.CHANNEL_ID);
                        channel.sendMessage(String.format(config.JOINED_MESSAGE, getNameInDiscord(player.get())), false, config.NONCE);
                    } catch (DiscordException | MissingPermissionsException | RateLimitException e) {
                        logger.error(e.getLocalizedMessage(), e);
                    }
                }
            }
        }
    }

    @Listener
    public void onDisconnect(ClientConnectionEvent.Disconnect event) throws IOException, DiscordException, MissingPermissionsException, RateLimitException {
        Optional<Player> player = event.getCause().first(Player.class);
        if (player.isPresent()) {
            UUID playerId = player.get().getUniqueId();
            if (config.CHANNEL_ID != null && !config.CHANNEL_ID.isEmpty() && StringUtils.isNotBlank(config.LEFT_MESSAGE)) {
                IDiscordClient client = clients.get(playerId);
                if (client != null && client.isReady()) {
                    IChannel channel = client.getChannelByID(config.CHANNEL_ID);
                    channel.sendMessage(String.format(config.LEFT_MESSAGE, getNameInDiscord(player.get())), false, config.NONCE);
                } else if (defaultClient != null && defaultClient.isReady()) {
                    IChannel channel = defaultClient.getChannelByID(config.CHANNEL_ID);
                    channel.sendMessage(String.format(config.LEFT_MESSAGE, getNameInDiscord(player.get())), false, config.NONCE);
                }
            }
            removeAndLogoutClient(playerId);
            unauthenticatedPlayers.remove(playerId);
            getLogger().info(player.get().getName() + " has disconnected!");
        }
    }

    @Listener
    public void onChat(MessageChannelEvent.Chat event) throws IOException, DiscordException, MissingPermissionsException, RateLimitException {
        if (StringUtils.isNotBlank(config.CHANNEL_ID)) {
            String plainString = event.getRawMessage().toPlain().trim();
            if (StringUtils.isNotBlank(plainString) && !plainString.startsWith("/")) {
                Optional<Player> player = event.getCause().first(Player.class);
                if (player.isPresent()) {
                    UUID playerId = player.get().getUniqueId();
                    if (clients.containsKey(playerId)) {
                        IChannel channel = clients.get(playerId).getChannelByID(config.CHANNEL_ID);
                        channel.sendMessage(String.format(config.MESSAGE_DISCORD_TEMPLATE, plainString), false, config.NONCE);
                    } else if (defaultClient != null) {
                        IChannel channel = defaultClient.getChannelByID(config.CHANNEL_ID);
                        channel.sendMessage(
                                String.format(config.MESSAGE_DISCORD_ANONYMOUS_TEMPLATE.replace("%a", getNameInDiscord(player.get())), plainString),
                                false, config.NONCE);
                    }
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

    public IDiscordClient getDefaultClient() {
        return defaultClient;
    }

    public void setDefaultClient(IDiscordClient defaultClient) {
        this.defaultClient = defaultClient;
    }

    public Set<UUID> getUnauthenticatedPlayers() {
        return unauthenticatedPlayers;
    }

    public void addClient(UUID player, IDiscordClient client) {
        if (player == null) {
            consoleClient = client;
        } else {
            clients.put(player, client);
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
            if (clients.containsKey(player)) {
                IDiscordClient client = clients.get(player);
                if (client.isReady()) {
                    try {
                        client.logout();
                    } catch (DiscordException | RateLimitException e) {
                        e.printStackTrace();
                    }
                }
                clients.remove(player);
            }
        }
    }

    private static String getNameInDiscord(Player player) {
        return player.getName().replace("_", "\\_");
    }
}