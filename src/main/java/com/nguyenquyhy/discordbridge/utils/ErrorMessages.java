package com.nguyenquyhy.discordbridge.utils;

import com.nguyenquyhy.discordbridge.DiscordBridge;
import org.slf4j.Logger;

/**
 * Created by Hy on 11/7/2016.
 */
public enum ErrorMessages {
    CHANNEL_NOT_FOUND,
    CHANNEL_NOT_FOUND_HUMAN,
    BOT_TOKEN_NOT_FOUND;


    @SuppressWarnings("incomplete-switch")
	public void log(String... params) {
        Logger logger = DiscordBridge.getInstance().getLogger();
        switch (this) {
            case CHANNEL_NOT_FOUND:
                logger.error("Channel ID " + params[0] + " cannot be found! Please make sure the channel ID is correct and the bot has read & write permission for the channel.");
                return;
            case CHANNEL_NOT_FOUND_HUMAN:
                logger.error("Channel ID " + params[0] + " cannot be found! Please make sure the channel ID is correct and the user has read & write permission for the channel.");
                return;
        }
    }
}
