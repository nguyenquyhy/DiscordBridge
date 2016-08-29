package com.nguyenquyhy.spongediscord.logics;

import com.nguyenquyhy.spongediscord.SpongeDiscord;
import com.nguyenquyhy.spongediscord.utils.TextUtil;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import sx.blah.discord.handle.obj.IMessage;

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
    public static void discordMessageReceived(IMessage message, CommandSource commandSource) {
        SpongeDiscord mod = SpongeDiscord.getInstance();
        Config config = mod.getConfig();

        if (message.getChannel().getID().equals(config.CHANNEL_ID) && !config.NONCE.equals(message.getNonce())) {
            String content = message.getContent();
            String author = message.getAuthor().getName();
//            Text formattedMessage = TextSerializers.FORMATTING_CODE.deserialize(
//                    String.format(config.MESSAGE_MINECRAFT_TEMPLATE.replace("%a", author), content));
            Text formattedMessage = TextUtil.formatUrl(String.format(config.MESSAGE_MINECRAFT_TEMPLATE.replace("%a", author), content));
            if (commandSource != null) {
                commandSource.sendMessage(formattedMessage);
            } else {
                // This case is used for default account
                for (UUID playerUUID : mod.getUnauthenticatedPlayers()) {
                    Optional<Player> player = Sponge.getServer().getPlayer(playerUUID);
                    if (player.isPresent()) {
                        player.get().sendMessage(formattedMessage);
                    }
                }
            }
        }
    }

}
