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
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.serializer.TextSerializers;

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
                    && message.getChannelReceiver() != null
                    && message.getChannelReceiver().getId().equals(channelConfig.discordId)) {
                Text messageText = TextUtil.formatUrl(TextUtil.formatForMinecraft(channelConfig, message));

                // Format attachments
                if (channelConfig.minecraft.attachment != null
                        && StringUtils.isNotBlank(channelConfig.minecraft.attachment.template)
                        && message.getAttachments() != null) {
                    for (MessageAttachment attachment : message.getAttachments()) {
                        String spacing = message.getContent().equals("") ? "" : " ";
                        Text.Builder builder = Text.builder()
                                .append(TextSerializers.FORMATTING_CODE.deserialize(spacing + channelConfig.minecraft.attachment.template));
                        if (channelConfig.minecraft.attachment.allowLink)
                            builder = builder.onClick(TextActions.openUrl(attachment.getUrl()));
                        if (StringUtils.isNotBlank(channelConfig.minecraft.attachment.hoverTemplate))
                            builder = builder.onHover(TextActions.showText(Text.of(channelConfig.minecraft.attachment.hoverTemplate)));
                        messageText = Text.join(messageText, builder.build());
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
