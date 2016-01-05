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
import sx.blah.discord.DiscordClient;
import sx.blah.discord.handle.obj.User;

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
        src.sendMessage(Text.of("Logging in to Discord..."));
        DiscordClient client = DiscordClient.get();
        try {
            client.login(email, password);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String token = client.getToken();

        if (token != null) {
            src.sendMessage(Text.of("Getting user info..."));
            User user = client.getOurUser();
            String name = user.getName();

            if (src instanceof Player) {
                Player player = (Player) src;

                player.sendMessage(Text.of("Hello " + name + "!"));

                return CommandResult.success();
            } else if (src instanceof ConsoleSource) {
                src.sendMessage(Text.of(TextColors.YELLOW, "This Discord account will be used only for this console session!"));

                SpongeDiscord.consoleToken = token;
                SpongeDiscord.consoleUsername = name;

                return CommandResult.empty();
            } else if (src instanceof CommandBlockSource) {
                src.sendMessage(Text.of(TextColors.GREEN, "Account is valid!"));
                return CommandResult.empty();
            }
        }
        else {
            src.sendMessage(Text.of(TextColors.RED, "Invalid username and/or password!"));
        }
        return CommandResult.empty();
    }
}
