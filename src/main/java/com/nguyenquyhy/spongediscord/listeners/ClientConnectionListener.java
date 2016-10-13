package com.nguyenquyhy.spongediscord.listeners;

import com.nguyenquyhy.spongediscord.SpongeDiscord;
import com.nguyenquyhy.spongediscord.logics.LoginHandler;
import com.nguyenquyhy.spongediscord.logics.MessageHandler;
import com.nguyenquyhy.spongediscord.models.ChannelConfig;
import com.nguyenquyhy.spongediscord.models.GlobalConfig;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Channel;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by Hy on 10/13/2016.
 */
public class ClientConnectionListener {
    @Listener
    public void onJoin(ClientConnectionEvent.Join event) {
        SpongeDiscord mod = SpongeDiscord.getInstance();
        GlobalConfig config = mod.getConfig();

        Optional<Player> player = event.getCause().first(Player.class);
        if (player.isPresent()) {
            UUID playerId = player.get().getUniqueId();
            boolean loggedIn = false;
            if (!mod.getHumanClients().containsKey(playerId)) {
                loggedIn = LoginHandler.loginNormalAccount(player.get());
            }

            if (!loggedIn && mod.getBotClient() != null) {
                // User is not logged in => use global client
                //unauthenticatedPlayers.add(playerId);

                for (ChannelConfig channelConfig : config.channels) {
                    if (StringUtils.isNotBlank(channelConfig.discordId)
                            && channelConfig.discord != null
                            && StringUtils.isNotBlank(channelConfig.discord.joinedTemplate)) {
                        Channel channel = mod.getBotClient().getChannelById(channelConfig.discordId);
                        channel.sendMessage(String.format(channelConfig.discord.joinedTemplate,
                                MessageHandler.getNameInDiscord(player.get(), channelConfig.discord.joinedTemplate)), false);
                    }
                }
            }
        }
    }

    @Listener
    public void onDisconnect(ClientConnectionEvent.Disconnect event) {
        SpongeDiscord mod = SpongeDiscord.getInstance();
        GlobalConfig config = mod.getConfig();

        Optional<Player> player = event.getCause().first(Player.class);
        if (player.isPresent()) {
            UUID playerId = player.get().getUniqueId();

            DiscordAPI client = mod.getHumanClients().get(playerId);
            if (client == null) client = mod.getBotClient();
            if (client != null) {
                for (ChannelConfig channelConfig : config.channels) {
                    if (StringUtils.isNotBlank(channelConfig.discordId)
                            && channelConfig.discord != null
                            && StringUtils.isNotBlank(channelConfig.discord.leftTemplate)) {
                        Channel channel = client.getChannelById(channelConfig.discordId);
                        if (channel != null) {
                            channel.sendMessage(String.format(channelConfig.discord.leftTemplate,
                                    MessageHandler.getNameInDiscord(player.get(), channelConfig.discord.leftTemplate)), false);
                        }
                    }
                    mod.removeAndLogoutClient(playerId);
                    //unauthenticatedPlayers.remove(playerId);
                    mod.getLogger().info(player.get().getName() + " has disconnected!");
                }
            }
        }
    }
}
