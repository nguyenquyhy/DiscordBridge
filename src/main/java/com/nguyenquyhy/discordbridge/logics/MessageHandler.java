package com.nguyenquyhy.discordbridge.logics;

import com.nguyenquyhy.discordbridge.DiscordBridge;
import com.nguyenquyhy.discordbridge.models.ChannelConfig;
import com.nguyenquyhy.discordbridge.models.GlobalConfig;
import com.nguyenquyhy.discordbridge.utils.ChannelUtil;
import com.nguyenquyhy.discordbridge.utils.ColorUtil;
import com.nguyenquyhy.discordbridge.utils.TextUtil;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageAttachment;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

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

        for (ChannelConfig channelConfig : config.channels) {
            if (config.prefixBlacklist != null) {
                for (String prefix : config.prefixBlacklist) {
                    if (StringUtils.isNotBlank(prefix) && message.getContent().startsWith(prefix)) {
                        return;
                    }
                }
            }
            if (config.ignoreBots && message.getAuthor().isBot()) {
                return;
            }
            if (message.getNonce() != null && message.getNonce().equals(ChannelUtil.SPECIAL_CHAR)) {
                return;
            }
            if (StringUtils.isNotBlank(channelConfig.discordId)
                    && channelConfig.minecraft != null
                    && StringUtils.isNotBlank(channelConfig.minecraft.chatTemplate)
                    && message.getChannelReceiver().getId().equals(channelConfig.discordId)) {
                Text messageText = TextUtil.formatUrl(TextUtil.formatForMinecraft(channelConfig, message));
                if (config.linkDiscordAttachments
                        && StringUtils.isNotBlank(channelConfig.minecraft.attachmentTemplate)
                        && message.getAttachments() != null) {
                    for (MessageAttachment attachment:message.getAttachments()) {
                        String spacing = message.getContent().equals("") ?  "" : " ";
                        messageText = Text.join(messageText,
                                Text.builder(spacing + channelConfig.minecraft.attachmentTemplate)
                                .color(ColorUtil.getTextColor(channelConfig.minecraft.attachmentColor))
                                .onClick(TextActions.openUrl(attachment.getUrl()))
                                .onHover(TextActions.showText(Text.of(channelConfig.minecraft.attachmentHoverTemplate)))
                                .build());
                    }
                }
                Text formattedMessage = messageText;
                // This case is used for default account
                logger.info(formattedMessage.toPlain());
                Sponge.getServer().getOnlinePlayers().forEach(p -> p.sendMessage(formattedMessage));
            }
        }
    }
}
