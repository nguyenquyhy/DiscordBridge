package com.nguyenquyhy.discordbridge;

import com.nguyenquyhy.discordbridge.commands.*;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

/**
 * Created by Hy on 8/5/2016.
 */
public class CommandRegistry {
    /**
     * Register all commands
     */
    public static void register() {
        CommandSpec loginCmd = CommandSpec.builder()
                .permission("discordbridge.login")
                .description(Text.of("(ADMIN) Login to your Discord account and bind to current Minecraft account"))
                .executor(new LoginCommand())
                .build();

        CommandSpec loginConfirmCmd = CommandSpec.builder()
                .permission("discordbridge.login")
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("email"))),
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("password"))))
                .executor(new LoginConfirmCommand())
                .build();

        CommandSpec logoutCmd = CommandSpec.builder()
                .permission("discordbridge.login")
                .description(Text.of("Logout of your Discord account and unbind from current Minecraft account"))
                .executor(new LogoutCommand())
                .build();

        CommandSpec reloadCmd = CommandSpec.builder()
                .permission("discordbridge.reload")
                .description(Text.of("Reload Discord Bridge configuration"))
                .executor(new ReloadCommand())
                .build();

        CommandSpec broadcastCmd = CommandSpec.builder()
                .permission("discordbridge.broadcast")
                .description(Text.of("Broadcast message to Discord and online Minecraft accounts"))
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("message"))))
                .executor(new BroadcastCommand())
                .build();

        CommandSpec statusCmd = CommandSpec.builder()
                .permission("discordbridge.status")
                .description(Text.of("Get status of current connections to Discord"))
                .executor(new StatusCommand())
                .build();

        CommandSpec otpCmd = CommandSpec.builder()
                .permission("discordbridge.login")
                .description(Text.of("Additional authorization for 2FA-enabled user accounts"))
                .arguments(GenericArguments.onlyOne(GenericArguments.integer(Text.of("code"))))
                .executor(new OtpCommand())
                .build();

        CommandSpec mainCommandSpec = CommandSpec.builder()
                //.permission("discordbridge")
                .description(Text.of("Discord in Minecraft"))
                .child(loginCmd, "login", "l")
                .child(loginConfirmCmd, "loginconfirm", "lc")
                .child(logoutCmd, "logout", "lo")
                .child(reloadCmd, "reload")
                .child(broadcastCmd, "broadcast", "b", "bc")
                .child(statusCmd, "status", "s")
                .child(otpCmd, "otp", "o")
                .build();

        DiscordBridge mod = DiscordBridge.getInstance();
        mod.getGame().getCommandManager().register(mod, mainCommandSpec, "discord", "d");

        mod.getLogger().info("/discord command registered.");
    }
}
