package com.nguyenquyhy.discordbridge.commands;

import com.nguyenquyhy.discordbridge.logics.LoginHandler;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

public class OtpCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        return LoginHandler.otp(src, args.<Integer>getOne("code").get());
    }
}
