package com.nguyenquyhy.discordbridge.logics;

import com.nguyenquyhy.discordbridge.DiscordBridge;
import com.nguyenquyhy.discordbridge.models.ChannelConfig;
import com.nguyenquyhy.discordbridge.models.GlobalConfig;
import com.nguyenquyhy.discordbridge.utils.TextUtil;
import de.btobastian.javacord.entities.message.Message;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Hy on 8/6/2016.
 */
public class MessageHandler {
    /**
     * Forward Discord messages to Minecraft
     *
     * @param message
     */
    public static void discordMessageReceived(Message message) {
        DiscordBridge mod = DiscordBridge.getInstance();
        Logger logger = mod.getLogger();
        GlobalConfig config = mod.getConfig();

        String botId = mod.getBotClient().getYourself().getId();
        String content = TextUtil.formatDiscordMessage(message.getContent());
        for (ChannelConfig channelConfig : config.channels) {

            if (config.prefixBlacklist != null) {
                for (String prefix : config.prefixBlacklist) {
                    if (StringUtils.isNotBlank(prefix) && content.startsWith(prefix)) {
                        return;
                    }
                }
            }
            if (config.cancelAllMessagesFromBot && message.getAuthor().getId().equals(botId)) {
                return;
            }
            if (!config.cancelAllMessagesFromBot && content.contains(TextUtil.SPECIAL_CHAR)) {
                return;
            }

            if (StringUtils.isNotBlank(channelConfig.discordId)
                    && channelConfig.minecraft != null
                    && StringUtils.isNotBlank(channelConfig.minecraft.chatTemplate)
                    && message.getChannelReceiver().getId().equals(channelConfig.discordId)) {
                String author = message.getAuthor().getName();
                Text formattedMessage = TextUtil.formatUrl(String.format(channelConfig.minecraft.chatTemplate.replace("%a", author), content));
                // This case is used for default account
                logger.info(formattedMessage.toPlain());
                Sponge.getServer().getOnlinePlayers().forEach(p -> p.sendMessage(formattedMessage));
            }
        }
    }
}
