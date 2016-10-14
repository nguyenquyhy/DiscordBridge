package com.nguyenquyhy.spongediscord.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

/**
 * Created by Hy on 10/15/2016.
 */
public class LoginCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        src.sendMessage(Text.of(TextColors.RED, TextStyles.BOLD, "WARNING!!"));
        src.sendMessage(Text.of(TextColors.RED, "You will need to provide Discord email & password to login."));
        src.sendMessage(Text.of(TextColors.RED, "This server might record those credential in its log!"));
        src.sendMessage(Text.of(TextColors.RED, "Proceed only if you completely trust the server owners/staffs with those information!"));
        src.sendMessage(Text.of(TextColors.GRAY, "To proceed, use: /discord loginconfirm <email> <password>"));
        return CommandResult.success();
    }
}
