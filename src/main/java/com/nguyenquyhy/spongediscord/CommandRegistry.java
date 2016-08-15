package com.nguyenquyhy.spongediscord;

import com.nguyenquyhy.spongediscord.commands.*;
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
        CommandSpec defaultLoginCmd = CommandSpec.builder()
                .description(Text.of("Log in and set a Discord account for unauthenticated users"))
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("email"))),
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("password"))))
                .executor(new DefaultLoginCommand())
                .build();

        CommandSpec defaultLogoutCmd = CommandSpec.builder()
                .description(Text.of("Log out of default Discord account"))
                .executor(new DefaultLogoutCommand())
                .build();

        CommandSpec defaultCmd = CommandSpec.builder()
                .permission("spongediscord.default")
                .description(Text.of("Commands to set/unset default Discord account for unauthenticated users"))
                .child(defaultLoginCmd, "login", "l")
                .child(defaultLogoutCmd, "logout", "lo")
                .build();

        CommandSpec loginCmd = CommandSpec.builder()
                //.permission("spongediscord.login")
                .description(Text.of("Login to your Discord account and bind to current Minecraft account"))
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("email"))),
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("password"))))
                .executor(new LoginCommand())
                .build();

        CommandSpec logoutCmd = CommandSpec.builder()
                //.permission("spongediscord.login")
                .description(Text.of("Logout of your Discord account and unbind from current Minecraft account"))
                .executor(new LogoutCommand())
                .build();

        CommandSpec reloadCmd = CommandSpec.builder()
                .permission("spongediscord.reload")
                .description(Text.of("Reload Discord Bridge configuration"))
                .executor(new ReloadCommand())
                .build();

        CommandSpec broadcastCmd = CommandSpec.builder()
                .permission("spongediscord.broadcast")
                .description(Text.of("Broadcast message to Discord and online Minecraft accounts"))
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("message"))))
                .executor(new BroadcastCommand())
                .build();

        CommandSpec mainCommandSpec = CommandSpec.builder()
                //.permission("spongediscord")
                .description(Text.of("Discord in Minecraft"))
                .child(defaultCmd, "default", "d")
                .child(loginCmd, "login", "l")
                .child(logoutCmd, "logout", "lo")
                .child(reloadCmd, "reload")
                .child(broadcastCmd, "broadcast", "b", "bc")
                .build();

        SpongeDiscord mod = SpongeDiscord.getInstance();
        mod.getGame().getCommandManager().register(mod, mainCommandSpec, "discord", "d");

        mod.getLogger().info("/discord command registered.");
    }
}
