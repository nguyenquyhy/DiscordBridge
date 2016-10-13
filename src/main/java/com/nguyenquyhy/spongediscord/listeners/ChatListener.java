package com.nguyenquyhy.spongediscord.listeners;

import com.nguyenquyhy.spongediscord.SpongeDiscord;
import com.nguyenquyhy.spongediscord.logics.Config;
import com.nguyenquyhy.spongediscord.logics.LoginHandler;
import com.nguyenquyhy.spongediscord.logics.MessageHandler;
import com.nguyenquyhy.spongediscord.utils.TextUtil;
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
    @Listener(order = Order.POST)
    public void onChat(MessageChannelEvent.Chat event) {
        SpongeDiscord mod = SpongeDiscord.getInstance();
        Config config = mod.getConfig();
        Logger logger = mod.getLogger();

        if (event.isCancelled() || event.isMessageCancelled()) return;
        if (StringUtils.isBlank(config.CHANNEL_ID)) return;

        String plainString = event.getRawMessage().toPlain().trim();
        if (StringUtils.isBlank(plainString) || plainString.startsWith("/")) return;

        Optional<Player> player = event.getCause().first(Player.class);
        if (player.isPresent()) {
            UUID playerId = player.get().getUniqueId();

            plainString = TextUtil.formatMinecraftMessage(plainString);

            if (mod.getDiscordClients().containsKey(playerId)) {
                Channel channel = mod.getDiscordClients().get(playerId).getChannelById(config.CHANNEL_ID);
                if (channel == null) {
                    LoginHandler.loginNormalAccount(player.get());
                }
                if (channel != null) {
                    channel.sendMessage(String.format(config.MESSAGE_DISCORD_TEMPLATE, plainString), false);
                } else {
                    logger.warn("Cannot re-login user account!");
                }
            } else if (mod.getDefaultDiscordClient() != null) {
                Channel channel = mod.getDefaultDiscordClient().getChannelById(config.CHANNEL_ID);
                if (channel == null) {
                    LoginHandler.loginGlobalAccount();
                }
                if (channel != null) {
                    channel.sendMessage(
                            String.format(config.MESSAGE_DISCORD_ANONYMOUS_TEMPLATE.replace("%a",
                                    MessageHandler.getNameInDiscord(player.get())), plainString),
                            false);
                } else {
                    logger.warn("Cannot re-login default account!");
                }
            }
        }
    }
}
