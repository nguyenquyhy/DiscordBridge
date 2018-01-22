package com.nguyenquyhy.discordbridge.listeners;

import com.nguyenquyhy.discordbridge.DiscordBridge;
import com.nguyenquyhy.discordbridge.models.ChannelConfig;
import com.nguyenquyhy.discordbridge.models.GlobalConfig;
import com.nguyenquyhy.discordbridge.utils.ChannelUtil;
import com.nguyenquyhy.discordbridge.utils.ConfigUtil;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent;

public class DeathListener {
    @Listener
    public void onPlayerDeath(DestructEntityEvent.Death event) {
        DiscordBridge mod = DiscordBridge.getInstance();
        GlobalConfig config = mod.getConfig();
        JDA client = mod.getBotClient();

        if (!(event.getTargetEntity() instanceof Player) || event.isMessageCancelled() || StringUtils.isBlank(event.getMessage().toPlain())) return;
        Player player = (Player) event.getTargetEntity();

        if (client != null) {
            for (ChannelConfig channelConfig : config.channels) {
                if (StringUtils.isNotBlank(channelConfig.discordId) && channelConfig.discord != null) {
                    String template = ConfigUtil.get(channelConfig.discord.deathTemplate, null);
                    if (StringUtils.isNotBlank(template)) {
                        TextChannel channel = client.getTextChannelById(channelConfig.discordId);
                        ChannelUtil.sendMessage(channel, template.replace("%s", event.getMessage().toPlain()));
                    }
                }
            }
        }
    }

}
