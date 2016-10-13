package com.nguyenquyhy.spongediscord.logics;

import com.google.common.util.concurrent.FutureCallback;
import com.nguyenquyhy.spongediscord.SpongeDiscord;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.CommandBlockSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by Hy on 8/6/2016.
 */
public class LoginHandler {
    public static boolean loginGlobalAccount() {
        SpongeDiscord mod = SpongeDiscord.getInstance();
        Logger logger = mod.getLogger();
        Config config = mod.getConfig();

        if (config.CHANNEL_ID == null) {
            logger.error("Missing Channel config!");
            return false;
        }

        String cachedToken = mod.getStorage().getDefaultToken();
        String tokenUsed = StringUtils.isNotBlank(config.BOT_TOKEN) ? config.BOT_TOKEN : cachedToken;

        if (StringUtils.isBlank(tokenUsed)) {
            logger.warn("No default account is available! Messages can only get from and to authenticated players.");
            return false;
        }

        DiscordAPI defaultClient = mod.getDefaultDiscordClient();
        if (defaultClient != null && defaultClient.getChannelById(config.CHANNEL_ID) != null && defaultClient.getToken().equals(tokenUsed)) {
            return true;
        }

        if (defaultClient != null) {
            defaultClient.disconnect();
        }

        if (StringUtils.isNotBlank(config.BOT_TOKEN)) {
            logger.info("Logging in to bot Discord account...");

            DiscordAPI client = Javacord.getApi(config.BOT_TOKEN, true);
            prepareDefaultClient(client, null);
            return true;
        } else if (StringUtils.isNotBlank(cachedToken)) {
            logger.info("Logging in to default Discord account...");

            DiscordAPI client = Javacord.getApi(cachedToken, false);
            prepareDefaultClient(client, null);
            return true;
        }

        return false;
    }

    public static CommandResult login(CommandSource commandSource, String email, String password, boolean defaultAccount) {
        SpongeDiscord mod = SpongeDiscord.getInstance();
        Logger logger = mod.getLogger();
        DiscordAPI defaultClient = mod.getDefaultDiscordClient();

        if (defaultAccount) {
            if (defaultClient != null) {
                defaultClient.disconnect();
                commandSource.sendMessage(Text.of("Logged out of current default account."));
            }
        } else {
            logout(commandSource, true);
        }

        DiscordAPI client = Javacord.getApi(email, password);
        if (defaultAccount) {
            prepareDefaultClient(client, commandSource);
        } else {
            prepareClient(client, commandSource);
        }

        if (client.getToken() != null) {
            return CommandResult.success();
        } else {
            commandSource.sendMessage(Text.of(TextColors.RED, "Invalid username and/or password!"));
            return CommandResult.empty();
        }
    }

    public static CommandResult logout(CommandSource commandSource, boolean isSilence) {
        SpongeDiscord mod = SpongeDiscord.getInstance();

        if (commandSource instanceof Player) {
            Player player = (Player) commandSource;
            UUID playerId = player.getUniqueId();
            try {
                SpongeDiscord.getInstance().getStorage().removeToken(playerId);
            } catch (IOException e) {
                e.printStackTrace();
                commandSource.sendMessage(Text.of(TextColors.RED, "Cannot remove cached token!"));
            }
            mod.removeAndLogoutClient(playerId);
            mod.getUnauthenticatedPlayers().add(player.getUniqueId());

            if (!isSilence)
                commandSource.sendMessage(Text.of(TextColors.YELLOW, "Logged out of Discord!"));
            return CommandResult.success();
        } else if (commandSource instanceof ConsoleSource) {
            mod.removeAndLogoutClient(null);
            commandSource.sendMessage(Text.of("Logged out of Discord!"));
            return CommandResult.success();
        } else if (commandSource instanceof CommandBlockSource) {
            commandSource.sendMessage(Text.of(TextColors.YELLOW, "Cannot log out from command blocks!"));
            return CommandResult.empty();
        }
        return CommandResult.empty();
    }

    public static boolean loginNormalAccount(Player player) {
        SpongeDiscord mod = SpongeDiscord.getInstance();
        Logger logger = mod.getLogger();

        String cachedToken = mod.getStorage().getToken(player.getUniqueId());
        if (null != cachedToken && !cachedToken.isEmpty()) {
            player.sendMessage(Text.of(TextColors.GRAY, "Logging in to Discord..."));

            DiscordAPI client = Javacord.getApi(cachedToken, false);
            prepareClient(client, player);
            return true;
        }
        return false;
    }

    private static void prepareDefaultClient(DiscordAPI client, CommandSource commandSource) {
        SpongeDiscord mod = SpongeDiscord.getInstance();
        Logger logger = mod.getLogger();
        Config config = mod.getConfig();

        if (commandSource != null)
            commandSource.sendMessage(Text.of(TextColors.GOLD, TextStyles.BOLD, "Logging in..."));
        else
            logger.info("Logging in...");

        client.connect(new FutureCallback<DiscordAPI>() {
            @Override
            public void onSuccess(@Nullable DiscordAPI discordAPI) {
                client.registerListener((MessageCreateListener) (client, message)
                        -> {
                    if (message.getAuthor().isYourself()) {
                        MessageHandler.discordMessageReceived(message, commandSource);
                    } else {
                        MessageHandler.discordMessageReceived(message, null);
                    }
                });

//        client.getDispatcher().registerListener(new IListener<GuildCreateEvent>() {
//            @Override
//            public void handle(GuildCreateEvent guildCreateEvent) {
//                handleGuildCreateEvent(guildCreateEvent, client, commandSource);
//            }
//        });

                try {
                    User user = discordAPI.getYourself();
                    String name = "unknown";
                    if (user != null)
                        name = user.getName();
                    String text = "Discord account " + name + " will be used for all unauthenticated users!";
                    if (commandSource != null)
                        commandSource.sendMessage(Text.of(TextColors.GOLD, TextStyles.BOLD, text));
                    else
                        logger.info(text);

                    mod.setDefaultDiscordClient(client);
                    mod.getStorage().putDefaultToken(client.getToken());

                    if (config.CHANNEL_ID != null && !config.CHANNEL_ID.isEmpty()) {
                        Channel channel = client.getChannelById(config.CHANNEL_ID);
                        if (channel == null) {
                            if (StringUtils.isNotBlank(config.BOT_TOKEN)) {
                                logger.warn("Cannot access channel from Bot account! Please make sure the bot has permission.");
                            } else {
                                logger.info("Accepting channel invite for default account...");
                                acceptInvite(client);
                            }
                        } else {
                            channelJoined(client, channel, commandSource);
                        }
                    }
                } catch (IOException e) {
                    logger.error("Cannot connect to Discord!", e);
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                logger.error("Cannot connect to Discord!");
            }
        });
    }

    private static void prepareClient(DiscordAPI client, CommandSource commandSource) {
        SpongeDiscord mod = SpongeDiscord.getInstance();
        Config config = mod.getConfig();
        Logger logger = mod.getLogger();

        client.connect(new FutureCallback<DiscordAPI>() {
            @Override
            public void onSuccess(@Nullable DiscordAPI discordAPI) {
                try {
                    String name = client.getYourself().getName();
                    commandSource.sendMessage(Text.of(TextColors.GOLD, TextStyles.BOLD, "You have logged in to Discord account " + name + "!"));

                    client.registerListener((MessageCreateListener) (discordAPI1, message)
                            -> MessageHandler.discordMessageReceived(message, commandSource));

//        client.getDispatcher().registerListener(new IListener<GuildCreateEvent>() {
//            @Override
//            public void handle(GuildCreateEvent event) {
//                handleGuildCreateEvent(event, client, commandSource);
//            }
//        });

                    if (commandSource instanceof Player) {
                        Player player = (Player) commandSource;
                        UUID playerId = player.getUniqueId();
                        mod.getUnauthenticatedPlayers().remove(playerId);
                        mod.addClient(playerId, client);
                        mod.getStorage().putToken(playerId, client.getToken());
                    } else if (commandSource instanceof ConsoleSource) {
                        commandSource.sendMessage(Text.of("WARNING: This Discord account will be used only for this console session!"));
                        mod.addClient(null, client);
                    } else if (commandSource instanceof CommandBlockSource) {
                        commandSource.sendMessage(Text.of(TextColors.GREEN, "Account is valid!"));
                        return;
                    }

                    if (config.CHANNEL_ID != null && !config.CHANNEL_ID.isEmpty()) {
                        Channel channel = client.getChannelById(config.CHANNEL_ID);
                        if (channel == null) {
                            SpongeDiscord.getInstance().getLogger().info("Accepting channel invite");
                            acceptInvite(client);
                        } else {
                            channelJoined(client, channel, commandSource);
                        }
                    }
                } catch (IOException e) {
                    logger.error("Cannot connect to Discord!", e);
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                logger.error("Cannot connect to Discord!", throwable);
            }
        });
    }

    private static Channel acceptInvite(DiscordAPI client) {
        SpongeDiscord mod = SpongeDiscord.getInstance();
        Logger logger = mod.getLogger();
        Config config = mod.getConfig();

        if (StringUtils.isNotBlank(config.INVITE_CODE)) {
            client.acceptInvite(config.INVITE_CODE, new FutureCallback<Server>() {
                @Override
                public void onSuccess(@Nullable Server server) {

                }

                @Override
                public void onFailure(Throwable throwable) {

                }
            });
//            try {
//                invite.accept();
//                return client.getChannelByID(config.CHANNEL_ID);
//            } catch (Exception e) {
//                logger.error("Cannot accept invitation " + config.INVITE_CODE + "!" + e.getLocalizedMessage(), e);
//            }
        }
        return null;
    }

//    private static void handleGuildCreateEvent(GuildCreateEvent event, IDiscordClient client, CommandSource commandSource) {
//        SpongeDiscord mod = SpongeDiscord.getInstance();
//        Logger logger = mod.getLogger();
//        Config config = mod.getConfig();
//
//        IChannel channel = event.getGuild().getChannelByID(config.CHANNEL_ID);
//        try {
//            channelJoined(client, channel, commandSource);
//        } catch (DiscordException | MissingPermissionsException | RateLimitException e) {
//            logger.error(e.getLocalizedMessage(), e);
//        }
//    }

    private static void channelJoined(DiscordAPI client, Channel channel, CommandSource src) {
        SpongeDiscord mod = SpongeDiscord.getInstance();
        Logger logger = mod.getLogger();
        Config config = mod.getConfig();

        if (channel != null) {
            if (client != mod.getDefaultDiscordClient()) {
                String playerName = "console";
                if (src instanceof Player) {
                    Player player = (Player) src;
                    playerName = player.getName();
                }
                if (StringUtils.isNotBlank(config.JOINED_MESSAGE)) {
                    channel.sendMessage(String.format(config.JOINED_MESSAGE, playerName), false);
                }
                logger.info(playerName + " connected to Discord channel.");
            } else {
                logger.info("Default account has connected to Discord channel.");
                if (StringUtils.isNotBlank(config.MESSAGE_DISCORD_SERVER_UP)) {
                    channel.sendMessage(config.MESSAGE_DISCORD_SERVER_UP, false);
                }
            }
        }
    }
}
