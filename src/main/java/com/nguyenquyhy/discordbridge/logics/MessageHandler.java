package com.nguyenquyhy.discordbridge.logics;

import com.nguyenquyhy.discordbridge.DiscordBridge;
import com.nguyenquyhy.discordbridge.models.ChannelConfig;
import com.nguyenquyhy.discordbridge.models.ChannelMinecraftConfigCore;
import com.nguyenquyhy.discordbridge.models.GlobalConfig;
import com.nguyenquyhy.discordbridge.utils.ChannelUtil;
import com.nguyenquyhy.discordbridge.utils.TextUtil;
import net.dv8tion.jda.core.entities.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

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
            if (message.getNonce() != null && message.getNonce().equals(ChannelUtil.SPECIAL_CHAR + ChannelUtil.BOT_RANDOM)) {
                return;
            }
            if (message.isPinned()) {
                return;
            }
            if (StringUtils.isNotBlank(channelConfig.discordId)
                    && channelConfig.minecraft != null
                    && message.getChannel() != null
                    && message.getChannel().getId().equals(channelConfig.discordId)) {

                // Role base configuration
                ChannelMinecraftConfigCore minecraftConfig = channelConfig.minecraft;
                if (channelConfig.minecraft.roles != null) {
                    Collection<Role> roles = message.getMember().getRoles();
                    for (String roleName : channelConfig.minecraft.roles.keySet()) {
                        if (roles.stream().anyMatch(r -> r.getName().equals(roleName))) {
                            ChannelMinecraftConfigCore roleConfig = channelConfig.minecraft.roles.get(roleName);
                            roleConfig.inherit(channelConfig.minecraft);
                            minecraftConfig = roleConfig;
                            break;
                        }
                    }
                }

                if (StringUtils.isNotBlank(minecraftConfig.chatTemplate)) {
                    Text messageText = TextUtil.formatForMinecraft(minecraftConfig, message);

                    // Format attachments
                    if (minecraftConfig.attachment != null
                            && StringUtils.isNotBlank(minecraftConfig.attachment.template)
                            && message.getAttachments() != null) {
                        for (Message.Attachment attachment : message.getAttachments()) {
                            String spacing = StringUtils.isBlank(message.getContent()) ? "" : " ";
                            Text.Builder builder = Text.builder()
                                    .append(TextSerializers.FORMATTING_CODE.deserialize(spacing + minecraftConfig.attachment.template));
                            if (minecraftConfig.attachment.allowLink) {
                                try {
                                    builder = builder.onClick(TextActions.openUrl(new URL(attachment.getUrl())));
                                } catch (MalformedURLException ignored) {

                                }
                            }
                            if (StringUtils.isNotBlank(minecraftConfig.attachment.hoverTemplate))
                                builder = builder.onHover(TextActions.showText(Text.of(minecraftConfig.attachment.hoverTemplate)));
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
}
