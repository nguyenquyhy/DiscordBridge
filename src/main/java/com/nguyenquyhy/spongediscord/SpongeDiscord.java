package com.nguyenquyhy.spongediscord;

import com.google.inject.Inject;
import com.nguyenquyhy.spongediscord.commands.*;
import com.nguyenquyhy.spongediscord.database.*;
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
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IListener;
import sx.blah.discord.handle.impl.events.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.obj.Invite;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.util.MissingPermissionsException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Hy on 1/4/2016.
 */
@Plugin(id = "com.nguyenquyhy.spongediscord", name = "Sponge Discord", version = "1.2.0")
public class SpongeDiscord {
    public static String DEBUG = "";
    public static String CHANNEL_ID = "";
    public static String INVITE_CODE = "";
    public static String JOINED_MESSAGE = "";
    public static String LEFT_MESSAGE = "";
    public static String MESSAGE_DISCORD_TEMPLATE = "";
    public static String MESSAGE_DISCORD_ANONYMOUS_TEMPLATE = "";
    public static String MESSAGE_MINECRAFT_TEMPLATE = "";
    public static String MESSAGE_DISCORD_SERVER_UP = "";
    public static String MESSAGE_DISCORD_SERVER_DOWN = "";

    public static String NONCE = "sponge-discord";

    private static IDiscordClient consoleClient = null;
    private static final Map<UUID, IDiscordClient> clients = new HashMap<UUID, IDiscordClient>();
    private static IDiscordClient defaultClient = null;
    private static Set<UUID> unauthenticatedPlayers = new HashSet<>(100);

    @Inject
    private Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    @Inject
    private Game game;

    private MessageProvider messageProvider = new MessageProvider();

    private IStorage storage;

    private static SpongeDiscord instance;

    @Listener
    public void onPreInitialization(GamePreInitializationEvent event) {
        instance = this;
        loadConfiguration();
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        CommandSpec defaultLoginCmd = CommandSpec.builder()
                .description(Text.of("Log in and set a Discord account for unauthenticated users"))
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("email"))),
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("password"))))
                .executor(new DefaultLoginCommand())
                .build();

        CommandSpec defaultLogoutCmd = CommandSpec.builder()
                .description(Text.of("Log out of default Discord account"))
                .executor(new DefaultLogoutCommand())
                .build();

        CommandSpec defaultCmd = CommandSpec.builder()
                .permission("spongediscord.default")
                .description(Text.of("Commands to set/unset default Discord account for unauthenticated users"))
                .child(defaultLoginCmd, "login", "l")
                .child(defaultLogoutCmd, "logout", "lo")
                .build();

        CommandSpec loginCmd = CommandSpec.builder()
                //.permission("spongediscord.login")
                .description(Text.of("Login to your Discord account and bind to current Minecraft account"))
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("email"))),
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("password"))))
                .executor(new LoginCommand())
                .build();

        CommandSpec logoutCmd = CommandSpec.builder()
                //.permission("spongediscord.login")
                .description(Text.of("Logout of your Discord account and unbind from current Minecraft account"))
                .executor(new LogoutCommand())
                .build();

        CommandSpec reloadCmd = CommandSpec.builder()
                .permission("spongediscord.reload")
                .description(Text.of("Reload Sponge Discord configuration"))
                .executor(new ReloadCommand())
                .build();

        CommandSpec broadcastCmd = CommandSpec.builder()
                .permission("spongediscord.broadcast")
                .description(Text.of("Broadcast message to Discord and online Minecraft accounts"))
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("message"))))
                .executor(new BroadcastCommand())
                .build();

        CommandSpec mainCommandSpec = CommandSpec.builder()
                //.permission("spongediscord")
                .description(Text.of("Discord in Minecraft"))
                .child(defaultCmd, "default", "d")
                .child(loginCmd, "login", "l")
                .child(logoutCmd, "logout", "lo")
                .child(reloadCmd, "reload")
                .child(broadcastCmd, "broadcast", "b")
                .build();

        game.getCommandManager().register(this, mainCommandSpec, "discord", "d");

        getLogger().info("/discord command registered.");

        String cachedToken = getStorage().getDefaultToken();
        if (null != cachedToken && !cachedToken.isEmpty()) {
            getLogger().info("Logging in to default Discord account...");

            try {
                ClientBuilder clientBuilder = new ClientBuilder();
                IDiscordClient client = clientBuilder.withToken(cachedToken).build();
                prepareDefaultClient(client, null);
                client.login();
            } catch (DiscordException e) {
                e.printStackTrace();
            }
        }
    }

    @Listener
    public void onServerStop(GameStoppingServerEvent event) {
        if (defaultClient != null && defaultClient.isReady()) {
            IChannel channel = defaultClient.getChannelByID(CHANNEL_ID);
            if (channel != null) {
                try {
                    channel.sendMessage(MESSAGE_DISCORD_SERVER_DOWN, NONCE, false);
                } catch (HTTP429Exception e) {
                    e.printStackTrace();
                } catch (DiscordException e) {
                    e.printStackTrace();
                } catch (MissingPermissionsException e) {
                    e.printStackTrace();
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
                String cachedToken = getStorage().getToken(playerId);
                if (null != cachedToken && !cachedToken.isEmpty()) {
                    player.get().sendMessage(Text.of(TextColors.GRAY, "Logging in to Discord..."));

                    try {
                        ClientBuilder clientBuilder = new ClientBuilder();
                        IDiscordClient client = clientBuilder.withToken(cachedToken).build();
                        prepareClient(client, player.get());
                        client.login();
                        loggedIn = true;
                    } catch (DiscordException e) {
                        SpongeDiscord.getInstance().getLogger().error("Cannot login to Discord! " + e.getMessage());
                    }
                }
            }

            if (!loggedIn) {
                unauthenticatedPlayers.add(playerId);

                if (JOINED_MESSAGE != null && defaultClient != null && defaultClient.isReady()) {
                    try {
                        IChannel channel = defaultClient.getChannelByID(CHANNEL_ID);
                        channel.sendMessage(String.format(JOINED_MESSAGE, player.get().getName()), NONCE, false);
                    } catch (DiscordException e) {
                        e.printStackTrace();
                    } catch (HTTP429Exception e) {
                        e.printStackTrace();
                    } catch (MissingPermissionsException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Listener
    public void onDisconnect(ClientConnectionEvent.Disconnect event) throws IOException, HTTP429Exception, DiscordException, MissingPermissionsException {
        Optional<Player> player = event.getCause().first(Player.class);
        if (player.isPresent()) {
            UUID playerId = player.get().getUniqueId();
            if (CHANNEL_ID != null && !CHANNEL_ID.isEmpty() && LEFT_MESSAGE != null) {
                IDiscordClient client = clients.get(playerId);
                if (client != null && client.isReady()) {
                    IChannel channel = client.getChannelByID(CHANNEL_ID);
                    channel.sendMessage(String.format(LEFT_MESSAGE, player.get().getName()), NONCE, false);
                } else if (defaultClient != null && defaultClient.isReady()) {
                    IChannel channel = defaultClient.getChannelByID(CHANNEL_ID);
                    channel.sendMessage(String.format(LEFT_MESSAGE, player.get().getName()), NONCE, false);
                }
            }
            removeAndLogoutClient(playerId);
            unauthenticatedPlayers.remove(playerId);
            getLogger().info(player.get().getName() + " has disconnected!");
        }
    }

    @Listener
    public void onChat(MessageChannelEvent.Chat event) throws IOException, HTTP429Exception, DiscordException, MissingPermissionsException {
        if (CHANNEL_ID != null && !CHANNEL_ID.isEmpty()) {
            String plainString = event.getRawMessage().toPlain().trim();
            if (plainString != null && !plainString.isEmpty() && !plainString.startsWith("/")) {
                Optional<Player> player = event.getCause().first(Player.class);
                if (player.isPresent()) {
                    UUID playerId = player.get().getUniqueId();
                    if (clients.containsKey(playerId)) {
                        IChannel channel = clients.get(playerId).getChannelByID(CHANNEL_ID);
                        channel.sendMessage(String.format(MESSAGE_DISCORD_TEMPLATE, plainString), NONCE, false);
                    } else if (defaultClient != null) {
                        IChannel channel = defaultClient.getChannelByID(CHANNEL_ID);
                        channel.sendMessage(String.format(MESSAGE_DISCORD_ANONYMOUS_TEMPLATE.replace("%a", player.get().getName()), plainString), NONCE, false);
                    }
                }
            }
        }
    }

    public static SpongeDiscord getInstance() {
        return instance;
    }

    public Logger getLogger() {
        return logger;
    }

    public IStorage getStorage() {
        return storage;
    }

    public void loadConfiguration() {
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            Path configFile = Paths.get(configDir + "/config.conf");
            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(configFile).build();

            CommentedConfigurationNode configNode;

            if (!Files.exists(configFile)) {
                Files.createFile(configFile);
                getLogger().info("[Sponge-Discord]: Created default configuration, ConfigDatabase will not run until you have edited this file!");
            }
            configNode = configLoader.load();

            DEBUG = configNode.getNode("Debug").getString();
            CHANNEL_ID = ConfigUtil.readString(configNode, "Channel", "");
            INVITE_CODE = ConfigUtil.readString(configNode, "InviteCode", "");
            JOINED_MESSAGE = ConfigUtil.readString(configNode, "JoinedMessageTemplate", "_%s just joined the server_");
            LEFT_MESSAGE = ConfigUtil.readString(configNode, "LeftMessageTemplate", "_%s just left the server_");
            MESSAGE_DISCORD_TEMPLATE = ConfigUtil.readString(configNode, "MessageInDiscordTemplate", "%s");
            MESSAGE_DISCORD_ANONYMOUS_TEMPLATE = ConfigUtil.readString(configNode, "MessageInDiscordAnonymousTemplate", "_<%a>_ %s");
            MESSAGE_MINECRAFT_TEMPLATE = ConfigUtil.readString(configNode, "MessageInMinecraftTemplate", "&7<%a> &f%s");
            MESSAGE_DISCORD_SERVER_UP = ConfigUtil.readString(configNode, "MessageInDiscordServerUp", "Server has started.");
            MESSAGE_DISCORD_SERVER_DOWN = ConfigUtil.readString(configNode, "MessageInDiscordServerDown", "Server has stopped.");
            String tokenStore = ConfigUtil.readString(configNode, "TokenStore", "JSON");

            configLoader.save(configNode);

            // TODO: exit if channel is empty
            if (StringUtils.isBlank(CHANNEL_ID)) {
                getLogger().error("Channel ID is not set!");
            }

            switch (tokenStore) {
                case "InMemory":
                    storage = new InMemoryStorage();
                    getLogger().info("Use InMemory storage.");
                    break;
                case "JSON":
                    storage = new JsonFileStorage(configDir);
                    getLogger().info("Use JSON storage.");
                    break;
                default:
                    getLogger().warn("Invalid TokenStore config. JSON setting with be used!");
                    storage = new JsonFileStorage(configDir);
                    break;
            }
            getLogger().info("Configuration loaded. Channel " + CHANNEL_ID);
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().error("[Sponge-Discord]: Couldn't create default configuration file!");
        }
    }

    public void prepareClient(IDiscordClient client, CommandSource commandSource) {
        client.getDispatcher().registerListener(new IListener<ReadyEvent>() {
            @Override
            public void handle(ReadyEvent readyEvent) {
                try {
                    String name = client.getOurUser().getName();
                    commandSource.sendMessage(Text.of(TextColors.GOLD, TextStyles.BOLD, "You have logged in to Discord account " + name + "!"));
                    if (commandSource instanceof Player) {
                        Player player = (Player) commandSource;
                        UUID playerId = player.getUniqueId();
                        unauthenticatedPlayers.remove(playerId);
                        SpongeDiscord.addClient(playerId, client);
                        getStorage().putToken(playerId, client.getToken());
                    } else if (commandSource instanceof ConsoleSource) {
                        commandSource.sendMessage(Text.of("WARNING: This Discord account will be used only for this console session!"));
                        SpongeDiscord.addClient(null, client);
                    } else if (commandSource instanceof CommandBlockSource) {
                        commandSource.sendMessage(Text.of(TextColors.GREEN, "Account is valid!"));
                        return;
                    }

                    if (CHANNEL_ID != null && !CHANNEL_ID.isEmpty()) {
                        IChannel channel = client.getChannelByID(CHANNEL_ID);
                        if (channel == null) {
                            SpongeDiscord.getInstance().getLogger().info("Accepting channel invite");
                            acceptInvite(client);
                        } else {
                            channelJoined(client, channel, commandSource);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (DiscordException e) {
                    e.printStackTrace();
                } catch (HTTP429Exception e) {
                    e.printStackTrace();
                } catch (MissingPermissionsException e) {
                    e.printStackTrace();
                }
            }
        });

        client.getDispatcher().registerListener(new IListener<MessageReceivedEvent>() {
            @Override
            public void handle(MessageReceivedEvent event) {
                handleMessageReceivedEvent(event, commandSource);
            }
        });

        client.getDispatcher().registerListener(new IListener<GuildCreateEvent>() {
            @Override
            public void handle(GuildCreateEvent event) {
                handleGuildCreateEvent(event, client, commandSource);
            }
        });
    }

    public void prepareDefaultClient(IDiscordClient client, CommandSource commandSource) {
        client.getDispatcher().registerListener(new IListener<ReadyEvent>() {
            @Override
            public void handle(ReadyEvent readyEvent) {
                try {
                    String name = client.getOurUser().getName();
                    String text = "Discord account " + name + " will be used for all unauthenticated users!";
                    if (commandSource != null)
                        commandSource.sendMessage(Text.of(TextColors.GOLD, TextStyles.BOLD, text));
                    else
                        getLogger().info(text);

                    defaultClient = client;
                    getStorage().putDefaultToken(client.getToken());

                    if (CHANNEL_ID != null && !CHANNEL_ID.isEmpty()) {
                        IChannel channel = client.getChannelByID(CHANNEL_ID);
                        if (channel == null) {
                            getLogger().info("Accepting channel invite for default account...");
                            acceptInvite(client);
                        } else {
                            channelJoined(client, channel, commandSource);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (DiscordException e) {
                    e.printStackTrace();
                } catch (HTTP429Exception e) {
                    e.printStackTrace();
                } catch (MissingPermissionsException e) {
                    e.printStackTrace();
                }
            }
        });

        client.getDispatcher().registerListener(new IListener<MessageReceivedEvent>() {
            @Override
            public void handle(MessageReceivedEvent messageReceivedEvent) {
                handleMessageReceivedEvent(messageReceivedEvent, null);
            }
        });

        client.getDispatcher().registerListener(new IListener<GuildCreateEvent>() {
            @Override
            public void handle(GuildCreateEvent guildCreateEvent) {
                handleGuildCreateEvent(guildCreateEvent, client, commandSource);
            }
        });
    }

    public static CommandResult logoutDefault(CommandSource commandSource) throws IOException, HTTP429Exception, DiscordException {
        if (defaultClient != null) {
            defaultClient.logout();
            defaultClient = null;
            getInstance().getStorage().removeDefaultToken();

            if (commandSource != null) {
                commandSource.sendMessage(Text.of(TextColors.YELLOW, "Default Discord account is removed."));
            }
        }
        return CommandResult.success();
    }

    public static CommandResult login(CommandSource commandSource, String email, String password, boolean defaultAccount) {
        if (defaultAccount) {
            if (defaultClient != null) {
                try {
                    defaultClient.logout();
                    commandSource.sendMessage(Text.of("Logged out of current default account."));
                } catch (HTTP429Exception e) {
                    e.printStackTrace();
                } catch (DiscordException e) {
                    e.printStackTrace();
                }
            }
        } else {
            logout(commandSource, true);
        }

        IDiscordClient client = null;
        try {
            ClientBuilder clientBuilder = new ClientBuilder().withLogin(email, password);
            client = clientBuilder.build();
            if (defaultAccount)
                SpongeDiscord.getInstance().prepareDefaultClient(client, commandSource);
            else
                SpongeDiscord.getInstance().prepareClient(client, commandSource);

            client.login();
        } catch (DiscordException e) {
            e.printStackTrace();
        }

        if (client.getToken() != null) {
            return CommandResult.success();
        } else {
            commandSource.sendMessage(Text.of(TextColors.RED, "Invalid username and/or password!"));
            return CommandResult.empty();
        }
    }

    public static CommandResult logout(CommandSource commandSource, boolean isSilence) {
        if (commandSource instanceof Player) {
            Player player = (Player) commandSource;
            UUID playerId = player.getUniqueId();
            try {
                SpongeDiscord.getInstance().getStorage().removeToken(playerId);
            } catch (IOException e) {
                e.printStackTrace();
                commandSource.sendMessage(Text.of(TextColors.RED, "Cannot remove cached token!"));
            }
            SpongeDiscord.removeAndLogoutClient(playerId);
            unauthenticatedPlayers.add(player.getUniqueId());

            if (!isSilence)
                commandSource.sendMessage(Text.of(TextColors.YELLOW, "Logged out of Discord!"));
            return CommandResult.success();
        } else if (commandSource instanceof ConsoleSource) {
            SpongeDiscord.removeAndLogoutClient(null);
            commandSource.sendMessage(Text.of("Logged out of Discord!"));
            return CommandResult.success();
        } else if (commandSource instanceof CommandBlockSource) {
            commandSource.sendMessage(Text.of(TextColors.YELLOW, "Cannot log out from command blocks!"));
            return CommandResult.empty();
        }
        return CommandResult.empty();
    }

    public static CommandResult broadcast(CommandSource commandSource, String message) throws IOException, HTTP429Exception, DiscordException, MissingPermissionsException {
        if (CHANNEL_ID != null && !CHANNEL_ID.isEmpty()) {
            if (defaultClient == null) {
                commandSource.sendMessage(Text.of(TextColors.RED, "You have to set up a default account first!"));
                return CommandResult.empty();
            }
            IChannel channel = defaultClient.getChannelByID(CHANNEL_ID);
            channel.sendMessage(String.format(MESSAGE_DISCORD_TEMPLATE, "_<Broadcast>_ " + message), NONCE, false);
            Collection<Player> players = Sponge.getServer().getOnlinePlayers();
            for (Player player : players) {
                player.sendMessage(Text.of(TextStyles.ITALIC, "<Broadcast> " + message));
            }
        }
        return CommandResult.success();
    }

    private void handleMessageReceivedEvent(MessageReceivedEvent event, CommandSource commandSource) {
        IMessage message = event.getMessage();
        if (message.getChannel().getID().equals(CHANNEL_ID) && !NONCE.equals(message.getNonce())) {
            String content = message.getContent();
            String author = message.getAuthor().getName();
            Text formattedMessage = TextSerializers.FORMATTING_CODE.deserialize(String.format(MESSAGE_MINECRAFT_TEMPLATE.replace("%a", author), content));
            if (commandSource != null) {
                commandSource.sendMessage(formattedMessage);
            } else {
                // This case is used for default account
                for (UUID playerUUID : unauthenticatedPlayers) {
                    Optional<Player> player = Sponge.getServer().getPlayer(playerUUID);
                    if (player.isPresent()) {
                        player.get().sendMessage(formattedMessage);
                    }
                }
            }
        }
    }

    private IChannel acceptInvite(IDiscordClient client) {
        if (INVITE_CODE != null && !INVITE_CODE.isEmpty()) {
            Invite invite = new Invite(client, INVITE_CODE, null);
            try {
                invite.accept();
                return client.getChannelByID(CHANNEL_ID);
            } catch (Exception e) {
                getLogger().error("Cannot accept invitation " + INVITE_CODE);
                e.printStackTrace();
            }
        }
        return null;
    }

    private void handleGuildCreateEvent(GuildCreateEvent event, IDiscordClient client, CommandSource commandSource) {
        IChannel channel = event.getGuild().getChannelByID(CHANNEL_ID);
        try {
            channelJoined(client, channel, commandSource);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DiscordException e) {
            e.printStackTrace();
        } catch (HTTP429Exception e) {
            e.printStackTrace();
        } catch (MissingPermissionsException e) {
            e.printStackTrace();
        }
    }

    private void channelJoined(IDiscordClient client, IChannel channel, CommandSource src) throws IOException, HTTP429Exception, DiscordException, MissingPermissionsException {
        if (channel != null) {
            if (client != defaultClient) {
                String playerName = "console";
                if (src instanceof Player) {
                    Player player = (Player) src;
                    playerName = player.getName();
                }
                if (JOINED_MESSAGE != null)
                    channel.sendMessage(String.format(JOINED_MESSAGE, playerName), NONCE, false);
                getLogger().info(playerName + " connected to Discord channel.");
            } else {
                getLogger().info("Default account has connected to Discord channel.");
                if (StringUtils.isNotBlank(MESSAGE_DISCORD_SERVER_UP))
                    channel.sendMessage(MESSAGE_DISCORD_SERVER_UP, NONCE, false);
            }
        }
    }

    private static void addClient(UUID player, IDiscordClient client) {
        if (player == null) {
            consoleClient = client;
        } else {
            clients.put(player, client);
        }
    }

    private static void removeAndLogoutClient(UUID player) {
        if (player == null) {
            try {
                consoleClient.logout();
            } catch (HTTP429Exception e) {
                e.printStackTrace();
            } catch (DiscordException e) {
                e.printStackTrace();
            }
            consoleClient = null;
        } else {
            if (clients.containsKey(player)) {
                IDiscordClient client = clients.get(player);
                if (client.isReady()) {
                    try {
                        client.logout();
                    } catch (HTTP429Exception e) {
                        e.printStackTrace();
                    } catch (DiscordException e) {
                        e.printStackTrace();
                    }
                }
                clients.remove(player);
            }
        }
    }
}