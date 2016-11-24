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
     * @param message
     */
    public static void discordMessageReceived(Message message) {
        DiscordBridge mod = DiscordBridge.getInstance();
        Logger logger = mod.getLogger();
        GlobalConfig config = mod.getConfig();

        String botid = mod.getBotClient().getYourself().getId();
        String content = TextUtil.formatDiscordEmoji(message.getContent());
        for (ChannelConfig channelConfig : config.channels) {
        	if(StringUtils.isNotBlank(config.prefixBlacklist)){
        		if(content.startsWith(config.prefixBlacklist)){
        			return;
        		}
        	} 
        	if(config.cancelMessageFromBot == true){
        		if(message.getAuthor().getId().equals(botid)){
        			return;
        		}
        	}   
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
