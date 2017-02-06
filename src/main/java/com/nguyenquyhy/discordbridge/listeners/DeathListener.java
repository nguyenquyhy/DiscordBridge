package com.nguyenquyhy.discordbridge.listeners;

import com.nguyenquyhy.discordbridge.DiscordBridge;
import com.nguyenquyhy.discordbridge.models.ChannelConfig;
import com.nguyenquyhy.discordbridge.models.GlobalConfig;
import com.nguyenquyhy.discordbridge.utils.ChannelUtil;
import com.nguyenquyhy.discordbridge.utils.ConfigUtil;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Channel;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent;

public class DeathListener {
    @Listener
    public void onPlayerDeath(DestructEntityEvent.Death event) {
        DiscordBridge mod = DiscordBridge.getInstance();
        GlobalConfig config = mod.getConfig();
        DiscordAPI client = mod.getBotClient();

        if (!(event.getTargetEntity() instanceof Player) || event.isMessageCancelled()) return;
        Player player = (Player) event.getTargetEntity();

        if (client != null) {
            for (ChannelConfig channelConfig : config.channels) {
                if (StringUtils.isNotBlank(channelConfig.discordId) && channelConfig.discord != null) {
                    String template = ConfigUtil.get(channelConfig.discord.deathTemplate, null);
                    if (StringUtils.isNotBlank(template)) {
                        Channel channel = client.getChannelById(channelConfig.discordId);
                        ChannelUtil.sendMessage(channel, template.replace("%s", event.getMessage().toPlain()));
                    }
                }
            }
        }
    }

}
