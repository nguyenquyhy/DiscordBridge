package com.nguyenquyhy.discordbridge.commands;

import com.nguyenquyhy.discordbridge.DiscordBridge;
import com.nguyenquyhy.discordbridge.logics.ConfigHandler;
import com.nguyenquyhy.discordbridge.logics.LoginHandler;
import com.nguyenquyhy.discordbridge.models.GlobalConfig;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by Hy on 1/5/2016.
 */
public class ReconnectCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource commandSource, CommandContext commandContext) throws CommandException {
        Logger logger = DiscordBridge.getInstance().getLogger();
        try {
            GlobalConfig config = ConfigHandler.loadConfiguration();
            DiscordBridge.getInstance().setConfig(config);

            LoginHandler.loginBotAccount();
            for (UUID uuid : DiscordBridge.getInstance().getHumanClients().keySet()) {
                Optional<Player> player = Sponge.getServer().getPlayer(uuid);
                player.ifPresent(LoginHandler::loginHumanAccount);
            }

            return CommandResult.success();
        } catch (Exception e) {
            logger.error("Cannot reload configuration!", e);
            return CommandResult.empty();
        }
    }
}
