package com.nguyenquyhy.spongediscord;

import com.google.inject.Inject;
import com.nguyenquyhy.spongediscord.commands.*;
import com.nguyenquyhy.spongediscord.database.*;
import com.nguyenquyhy.spongediscord.discord.DiscordClient;
import com.nguyenquyhy.spongediscord.discord.handle.IListener;
import com.nguyenquyhy.spongediscord.discord.handle.impl.events.GuildCreateEvent;
import com.nguyenquyhy.spongediscord.discord.handle.impl.events.MessageReceivedEvent;
import com.nguyenquyhy.spongediscord.discord.handle.impl.events.ReadyEvent;
import com.nguyenquyhy.spongediscord.discord.handle.obj.Channel;
import com.nguyenquyhy.spongediscord.discord.handle.obj.Invite;
import com.nguyenquyhy.spongediscord.discord.handle.obj.Message;
import com.nguyenquyhy.spongediscord.discord.util.HttpException;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.json.simple.parser.ParseException;
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
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Hy on 1/4/2016.
 */
@Plugin(id = "SpongeDiscord", name = "Sponge Discord", version = "1.0.0")
public class SpongeDiscord {
    public static String DEBUG = "";
    public static String CHANNEL_ID = "";
    public static String INVITE_CODE = "";
    public static String JOINED_MESSAGE = "";
    public static String LEFT_MESSAGE = "";
    public static String MESSAGE_DISCORD_PREFIX = "";
    public static String MESSAGE_MINECRAFT_PREFIX = "";

    public static String NONCE = "sponge-discord";

    private static DiscordClient consoleClient = null;
    private static final Map<UUID, DiscordClient> clients = new HashMap<UUID, DiscordClient>();
    private static DiscordClient defaultClient = null;
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

        // Create Config Directory for EssentialCmds
        loadConfiguration();
    }

    @Listener
    public void onServerInit(GameInitializationEvent event) {
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
                .description(Text.of("Reload Sponge Discord configuration"))
                .executor(new ReloadCommand())
                .build();

        CommandSpec broadcastCmd = CommandSpec.builder()
                //.permission("spongediscord.login")
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
            DiscordClient client = new DiscordClient();
            prepareDefaultClient(client, null);
            try {
                client.login(cachedToken);
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join event) throws ParseException, URISyntaxException {
        Optional<Player> player = event.getCause().first(Player.class);
        if (player.isPresent()) {
            UUID playerId = player.get().getUniqueId();
            boolean loggedIn = false;
            if (!clients.containsKey(playerId)) {
                String cachedToken = getStorage().getToken(playerId);
                if (null != cachedToken && !cachedToken.isEmpty()) {
                    player.get().sendMessage(Text.of(TextColors.GRAY, "Logging in to Discord..."));
                    DiscordClient client = new DiscordClient();
                    prepareClient(client, player.get());
                    client.login(cachedToken);
                    loggedIn = true;
                }
            }

            if (!loggedIn) {
                unauthenticatedPlayers.add(playerId);

                if (JOINED_MESSAGE != null && defaultClient != null && defaultClient.isReady()) {
                    try {
                        defaultClient.sendMessage(String.format(JOINED_MESSAGE, player.get().getName()), NONCE, CHANNEL_ID);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Listener
    public void onDisconnect(ClientConnectionEvent.Disconnect event) throws IOException, ParseException {
        Optional<Player> player = event.getCause().first(Player.class);
        if (player.isPresent()) {
            UUID playerId = player.get().getUniqueId();
            if (CHANNEL_ID != null && !CHANNEL_ID.isEmpty() && LEFT_MESSAGE != null) {
                DiscordClient client = clients.get(playerId);
                if (client != null && client.isReady()) {
                    client.sendMessage(String.format(LEFT_MESSAGE, player.get().getName()), NONCE, CHANNEL_ID);
                } else {
                    defaultClient.sendMessage(String.format(LEFT_MESSAGE, player.get().getName()), NONCE, CHANNEL_ID);
                }
            }
            removeAndLogoutClient(playerId);
            unauthenticatedPlayers.remove(playerId);
            getLogger().info(player.get().getName() + " has disconnected!");
        }
    }

    @Listener
    public void onChat(MessageChannelEvent.Chat event) throws IOException, ParseException {
        if (CHANNEL_ID != null && !CHANNEL_ID.isEmpty()) {
            String plainString = event.getRawMessage().toPlain().trim();
            if (plainString != null && !plainString.isEmpty() && !plainString.startsWith("/")) {
                Optional<Player> player = event.getCause().first(Player.class);
                if (player.isPresent()) {
                    UUID playerId = player.get().getUniqueId();
                    if (clients.containsKey(playerId)) {
                        clients.get(playerId).sendMessage(MESSAGE_DISCORD_PREFIX + plainString, NONCE, CHANNEL_ID);
                    } else if (defaultClient != null) {
                        defaultClient.sendMessage(MESSAGE_DISCORD_PREFIX + "_<" + player.get().getName() + ">_ " + plainString, NONCE, CHANNEL_ID);
                    }
                }
            }
        }
    }

    public static SpongeDiscord getInstance() { return instance; }
    public Logger getLogger() {
        return logger;
    }
    public IStorage getStorage() { return storage; }

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
                configNode = configLoader.load();
                configNode.getNode("Channel").setValue("");
                configNode.getNode("InviteCode").setValue("");
                configNode.getNode("JoinedMessage").setValue("_%s just joined the server_");
                configNode.getNode("LeftMessage").setValue("_%s just left the server_");
                configNode.getNode("MessageInDiscordPrefix").setValue("");
                configNode.getNode("MessageInMinecraftPrefix").setValue("");
                configNode.getNode("TokenStore").setValue("JSON");
                configLoader.save(configNode);
                getLogger().info("[Sponge-Discord]: Created default configuration, ConfigDatabase will not run until you have edited this file!");
            }
            configNode = configLoader.load();
            DEBUG = configNode.getNode("Debug").getString();
            CHANNEL_ID = configNode.getNode("Channel").getString();
            INVITE_CODE = configNode.getNode("InviteCode").getString();
            JOINED_MESSAGE = configNode.getNode("JoinedMessage").getString();
            LEFT_MESSAGE = configNode.getNode("LeftMessage").getString();
            MESSAGE_DISCORD_PREFIX = configNode.getNode("MessageInDiscordPrefix").getString();
            if (MESSAGE_DISCORD_PREFIX == null) MESSAGE_DISCORD_PREFIX = "";
            MESSAGE_MINECRAFT_PREFIX = configNode.getNode("MessageInMinecraftPrefix").getString();
            if (MESSAGE_MINECRAFT_PREFIX == null) MESSAGE_MINECRAFT_PREFIX = "";
            switch (configNode.getNode("TokenStore").getString()) {
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

    public void prepareClient(DiscordClient client, CommandSource commandSource) {
        client.getDispatcher().registerListener(new IListener<ReadyEvent>() {
            @Override
            public void receive(ReadyEvent readyEvent) {
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
                        Channel channel = client.getChannelByID(CHANNEL_ID);
                        if (channel == null) {
                            SpongeDiscord.getInstance().getLogger().info("Accepting channel invite");
                            acceptInvite(client);
                        } else {
                            channelJoined(client, channel, commandSource);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        client.getDispatcher().registerListener(new IListener<MessageReceivedEvent>() {
            @Override
            public void receive(MessageReceivedEvent event) {
                handleMessageReceivedEvent(event, commandSource);
            }
        });

        client.getDispatcher().registerListener(new IListener<GuildCreateEvent>() {
            @Override
            public void receive(GuildCreateEvent event) {
                handleGuildCreateEvent(event, client, commandSource);
            }
        });
    }

    public void prepareDefaultClient(DiscordClient client, CommandSource commandSource) {
        client.getDispatcher().registerListener(new IListener<ReadyEvent>() {
            @Override
            public void receive(ReadyEvent readyEvent) {
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
                        Channel channel = client.getChannelByID(CHANNEL_ID);
                        if (channel == null) {
                            SpongeDiscord.getInstance().getLogger().info("Accepting channel invite for default account");
                            acceptInvite(client);
                        } else {
                            channelJoined(client, channel, commandSource);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        client.getDispatcher().registerListener(new IListener<MessageReceivedEvent>() {
            @Override
            public void receive(MessageReceivedEvent event) {
                handleMessageReceivedEvent(event, null);
            }
        });

        client.getDispatcher().registerListener(new IListener<GuildCreateEvent>() {
            @Override
            public void receive(GuildCreateEvent event) {
                handleGuildCreateEvent(event, client, commandSource);
            }
        });
    }

    public static CommandResult logoutDefault(CommandSource commandSource) throws IOException {
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

    public static CommandResult login(DiscordClient client, CommandSource commandSource, String email, String password, boolean defaultAccount) {
        if (defaultAccount) {
            if (defaultClient != null) {
                defaultClient.logout();
                commandSource.sendMessage(Text.of("Logged out of current default account."));
            }
        } else {
            logout(commandSource);
        }

        try {
            client.login(email, password);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (HttpException e) {
            commandSource.sendMessage(Text.of("Cannot login! " + e.getMessage()));
        }

        if (client.getToken() != null) {
            return CommandResult.success();
        }
        else {
            commandSource.sendMessage(Text.of(TextColors.RED, "Invalid username and/or password!"));
            return CommandResult.empty();
        }
    }

    public static CommandResult logout(CommandSource commandSource) {
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

    public static CommandResult broadcast(CommandSource commandSource, String message) throws IOException, ParseException {
        if (CHANNEL_ID != null && !CHANNEL_ID.isEmpty()) {
            if (defaultClient == null) {
                commandSource.sendMessage(Text.of(TextColors.RED, "You have to set up a default account first!"));
                return CommandResult.empty();
            }
            defaultClient.sendMessage(MESSAGE_DISCORD_PREFIX + "_<Broadcast>_ " + message, NONCE, CHANNEL_ID);
            Collection<Player> players = Sponge.getServer().getOnlinePlayers();
            for (Player player : players) {
                player.sendMessage(Text.of(TextStyles.ITALIC, "<Broadcast> " + message));
            }
        }
        return CommandResult.success();
    }

    private void handleMessageReceivedEvent(MessageReceivedEvent event, CommandSource commandSource) {
        Message message = event.getMessage();
        if (message.getChannel().getID().equals(CHANNEL_ID) && !NONCE.equals(message.getNonce())) {
            String content = message.getContent();
            String author = message.getAuthor().getName();
            Text formattedMessage = Text.of(MESSAGE_MINECRAFT_PREFIX, TextColors.GRAY, "<" + author + "> ", TextColors.WHITE, content);
            if (commandSource != null) {
                commandSource.sendMessage(formattedMessage);
            } else {
                // This case is used for default account
                for (UUID playerUUID : unauthenticatedPlayers) {
                    Optional<Player> player = Sponge.getServer().getPlayer(playerUUID);
                    if(player.isPresent()) {
                        player.get().sendMessage(formattedMessage);
                    }
                }
            }
        }
    }

    private Channel acceptInvite(DiscordClient client) {
        if (INVITE_CODE != null && !INVITE_CODE.isEmpty()) {
            Invite invite = new Invite(INVITE_CODE, client);
            try {
                invite.accept();
                return client.getChannelByID(CHANNEL_ID);
            } catch (HttpException e) {
                getLogger().error("Cannot accept invitation " + INVITE_CODE);
                e.printStackTrace();
            }
        }
        return null;
    }

    private void handleGuildCreateEvent(GuildCreateEvent event, DiscordClient client, CommandSource commandSource) {
        Channel channel = event.getGuild().getChannelByID(CHANNEL_ID);
        try {
            channelJoined(client, channel, commandSource);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void channelJoined(DiscordClient client, Channel channel, CommandSource src) throws IOException, ParseException {
        if (channel != null) {
            if (client != defaultClient) {
                String playerName = "console";
                if (src instanceof Player) {
                    Player player = (Player) src;
                    playerName = player.getName();
                }
                if (JOINED_MESSAGE != null)
                    client.sendMessage(String.format(JOINED_MESSAGE, playerName), NONCE, CHANNEL_ID);
                getLogger().info(playerName + " connected to Discord channel.");
            } else {
                getLogger().info("Default account has connected to Discord channel.");
            }
        }
    }

    private static void addClient(UUID player, DiscordClient client) {
        if (player == null) {
            consoleClient = client;
        } else {
            clients.put(player, client);
        }
    }

    private static void removeAndLogoutClient(UUID player) {
        if (player == null) {
            consoleClient.logout();
            consoleClient = null;
        } else {
            if (clients.containsKey(player)) {
                DiscordClient client = clients.get(player);
                if (client.isReady()) {
                    client.logout();
                }
                clients.remove(player);
            }
        }
    }
}