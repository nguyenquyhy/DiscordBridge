package com.nguyenquyhy.spongediscord.commands;

import com.nguyenquyhy.spongediscord.SpongeDiscord;
import com.nguyenquyhy.spongediscord.logics.Config;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.io.IOException;
import java.util.Collection;

/**
 * Created by Hy on 1/11/2016.
 */
public class BroadcastCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource commandSource, CommandContext commandContext) throws CommandException {
        String message = commandContext.<String>getOne("message").get();
        try {
            return broadcast(commandSource, message);
        } catch (DiscordException | RateLimitException | MissingPermissionsException e) {
            SpongeDiscord.getInstance().getLogger().error("Cannot broadcast! " + e.getLocalizedMessage());
        }
        return CommandResult.empty();
    }

    private CommandResult broadcast(CommandSource commandSource, String message) throws DiscordException, MissingPermissionsException, RateLimitException {
        SpongeDiscord mod = SpongeDiscord.getInstance();
        Config config = mod.getConfig();
        IDiscordClient defaultClient = mod.getDefaultClient();

        if (config.CHANNEL_ID != null && !config.CHANNEL_ID.isEmpty()) {
            if (defaultClient == null) {
                commandSource.sendMessage(Text.of(TextColors.RED, "You have to set up a default account first!"));
                return CommandResult.empty();
            }
            IChannel channel = defaultClient.getChannelByID(config.CHANNEL_ID);
            if (channel == null) {
                SpongeDiscord.getInstance().getLogger().error("No active Discord connection is available!");
                return CommandResult.empty();
            }

            channel.sendMessage(String.format(config.MESSAGE_DISCORD_TEMPLATE, "_<Broadcast>_ " + message), false, config.NONCE);
            Collection<Player> players = Sponge.getServer().getOnlinePlayers();
            for (Player player : players) {
                player.sendMessage(Text.of(TextStyles.ITALIC, "<Broadcast> " + message));
            }
        }
        return CommandResult.success();
    }
}
