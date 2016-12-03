package com.nguyenquyhy.discordbridge.utils;

import de.btobastian.javacord.entities.Channel;

/**
 * Created by Hy on 12/4/2016.
 */
public class ChannelUtil {
    public static final String SPECIAL_CHAR = "\u2062";

    public static void sendMessage(Channel channel, String content) {
        channel.sendMessage(content, false, SPECIAL_CHAR);
    }
}
