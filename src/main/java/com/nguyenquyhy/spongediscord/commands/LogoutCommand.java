package com.nguyenquyhy.spongediscord.commands;

import com.nguyenquyhy.spongediscord.SpongeDiscord;
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

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Hy on 1/4/2016.
 */
public class LogoutCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            Player player = (Player) src;
            UUID playerId = player.getUniqueId();
            try {
                SpongeDiscord.getInstance().getStorage().removeToken(playerId);
            } catch (IOException e) {
                e.printStackTrace();
                src.sendMessage(Text.of(TextColors.RED, "Cannot remove cached token!"));
            }
            SpongeDiscord.removeClient(playerId);
            src.sendMessage(Text.of(TextColors.YELLOW, "Logged out of Discord!"));
            return CommandResult.success();
        } else if (src instanceof ConsoleSource) {
            SpongeDiscord.removeClient(null);
            src.sendMessage(Text.of("Logged out of Discord!"));
            return CommandResult.success();
        } else if (src instanceof CommandBlockSource) {
            src.sendMessage(Text.of(TextColors.YELLOW, "Cannot log out from command blocks!"));
            return CommandResult.empty();
        }
        return CommandResult.empty();
    }
}
