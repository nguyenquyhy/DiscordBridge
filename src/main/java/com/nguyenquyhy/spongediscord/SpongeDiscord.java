package com.nguyenquyhy.spongediscord;

import com.google.inject.Inject;
import com.nguyenquyhy.spongediscord.commands.LoginCommand;
import com.nguyenquyhy.spongediscord.commands.LogoutCommand;
import com.nguyenquyhy.spongediscord.commands.ReloadCommand;
import com.nguyenquyhy.spongediscord.database.InMemoryStorage;
import com.nguyenquyhy.spongediscord.database.JsonFileStorage;
import com.nguyenquyhy.spongediscord.database.IStorage;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
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
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import sx.blah.discord.DiscordClient;
import sx.blah.discord.handle.IListener;
import sx.blah.discord.handle.impl.events.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.Channel;
import sx.blah.discord.handle.obj.Invite;
import sx.blah.discord.handle.obj.Message;
import sx.blah.discord.util.HttpException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
    public static String CHANNEL_ID = "";
    public static String INVITE_CODE = "";
    public static String JOINED_MESSAGE = "";
    public static String LEFT_MESSAGE = "";
    public static String MESSAGE_DISCORD_PREFIX = "";
    public static String MESSAGE_MINECRAFT_PREFIX = "";

    private static DiscordClient consoleClient = null;
    private static final Map<UUID, DiscordClient> clients = new HashMap<UUID, DiscordClient>();

    @Inject
    private Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    private IStorage storage;

    private static SpongeDiscord instance;

    @Listener
    public void onPreInitialization(GamePreInitializationEvent event) {
        instance = this;

        // Create Config Directory for EssentialCmds
        loadConfiguration();
        Game game = Sponge.getGame();
    }

    @Listener
    public void onServerInit(GameInitializationEvent event) {
        Game game = Sponge.getGame();

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

        CommandSpec loginCommandSpec = CommandSpec.builder()
                //.permission("spongediscord")
                .description(Text.of("Discord in Minecraft"))
                .child(loginCmd, "login", "l")
                .child(logoutCmd, "logout", "lo")
                .child(reloadCmd, "reload")
                .build();

        game.getCommandManager().register(this, loginCommandSpec, "discord", "d");

        getLogger().info("/discord command registered.");
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join event) throws ParseException, URISyntaxException {
        Optional<Player> player = event.getCause().first(Player.class);
        if (player.isPresent()) {
            UUID playerId = player.get().getUniqueId();
            if (!clients.containsKey(playerId)) {
                String cachedToken = getStorage().getToken(playerId);
                if (null != cachedToken && !cachedToken.isEmpty()) {
                    player.get().sendMessage(Text.of(TextColors.GRAY, "Logging in to Discord..."));
                    DiscordClient client = new DiscordClient();
                    prepareClient(client, player.get());
                    client.login(cachedToken);
                }
            }
        }
    }

    @Listener
    public void onDisconnect(ClientConnectionEvent.Disconnect event) throws IOException, ParseException {
        Optional<Player> player = event.getCause().first(Player.class);
        if (player.isPresent()) {
            UUID playerId = player.get().getUniqueId();
            if (clients.containsKey(playerId)) {
                if (CHANNEL_ID != null && !CHANNEL_ID.isEmpty()) {
                    clients.get(playerId).sendMessage(String.format(LEFT_MESSAGE, player.get().getName()), CHANNEL_ID);
                }
                removeClient(playerId);
            }
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
                        clients.get(playerId).sendMessage(MESSAGE_DISCORD_PREFIX + plainString, CHANNEL_ID);
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
                configNode.getNode("JoinedMessage").setValue("_just joined the server as %s_");
                configNode.getNode("LeftMessage").setValue("_just left the server_");
                configNode.getNode("MessageInDiscordPrefix").setValue("");
                configNode.getNode("MessageInMinecraftPrefix").setValue("");
                configNode.getNode("TokenStore").setValue("JSON");
                configLoader.save(configNode);
                getLogger().info("[Sponge-Discord]: Created default configuration, ConfigDatabase will not run until you have edited this file!");
            }
            configNode = configLoader.load();
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

    public void prepareClient(DiscordClient client, CommandSource src) {
        client.getDispatcher().registerListener(new IListener<ReadyEvent>() {
            @Override
            public void receive(ReadyEvent readyEvent) {
                try {
                    String name = client.getOurUser().getName();
                    src.sendMessage(Text.of(TextColors.GOLD, TextStyles.BOLD, "You have logged in to Discord account " + name + "!"));
                    if (src instanceof Player) {
                        Player player = (Player) src;
                        SpongeDiscord.addClient(player.getUniqueId(), client);
                        SpongeDiscord.getInstance().getStorage().putToken(player.getUniqueId(), client.getToken());
                    } else if (src instanceof ConsoleSource) {
                        src.sendMessage(Text.of("WARNING: This Discord account will be used only for this console session!"));
                        SpongeDiscord.addClient(null, client);
                    } else if (src instanceof CommandBlockSource) {
                        src.sendMessage(Text.of(TextColors.GREEN, "Account is valid!"));
                        return;
                    }

                    if (CHANNEL_ID != null && !CHANNEL_ID.isEmpty()) {
                        Channel channel = client.getChannelByID(CHANNEL_ID);
                        if (channel == null) {
                            SpongeDiscord.getInstance().getLogger().info("Accepting channel invite");
                            if (INVITE_CODE != null && !INVITE_CODE.isEmpty()) {
                                Invite invite = new Invite(INVITE_CODE, client);
                                try {
                                    invite.accept();
                                    channel = client.getChannelByID(CHANNEL_ID);
                                } catch (HttpException e) {
                                    getLogger().error("Cannot accept invitation " + INVITE_CODE);
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            channelJoin(client, channel, src);
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
                Message message = event.getMessage();
                if (message.getChannel().getID().equals(CHANNEL_ID)) {
                    Text formattedMessage = Text.of(MESSAGE_MINECRAFT_PREFIX, TextColors.GRAY, "<", message.getAuthor().getName(), "> ", TextColors.WHITE, message.getContent());
                    src.sendMessage(formattedMessage);
                }
            }
        });

        client.getDispatcher().registerListener(new IListener<GuildCreateEvent>() {
            @Override
            public void receive(GuildCreateEvent event) {
                Channel channel = event.getGuild().getChannelByID(CHANNEL_ID);
                try {
                    channelJoin(client, channel, src);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void channelJoin(DiscordClient client, Channel channel, CommandSource src) throws IOException, ParseException {
        if (channel != null) {
            String playerName = "console";
            if (src instanceof Player) {
                Player player = (Player) src;
                playerName = player.getName();
            }
            client.sendMessage(String.format(JOINED_MESSAGE, playerName), CHANNEL_ID);
            getLogger().info(playerName + " connected to Discord channel.");
        }
    }

    private static void addClient(UUID player, DiscordClient client) {
        if (player == null) {
            consoleClient = client;
        } else {
            clients.put(player, client);
        }
    }

    public static void removeClient(UUID player) {
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