package com.nguyenquyhy.spongediscord.listeners;

import com.nguyenquyhy.spongediscord.SpongeDiscord;
import com.nguyenquyhy.spongediscord.logics.LoginHandler;
import com.nguyenquyhy.spongediscord.logics.MessageHandler;
import com.nguyenquyhy.spongediscord.models.ChannelConfig;
import com.nguyenquyhy.spongediscord.models.GlobalConfig;
import com.nguyenquyhy.spongediscord.utils.TextUtil;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Channel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.message.MessageChannelEvent;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by Hy on 10/13/2016.
 */
public class ChatListener {
    /**
     * Send chat from Minecraft to Discord
     * @param event
     */
    @Listener(order = Order.POST)
    public void onChat(MessageChannelEvent.Chat event) {
        SpongeDiscord mod = SpongeDiscord.getInstance();
        GlobalConfig config = mod.getConfig();
        Logger logger = mod.getLogger();

        if (event.isCancelled() || event.isMessageCancelled()) return;

        String plainString = event.getRawMessage().toPlain().trim();
        if (StringUtils.isBlank(plainString) || plainString.startsWith("/")) return;

        plainString = TextUtil.formatMinecraftMessage(plainString);
        Optional<Player> player = event.getCause().first(Player.class);

        if (player.isPresent()) {
            UUID playerId = player.get().getUniqueId();

            DiscordAPI client = mod.getBotClient();
            boolean isDefaultAccount = true;
            if (mod.getHumanClients().containsKey(playerId)) {
                client = mod.getHumanClients().get(playerId);
                isDefaultAccount = false;
            }

            if (client != null) {
                for (ChannelConfig channelConfig : config.channels) {
                    if (StringUtils.isNotBlank(channelConfig.discordId) && channelConfig.discord != null) {
                        Channel channel = client.getChannelById(channelConfig.discordId);

                        String template = isDefaultAccount ? channelConfig.discord.anonymousChatTemplate : channelConfig.discord.authenticatedChatTemplate;
                        if (StringUtils.isNotBlank(template)) {
                            if (isDefaultAccount) {
                                if (channel == null) {
                                    LoginHandler.loginBotAccount();
                                }
                                if (channel != null) {
                                    channel.sendMessage(
                                            String.format(
                                                    template.replace("%a",
                                                            MessageHandler.formatForDiscord(player.get().getName(), template)),
                                                    plainString),
                                            false);
                                } else {
                                    logger.warn("Cannot re-login default account!");
                                }
                            } else {
                                if (channel == null) {
                                    LoginHandler.loginNormalAccount(player.get());
                                }
                                if (channel != null) {
                                    channel.sendMessage(String.format(template, plainString), false);
                                } else {
                                    logger.warn("Cannot re-login user account!");
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}