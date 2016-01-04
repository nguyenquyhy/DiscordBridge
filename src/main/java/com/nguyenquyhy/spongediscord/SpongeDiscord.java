package com.nguyenquyhy.spongediscord;

import com.nguyenquyhy.spongediscord.commands.LoginCommand;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import java.util.Optional;

/**
 * Created by Hy on 1/4/2016.
 */
@Plugin(id = "com.nguyenquyhy.spongediscord", name = "Sponge Discord", version = "0.1.0")
public class SpongeDiscord {
    //@Inject
    //private Logger logger;

    public static final Text FLARDARIAN = Text.of(TextColors.DARK_AQUA, TextStyles.BOLD, TextStyles.ITALIC, "Flardarian");

    @Listener
    public void onServerInit(GameInitializationEvent event) {
        Game game = Sponge.getGame();

        CommandSpec loginCmd = CommandSpec.builder()
                //.permission("spongediscord.login")
                .description(Text.of("Login to your Discord account"))
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("email"))),
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("password"))))
                .executor(new LoginCommand())
                .build();

        CommandSpec loginCommandSpec = CommandSpec.builder()
                //.permission("spongediscord")
                .description(Text.of("Discord in Minecraft"))
                .child(loginCmd, "login", "l")
                .build();

        game.getCommandManager().register(this, loginCommandSpec, "discord", "d");
    }

    @Listener
    public void onGameStarted(GameStartedServerEvent event) {

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