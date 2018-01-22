package com.nguyenquyhy.discordbridge.commands;

import com.nguyenquyhy.discordbridge.DiscordBridge;
import net.dv8tion.jda.core.JDA;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.concurrent.ExecutionException;

/**
 * Created by Hy on 10/15/2016.
 */
public class StatusCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        DiscordBridge mod = DiscordBridge.getInstance();
        JDA bot = mod.getBotClient();

        boolean isProfileReady = false;
        if (bot != null) {
            isProfileReady = bot.getSelfUser() != null;
        }

        src.sendMessage(Text.of(TextColors.GREEN, "Bot account:"));
        src.sendMessage(Text.of("- Profile: " + (isProfileReady ? bot.getSelfUser().getName() : "Not available")));
        src.sendMessage(Text.of("- Status: " + (bot == null ? "N/A" : bot.getStatus().toString())));
        return CommandResult.success();
    }
}
