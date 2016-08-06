package com.nguyenquyhy.spongediscord.commands;

import com.nguyenquyhy.spongediscord.SpongeDiscord;
import com.nguyenquyhy.spongediscord.logics.ConfigHandler;
import com.nguyenquyhy.spongediscord.logics.LoginHandler;
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
        ConfigHandler.loadConfiguration(SpongeDiscord.getInstance().getConfig());
        SpongeDiscord.getInstance().getLogger().info("Configuration Reloaded!");
        LoginHandler.loginGlobalAccount();
        return CommandResult.success();
    }
}
