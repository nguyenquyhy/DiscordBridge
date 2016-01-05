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

/**
 * Created by Hy on 1/4/2016.
 */
public class LogoutCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            Player player = (Player) src;

            player.sendMessage(Text.of("Hello " + player.getUniqueId() + "!"));

            return CommandResult.success();
        } else if (src instanceof ConsoleSource) {
            src.sendMessage(Text.of(TextColors.YELLOW, "This Discord account will be used only for this console session!"));

            SpongeDiscord.consoleToken = null;
            SpongeDiscord.consoleUsername = null;

            return CommandResult.empty();
        } else if (src instanceof CommandBlockSource) {
            src.sendMessage(Text.of(TextColors.YELLOW, "Cannot log out from command blocks!"));
            return CommandResult.empty();
        }
        return CommandResult.empty();
    }
}
