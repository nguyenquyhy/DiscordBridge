package com.nguyenquyhy.spongediscord.listeners;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

/**
 * Created by Hy on 10/13/2016.
 */
public class ClientConnectionListener {
    @Listener
    public void onJoin(ClientConnectionEvent.Join event) {
//        Optional<Player> player = event.getCause().first(Player.class);
//        if (player.isPresent()) {
//            UUID playerId = player.get().getUniqueId();
//            boolean loggedIn = false;
//            if (!discordClients.containsKey(playerId)) {
//                loggedIn = LoginHandler.loginNormalAccount(player.get());
//            }
//
//            if (!loggedIn) {
//                // User is not logged in => use global client
//                unauthenticatedPlayers.add(playerId);
//
//                if (StringUtils.isNotBlank(config.JOINED_MESSAGE) && defaultDiscordClient != null && defaultDiscordClient.isReady()) {
//                    try {
//                        IChannel channel = defaultDiscordClient.getChannelByID(config.CHANNEL_ID);
//                        channel.sendMessage(String.format(config.JOINED_MESSAGE, getNameInDiscord(player.get())), false, config.NONCE);
//                    } catch (DiscordException | MissingPermissionsException | RateLimitException e) {
//                        logger.error(e.getLocalizedMessage(), e);
//                    }
//                }
//            }
//        }
    }

    @Listener
    public void onDisconnect(ClientConnectionEvent.Disconnect event) {
//        Optional<Player> player = event.getCause().first(Player.class);
//        if (player.isPresent()) {
//            UUID playerId = player.get().getUniqueId();
//            if (config.CHANNEL_ID != null && !config.CHANNEL_ID.isEmpty() && StringUtils.isNotBlank(config.LEFT_MESSAGE)) {
//                IDiscordClient client = discordClients.get(playerId);
//                IChannel channel = null;
//                if (client != null && client.isReady()) {
//                    channel = client.getChannelByID(config.CHANNEL_ID);
//                } else if (defaultDiscordClient != null && defaultDiscordClient.isReady()) {
//                    channel = defaultDiscordClient.getChannelByID(config.CHANNEL_ID);
//                }
//                if (channel != null) {
//                    try {
//                        channel.sendMessage(String.format(config.LEFT_MESSAGE, getNameInDiscord(player.get())), false, config.NONCE);
//                    } catch (MissingPermissionsException | RateLimitException | DiscordException e) {
//                    }
//                }
//            }
//            removeAndLogoutClient(playerId);
//            unauthenticatedPlayers.remove(playerId);
//            getLogger().info(player.get().getName() + " has disconnected!");
//        }
    }
}
