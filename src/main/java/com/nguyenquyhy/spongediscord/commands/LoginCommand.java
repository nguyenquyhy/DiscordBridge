package com.nguyenquyhy.spongediscord.commands;

import com.nguyenquyhy.spongediscord.SpongeDiscord;
import org.json.simple.parser.ParseException;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.CommandBlockSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.format.TextStyles;
import sx.blah.discord.DiscordClient;
import sx.blah.discord.handle.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by Hy on 1/4/2016.
 */
public class LoginCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String email = args.<String>getOne("email").get();
        String password = args.<String>getOne("password").get();

        // Sign in to Discord
        src.sendMessage(Text.of(TextColors.GRAY, "Logging in to Discord..."));
        DiscordClient client = new DiscordClient();

        SpongeDiscord.getInstance().prepareClient(client, src);

        try {
            client.login(email, password);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (client.getToken() != null) {
            src.sendMessage(Text.of("Getting user info..."));
            return CommandResult.success();
        }
        else {
            src.sendMessage(Text.of(TextColors.RED, "Invalid username and/or password!"));
            return CommandResult.empty();
        }
    }
}
