package com.nguyenquyhy.spongediscord.commands;

import com.nguyenquyhy.spongediscord.SpongeDiscord;
import com.nguyenquyhy.spongediscord.logics.ConfigHandler;
import com.nguyenquyhy.spongediscord.logics.LoginHandler;
import com.nguyenquyhy.spongediscord.models.GlobalConfig;
import org.slf4j.Logger;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

/**
 * Created by Hy on 1/5/2016.
 */
public class ReloadCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource commandSource, CommandContext commandContext) throws CommandException {
        Logger logger = SpongeDiscord.getInstance().getLogger();
        try {
            GlobalConfig config = ConfigHandler.loadConfiguration();
            SpongeDiscord.getInstance().setConfig(config);
            logger.info("Configuration reloaded!");
            LoginHandler.loginBotAccount();
            return CommandResult.success();
        } catch (Exception e) {
            logger.error("Cannot reload configuration!", e);
            return CommandResult.empty();
        }
    }
}
