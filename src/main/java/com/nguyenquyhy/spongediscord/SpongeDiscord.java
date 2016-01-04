package com.nguyenquyhy.spongediscord;

import com.google.inject.Inject;
import com.nguyenquyhy.spongediscord.commands.LoginCommand;
import com.nguyenquyhy.spongediscord.commands.LogoutCommand;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Created by Hy on 1/4/2016.
 */
@Plugin(id = "com.nguyenquyhy.spongediscord", name = "Sponge Discord", version = "0.1.0")
public class SpongeDiscord {
    //@Inject
    //private Logger logger;

    public static final Text FLARDARIAN = Text.of(TextColors.DARK_AQUA, TextStyles.BOLD, TextStyles.ITALIC, "Flardarian");

    public static String consoleToken = null;
    public static String consoleUsername = null;
    private ConfigurationNode config = null;

    @Inject
    private static Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private File defaultConfig;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private ConfigurationLoader configManager;


    public static Logger getLogger() {
        return logger;
    }

    public File getDefaultConfig() {
        return defaultConfig;
    }

    public ConfigurationLoader getConfigManager() {
        return configManager;
    }

    @Listener
    public void onPreInitialization(GamePreInitializationEvent event) {
        Game game = Sponge.getGame();
        try {

            if (!getDefaultConfig().exists()) {

                getDefaultConfig().createNewFile();
                this.config = getConfigManager().load();
                this.config.getNode("Channel").setValue("");

                // This is a set of pathed config nodes, living in the DB 'folder' of the config file.
                // Splitting your config variables like this produces sections to the config file, making sure
                // all config variables relating to eachother can be grouped together.
                // Also useful for passing only parts of your config to other modules; see below.

                //this.config.getNode("DB", "Host").setValue("127.0.0.1");
                //this.config.getNode("DB", "Port").setValue(3306);
                //this.config.getNode("DB", "Username").setValue("SpongePlots");
                //this.config.getNode("DB", "Password").setValue("YouReallyShouldChangeMe");
                //this.config.getNode("DB", "Configured").setValue(0);

                getConfigManager().save(config);
                getLogger().info("[Sponge-Discord]: Created default configuration, ConfigDatabase will not run until you have edited this file!");
            }

            this.config = getConfigManager().load();

        } catch (IOException exception) {
            getLogger().error("[Sponge-Discord]: Couldn't create default configuration file!");
        }
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

        CommandSpec loginCommandSpec = CommandSpec.builder()
                //.permission("spongediscord")
                .description(Text.of("Discord in Minecraft"))
                .child(loginCmd, "login", "l")
                .build();

        game.getCommandManager().register(this, loginCommandSpec, "discord", "d");

        getLogger().info("[Sponge-Discord]: /discord command registered.");
    }

    @Listener
    public void onChat(MessageChannelEvent event) {
        Optional<Text> originalMessage = event.getOriginalMessage();
        if (originalMessage.isPresent()) {
            Optional<Player> player = event.getCause().first(Player.class);
            if (player.isPresent()) {
                event.getChannel().ifPresent(channel -> channel.send(Text.of(TextColors.BLUE, "Talkative ", TextColors.GOLD, TextStyles.BOLD, player.get().getName())));
            }
        }
    }
}