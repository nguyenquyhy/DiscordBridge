package com.nguyenquyhy.spongediscord.logics;

import com.nguyenquyhy.spongediscord.SpongeDiscord;
import com.nguyenquyhy.spongediscord.utils.TextUtil;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.message.Message;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by Hy on 8/6/2016.
 */
public class MessageHandler {
    /**
     * Forward Discord messages to Minecraft
     * @param message
     * @param commandSource
     */
    public static void discordMessageReceived(Message message, CommandSource commandSource) {
        SpongeDiscord mod = SpongeDiscord.getInstance();
        Logger logger = mod.getLogger();
        Config config = mod.getConfig();

        String content =  TextUtil.formatDiscordEmoji(message.getContent());
        if (message.getChannelReceiver().getId().equals(config.CHANNEL_ID) && !content.contains(TextUtil.SPECIAL_CHAR)) {
            String author = message.getAuthor().getName();
            Text formattedMessage = TextUtil.formatUrl(String.format(config.MESSAGE_MINECRAFT_TEMPLATE.replace("%a", author), content));
            logger.info(formattedMessage.toPlain());
            if (commandSource != null) {
                commandSource.sendMessage(formattedMessage);
            } else {
                // This case is used for default account
                Sponge.getServer().getWorlds().forEach(w ->
                        w.getEntities(e -> e.getType().equals(EntityTypes.PLAYER)).forEach(p -> ((Player)p).sendMessage(formattedMessage)));
//                for (UUID playerUUID : mod.getUnauthenticatedPlayers()) {
//                    Optional<Player> player = Sponge.getServer().getPlayer(playerUUID);
//                    if (player.isPresent()) {
//                        player.get().sendMessage(formattedMessage);
//                    }
//                }
            }
        }
    }

    public static String getNameInDiscord(Player player) {
        return player.getName().replace("_", "\\_");
    }
}
