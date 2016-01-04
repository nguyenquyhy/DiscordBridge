package com.nguyenquyhy.spongediscord;

import com.google.inject.Inject;
import com.nguyenquyhy.spongediscord.commands.LoginCommand;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
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

        CommandSpec loginCommandSpec = CommandSpec.builder()
                .description(Text.of("Login to Discord"))
                .permission("spongediscord.command.login")
                .executor(new LoginCommand())
                .build();

        game.getCommandManager().register(this, loginCommandSpec, "discordlogin", "dlogin");
    }

    @Listener
    public void onGameStarted(GameStartedServerEvent event) {

    }

    @Listener
    public void onChat(MessageChannelEvent event) {
        Optional<Text> originalMessage = event.getOriginalMessage();
        if (originalMessage.isPresent()) {
            Optional<Player> player = event.getCause().first(Player.class);
            event.getChannel().ifPresent(channel -> channel.send(Text.of(TextColors.BLUE, "Talkative ", TextColors.GOLD, TextStyles.BOLD, player.get().getName())));
            if (player.isPresent()) {
                //player.get().sendMessage(Text.of(TextColors.RED, "Talkative ", TextColors.GOLD, TextStyles.BOLD, player.get().getName()));
                //System.out.println(event.getRawMessage().toPlain());
                //System.out.println(message.get().toPlain());
            }
        }
    }
}