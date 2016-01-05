package com.nguyenquyhy.spongediscord;

import com.google.inject.Inject;
import com.nguyenquyhy.spongediscord.commands.LoginCommand;
import com.nguyenquyhy.spongediscord.commands.LogoutCommand;
import com.nguyenquyhy.spongediscord.commands.ReloadCommand;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
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
import sx.blah.discord.DiscordClient;
import sx.blah.discord.handle.obj.Channel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Hy on 1/4/2016.
 */
@Plugin(id = "spongediscord", name = "Sponge Discord", version = "0.1.0")
public class SpongeDiscord {
    public static String CHANNEL_ID = "";
    public static String JOIN_MESSAGE = "";

    private static DiscordClient consoleClient = null;
    private static final Map<UUID, DiscordClient> clients = new HashMap<UUID, DiscordClient>();

    @Inject
    private Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

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

        getLogger().info("[Sponge-Discord]: /discord command registered.");
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join event) {
        Optional<Player> player = event.getCause().first(Player.class);
        if (player.isPresent()) {

        }
    }

    @Listener
    public void onDisconnect(ClientConnectionEvent.Disconnect event) {
        Optional<Player> player = event.getCause().first(Player.class);
        if (player.isPresent()) {
            removeClient(player.get().getUniqueId());
        }
    }

    @Listener
    public void onChat(MessageChannelEvent.Chat event) throws IOException, ParseException {
        String plainString = event.getRawMessage().toPlain().trim();
        if (plainString != null && !plainString.isEmpty() && !plainString.startsWith("/")) {
            Optional<Player> player = event.getCause().first(Player.class);
            if (player.isPresent()) {
                UUID playerId = player.get().getUniqueId();
                if (clients.containsKey(playerId)) {
                    clients.get(playerId).sendMessage(plainString, CHANNEL_ID);
                }
            }
        }
    }

    public static SpongeDiscord getInstance() { return instance; }
    public Logger getLogger() {
        return logger;
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
                configNode = configLoader.load();
                configNode.getNode("Channel").setValue("");
                configNode.getNode("JoinMessage").setValue("I just joined the server as %s!");
                configLoader.save(configNode);
                getLogger().info("[Sponge-Discord]: Created default configuration, ConfigDatabase will not run until you have edited this file!");
            }
            configNode = configLoader.load();
            CHANNEL_ID = configNode.getNode("Channel").getString();
            JOIN_MESSAGE = configNode.getNode("JoinMessage").getString();
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().error("[Sponge-Discord]: Couldn't create default configuration file!");
        }
    }

    public static void addClient(UUID player, DiscordClient client) {
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