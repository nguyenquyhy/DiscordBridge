package com.nguyenquyhy.spongediscord.logics;

import com.nguyenquyhy.spongediscord.SpongeDiscord;
import com.nguyenquyhy.spongediscord.models.ChannelConfig;
import com.nguyenquyhy.spongediscord.models.GlobalConfig;
import com.nguyenquyhy.spongediscord.utils.TextUtil;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Created by Hy on 8/6/2016.
 */
public class MessageHandler {
    /**
     * Forward Discord messages to Minecraft
     * @param message
     */
    public static void discordMessageReceived(Message message) {
        SpongeDiscord mod = SpongeDiscord.getInstance();
        Logger logger = mod.getLogger();
        GlobalConfig config = mod.getConfig();

        String content = TextUtil.formatDiscordEmoji(message.getContent());
        for (ChannelConfig channelConfig : config.channels) {
            if (StringUtils.isNotBlank(channelConfig.discordId)
                    && channelConfig.minecraft != null
                    && StringUtils.isNotBlank(channelConfig.minecraft.chatTemplate)
                    && message.getChannelReceiver().getId().equals(channelConfig.discordId)
                    && !content.contains(TextUtil.SPECIAL_CHAR) /* Not sending back message from this plugin */) {
                String author = message.getAuthor().getName();
                Text formattedMessage = TextUtil.formatUrl(String.format(channelConfig.minecraft.chatTemplate.replace("%a", author), content));
                // This case is used for default account
                logger.info(formattedMessage.toPlain());
                Sponge.getServer().getOnlinePlayers().forEach(p -> p.sendMessage(formattedMessage));
            }
        }
    }

    private static Map<String, Map<String, Boolean>> needReplacementMap = new HashMap<>();

    public static String formatForDiscord(String text, String template, String token) {
        if (!needReplacementMap.containsKey(token)) {
            needReplacementMap.put(token, new HashMap<>());
        }
        Map<String, Boolean> needReplacement = needReplacementMap.get(token);
        if (!needReplacement.containsKey(template)) {
            boolean need = !Pattern.matches(".*`.*" + token + ".*`.*", template)
                    && Pattern.matches(".*_.*" + token + ".*_.*", template);
            needReplacement.put(template, need);
        }
        if (needReplacement.get(template)) text = text.replace("_", "\\_");
        return text;
    }
}
