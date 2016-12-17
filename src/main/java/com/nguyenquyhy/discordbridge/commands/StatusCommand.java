package com.nguyenquyhy.discordbridge.commands;

import com.nguyenquyhy.discordbridge.DiscordBridge;
import de.btobastian.javacord.ImplDiscordAPI;
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
        ImplDiscordAPI bot = (ImplDiscordAPI) mod.getBotClient();

        boolean isProfileReady = false;
        boolean isSocketOpen = false;
        if (bot != null) {
            isProfileReady = bot.getYourself() != null;
            try {
                isSocketOpen = bot.getSocketAdapter() != null && bot.getSocketAdapter().isReady().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        src.sendMessage(Text.of(TextColors.GREEN, "Bot account:"));
        src.sendMessage(Text.of("- Profile: " + (isProfileReady ? bot.getYourself().getName() : "Not available")));
        src.sendMessage(Text.of("- Websocket: " + (isSocketOpen ? "Open" : "Closed")));
        return CommandResult.success();
    }
}
