package com.nguyenquyhy.spongediscord.commands;

import com.nguyenquyhy.spongediscord.SpongeDiscord;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

import java.io.IOException;

/**
 * Created by Hy on 1/11/2016.
 */
public class DefaultLogoutCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource commandSource, CommandContext commandContext) throws CommandException {
        try {
            return SpongeDiscord.logoutDefault(commandSource);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DiscordException e) {
            e.printStackTrace();
        } catch (RateLimitException e) {
            e.printStackTrace();
        }
        return CommandResult.empty();
    }
}
