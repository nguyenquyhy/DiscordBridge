package com.nguyenquyhy.spongediscord.commands;

import com.nguyenquyhy.spongediscord.SpongeDiscord;
import com.nguyenquyhy.spongediscord.logics.MessageHandler;
import com.nguyenquyhy.spongediscord.models.ChannelConfig;
import com.nguyenquyhy.spongediscord.models.GlobalConfig;
import com.nguyenquyhy.spongediscord.utils.TextUtil;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Channel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

/**
 * Created by Hy on 1/11/2016.
 */
public class BroadcastCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource commandSource, CommandContext commandContext) throws CommandException {
        String message = commandContext.<String>getOne("message").get();
        broadcast(commandSource, message);
        return CommandResult.empty();
    }

    private void broadcast(CommandSource commandSource, String message) {
        SpongeDiscord mod = SpongeDiscord.getInstance();
        GlobalConfig config = mod.getConfig();
        Logger logger = mod.getLogger();

        DiscordAPI defaultClient = mod.getBotClient();
        if (defaultClient == null) {
            commandSource.sendMessage(Text.of(TextColors.RED, "You have to set up a Bot token first!"));
            return;
        }

        message = TextUtil.formatMinecraftMessage(message);

        for (ChannelConfig channelConfig : config.channels) {
            if (StringUtils.isNotBlank(channelConfig.discordId)) {
                Channel channel = defaultClient.getChannelById(channelConfig.discordId);
                if (channel == null) {
                    logger.error("No active Discord connection is available!");
                    continue;
                }

                if (StringUtils.isNotBlank(channelConfig.discord.broadcastTemplate)) {
                    channel.sendMessage(String.format(channelConfig.discord.broadcastTemplate,
                            MessageHandler.formatForDiscord(message, channelConfig.discord.broadcastTemplate)), false);
                }

                if (StringUtils.isNotBlank(channelConfig.minecraft.broadcastTemplate)) {
                    for (Player player : Sponge.getServer().getOnlinePlayers()) {
                        player.sendMessage(Text.of(String.format(channelConfig.minecraft.broadcastTemplate, message)));
                    }
                }
            }
        }
    }
}
