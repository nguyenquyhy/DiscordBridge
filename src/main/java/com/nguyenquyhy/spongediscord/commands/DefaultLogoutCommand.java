package com.nguyenquyhy.spongediscord.commands;

import com.nguyenquyhy.spongediscord.SpongeDiscord;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import sx.blah.discord.api.IDiscordClient;
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
            return logoutDefault(commandSource);
        } catch (IOException | DiscordException | RateLimitException e) {
            SpongeDiscord.getInstance().getLogger().error("Cannot log out! " + e.getLocalizedMessage());
        }
        return CommandResult.empty();
    }

    private static CommandResult logoutDefault(CommandSource commandSource) throws IOException, DiscordException, RateLimitException {
        SpongeDiscord mod = SpongeDiscord.getInstance();
        IDiscordClient defaultClient = mod.getDefaultClient();

        if (defaultClient != null) {
            defaultClient.logout();
            mod.setDefaultClient(null);
            mod.getStorage().removeDefaultToken();

            if (commandSource != null) {
                commandSource.sendMessage(Text.of(TextColors.YELLOW, "Default Discord account is removed."));
            }
        }
        return CommandResult.success();
    }
}
