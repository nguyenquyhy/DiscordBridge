package com.nguyenquyhy.discordbridge.utils;

import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Random;

/**
 * Created by Hy on 12/4/2016.
 */
public class ChannelUtil {
    public static final String SPECIAL_CHAR = "\u2062";
    public static final String BOT_RANDOM = String.valueOf(new Random().nextInt(100000));

    public static void sendMessage(TextChannel channel, String content) {
        channel.sendMessage(content).nonce(SPECIAL_CHAR + BOT_RANDOM).queue();
    }
}
