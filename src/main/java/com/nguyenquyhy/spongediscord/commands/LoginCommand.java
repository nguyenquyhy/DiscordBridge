package com.nguyenquyhy.spongediscord.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.CommandBlockSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

/**
 * Created by Hy on 1/4/2016.
 */
public class LoginCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            Player player = (Player) src;
            String email = args.<String>getOne("email").get();
            String password = args.<String>getOne("password").get();
            player.sendMessage(Text.of("Hello " + player.getName() + "!\nYou have used " + email + " " + password));
        }
        else if(src instanceof ConsoleSource) {
            src.sendMessage(Text.of("Hello GLaDOS!"));
            // The Cake Is a Lie
        }
        else if(src instanceof CommandBlockSource) {
            src.sendMessage(Text.of("Hello Companion Cube!"));
            // <3
        }
        return CommandResult.success();
    }
}
