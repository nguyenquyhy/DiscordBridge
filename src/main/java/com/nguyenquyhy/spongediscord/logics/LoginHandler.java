package com.nguyenquyhy.spongediscord.logics;

import com.nguyenquyhy.spongediscord.SpongeDiscord;
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
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.MessageSendEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.obj.Invite;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

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

        IDiscordClient defaultClient = mod.getDefaultClient();
        if (defaultClient != null && defaultClient.getChannelByID(config.CHANNEL_ID) != null && defaultClient.getToken().equals(tokenUsed)) {
            return true;
        }

        if (defaultClient != null) {
            try {
                defaultClient.logout();
            } catch (RateLimitException | DiscordException e) {
            }
        }

        if (StringUtils.isNotBlank(config.BOT_TOKEN)) {
            logger.info("Logging in to bot Discord account...");

            try {
                ClientBuilder clientBuilder = new ClientBuilder();
                IDiscordClient client = clientBuilder.withToken(config.BOT_TOKEN).build();
                prepareDefaultClient(client, null);
                client.login();
                return true;
            } catch (DiscordException e) {
                logger.error(e.getErrorMessage(), e);
            }
        } else if (StringUtils.isNotBlank(cachedToken)) {
            logger.info("Logging in to default Discord account...");

            try {
                ClientBuilder clientBuilder = new ClientBuilder();
                IDiscordClient client = clientBuilder.withUserToken(cachedToken).build();
                prepareDefaultClient(client, null);
                client.login();
                return true;
            } catch (DiscordException e) {
                logger.error(e.getErrorMessage(), e);
            }
        }

        return false;
    }

    public static CommandResult login(CommandSource commandSource, String email, String password, boolean defaultAccount) {
        SpongeDiscord mod = SpongeDiscord.getInstance();
        Logger logger = mod.getLogger();
        Config config = mod.getConfig();
        IDiscordClient defaultClient = mod.getDefaultClient();

        if (defaultAccount) {
            if (defaultClient != null) {
                try {
                    defaultClient.logout();
                    commandSource.sendMessage(Text.of("Logged out of current default account."));
                } catch (DiscordException | RateLimitException e) {
                    logger.error("Cannot logout! " + e.getLocalizedMessage(), e);
                }
            }
        } else {
            logout(commandSource, true);
        }

        try {
            ClientBuilder clientBuilder = new ClientBuilder().withLogin(email, password);
            IDiscordClient client = clientBuilder.build();
            if (defaultAccount) {
                prepareDefaultClient(client, commandSource);
            } else {
                prepareClient(client, commandSource);
            }

            client.login();

            if (client.getToken() != null) {
                return CommandResult.success();
            } else {
                commandSource.sendMessage(Text.of(TextColors.RED, "Invalid username and/or password!"));
                return CommandResult.empty();
            }
        } catch (DiscordException e) {
            logger.error("Cannot login! " + e.getLocalizedMessage(), e);
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

            try {
                ClientBuilder clientBuilder = new ClientBuilder();
                IDiscordClient client = clientBuilder.withUserToken(cachedToken).build();
                prepareClient(client, player);
                client.login();
                return true;
            } catch (DiscordException e) {
                logger.error("Cannot login to Discord! " + e.getMessage());
            }
        }
        return false;
    }

    private static void prepareDefaultClient(IDiscordClient client, CommandSource commandSource) {
        SpongeDiscord mod = SpongeDiscord.getInstance();
        Logger logger = mod.getLogger();
        Config config = mod.getConfig();

        client.getDispatcher().registerListener(new IListener<ReadyEvent>() {
            @Override
            public void handle(ReadyEvent readyEvent) {
                try {
                    String name = client.getOurUser().getName();
                    String text = "Discord account " + name + " will be used for all unauthenticated users!";
                    if (commandSource != null)
                        commandSource.sendMessage(Text.of(TextColors.GOLD, TextStyles.BOLD, text));
                    else
                        logger.info(text);

                    mod.setDefaultClient(client);
                    mod.getStorage().putDefaultToken(client.getToken());

                    if (config.CHANNEL_ID != null && !config.CHANNEL_ID.isEmpty()) {
                        IChannel channel = client.getChannelByID(config.CHANNEL_ID);
                        if (channel == null) {
                            logger.info("Accepting channel invite for default account...");
                            acceptInvite(client);
                        } else {
                            channelJoined(client, channel, commandSource);
                        }
                    }
                } catch (IOException | DiscordException | RateLimitException e) {
                    logger.error("Cannot handle ReadyEvent! " + e.getLocalizedMessage(), e);
                } catch (MissingPermissionsException e) {
                    if (e.getMissingPermission().contains(Permissions.SEND_MESSAGES)) {
                        logger.error("Sponge-Discord cannot send messages to the preset channel!");
                    } else {
                        logger.error(e.getErrorMessage(), e);
                    }
                }
            }
        });

        client.getDispatcher().registerListener(new IListener<MessageSendEvent>() {
            @Override
            public void handle(MessageSendEvent event) {
                MessageHandler.discordMessageReceived(event.getMessage(), commandSource);
            }
        });

        client.getDispatcher().registerListener(new IListener<MessageReceivedEvent>() {
            @Override
            public void handle(MessageReceivedEvent messageReceivedEvent) {
                MessageHandler.discordMessageReceived(messageReceivedEvent.getMessage(), null);
            }
        });

        client.getDispatcher().registerListener(new IListener<GuildCreateEvent>() {
            @Override
            public void handle(GuildCreateEvent guildCreateEvent) {
                handleGuildCreateEvent(guildCreateEvent, client, commandSource);
            }
        });
    }

    private static void prepareClient(IDiscordClient client, CommandSource commandSource) {
        SpongeDiscord mod = SpongeDiscord.getInstance();
        Logger logger = mod.getLogger();
        Config config = mod.getConfig();

        client.getDispatcher().registerListener(new IListener<ReadyEvent>() {
            @Override
            public void handle(ReadyEvent readyEvent) {
                try {
                    String name = client.getOurUser().getName();
                    commandSource.sendMessage(Text.of(TextColors.GOLD, TextStyles.BOLD, "You have logged in to Discord account " + name + "!"));
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
                        IChannel channel = client.getChannelByID(config.CHANNEL_ID);
                        if (channel == null) {
                            SpongeDiscord.getInstance().getLogger().info("Accepting channel invite");
                            acceptInvite(client);
                        } else {
                            channelJoined(client, channel, commandSource);
                        }
                    }
                } catch (IOException | DiscordException | MissingPermissionsException | RateLimitException e) {
                    e.printStackTrace();
                }
            }
        });

        client.getDispatcher().registerListener(new IListener<MessageSendEvent>() {
            @Override
            public void handle(MessageSendEvent event) {
                MessageHandler.discordMessageReceived(event.getMessage(), commandSource);
            }
        });

        client.getDispatcher().registerListener(new IListener<MessageReceivedEvent>() {
            @Override
            public void handle(MessageReceivedEvent event) {
                MessageHandler.discordMessageReceived(event.getMessage(), commandSource);
            }
        });

        client.getDispatcher().registerListener(new IListener<GuildCreateEvent>() {
            @Override
            public void handle(GuildCreateEvent event) {
                handleGuildCreateEvent(event, client, commandSource);
            }
        });
    }

    private static IChannel acceptInvite(IDiscordClient client) {
        SpongeDiscord mod = SpongeDiscord.getInstance();
        Logger logger = mod.getLogger();
        Config config = mod.getConfig();

        if (StringUtils.isNotBlank(config.INVITE_CODE)) {
            Invite invite = new Invite(client, config.INVITE_CODE);
            try {
                invite.accept();
                return client.getChannelByID(config.CHANNEL_ID);
            } catch (Exception e) {
                logger.error("Cannot accept invitation " + config.INVITE_CODE + "!" + e.getLocalizedMessage(), e);
            }
        }
        return null;
    }

    private static void handleGuildCreateEvent(GuildCreateEvent event, IDiscordClient client, CommandSource commandSource) {
        SpongeDiscord mod = SpongeDiscord.getInstance();
        Logger logger = mod.getLogger();
        Config config = mod.getConfig();

        IChannel channel = event.getGuild().getChannelByID(config.CHANNEL_ID);
        try {
            channelJoined(client, channel, commandSource);
        } catch (IOException | DiscordException | MissingPermissionsException | RateLimitException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    private static void channelJoined(IDiscordClient client, IChannel channel, CommandSource src)
            throws IOException, DiscordException, MissingPermissionsException, RateLimitException {
        SpongeDiscord mod = SpongeDiscord.getInstance();
        Logger logger = mod.getLogger();
        Config config = mod.getConfig();

        if (channel != null) {
            if (client != mod.getDefaultClient()) {
                String playerName = "console";
                if (src instanceof Player) {
                    Player player = (Player) src;
                    playerName = player.getName();
                }
                if (config.JOINED_MESSAGE != null) {
                    channel.sendMessage(String.format(config.JOINED_MESSAGE, playerName), false, config.NONCE);
                }
                logger.info(playerName + " connected to Discord channel.");
            } else {
                logger.info("Default account has connected to Discord channel.");
                if (StringUtils.isNotBlank(config.MESSAGE_DISCORD_SERVER_UP)) {
                    channel.sendMessage(config.MESSAGE_DISCORD_SERVER_UP, false, config.NONCE);
                }
            }
        }
    }
}
