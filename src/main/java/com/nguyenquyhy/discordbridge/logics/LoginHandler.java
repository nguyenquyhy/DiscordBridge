package com.nguyenquyhy.discordbridge.logics;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.nguyenquyhy.discordbridge.DiscordBridge;
import com.nguyenquyhy.discordbridge.database.IStorage;
import com.nguyenquyhy.discordbridge.models.ChannelConfig;
import com.nguyenquyhy.discordbridge.models.GlobalConfig;
import com.nguyenquyhy.discordbridge.utils.ChannelUtil;
import com.nguyenquyhy.discordbridge.utils.ErrorMessages;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.CommandBlockSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Hy on 8/6/2016.
 */
public class LoginHandler {
    private static DiscordBridge mod = DiscordBridge.getInstance();
    private static Logger logger = mod.getLogger();

    private static Map<CommandSource, String> MFA_TICKETS = new HashMap<>();

    public static boolean loginBotAccount() {
        GlobalConfig config = mod.getConfig();

        if (StringUtils.isBlank(config.botToken)) {
            logger.warn("No Bot token is available! Messages can only get from and to authenticated players.");
            return false;
        }

        JDA defaultClient = mod.getBotClient();
        if (defaultClient != null && defaultClient.getToken().equals(config.botToken)) {
            return true;
        }

        if (defaultClient != null) {
            defaultClient.shutdown();
        }

        logger.info("Logging in to bot Discord account...");

        try {
            prepareBotClient(config.botToken, null);
            return true;
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (RateLimitedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param player
     * @return
     */
    public static boolean loginHumanAccount(Player player) {
        IStorage storage = mod.getStorage();

        if (storage != null) {
            String cachedToken = mod.getStorage().getToken(player.getUniqueId());
            if (StringUtils.isNotBlank(cachedToken)) {
                player.sendMessage(Text.of(TextColors.GRAY, "Logging in to Discord..."));

                JDA client = mod.getHumanClients().get(player.getUniqueId());
                if (client != null) {
                    client.shutdown();
                }
                return PrepareHumanClientForCommandSource(player, cachedToken);
            }
        }
        return false;
    }

    public static CommandResult login(CommandSource commandSource, String email, String password) {
        logout(commandSource, true);

        try {
            HttpResponse<JsonNode> response = Unirest.post("https://discordapp.com/api/v6/auth/login")
                    .header("content-type", "application/json")
                    .body(new JSONObject().put("email", email).put("password", password))
                    .asJson();
            if (response.getStatus() != 200) {
                DiscordBridge.getInstance().getLogger().info("Auth response {} code with: {}", response.getStatus(), response.getBody());
                commandSource.sendMessage(Text.of(TextColors.RED, "Wrong email or password!"));
                return CommandResult.empty();
            }
            JSONObject result = response.getBody().getObject();
            if (result.has("mfa") && result.getBoolean("mfa")) {
                MFA_TICKETS.put(commandSource, result.getString("ticket"));
                commandSource.sendMessage(Text.of(TextColors.GREEN, "Additional authorization required! Please type '/discord otp <code>' within a code from your authorization app"));
            } else if (result.has("token")) {
                String token = result.getString("token");

                if (PrepareHumanClientForCommandSource(commandSource, token)) return CommandResult.success();
            } else {
                commandSource.sendMessage(Text.of(TextColors.RED, "Unexpected error!"));
            }
        } catch (UnirestException e) {
            e.printStackTrace();
            commandSource.sendMessage(Text.of(TextColors.RED, "Unexpected error!"));
        }
        return CommandResult.empty();
    }

    public static CommandResult otp(CommandSource commandSource, int code) {
        String ticket = MFA_TICKETS.remove(commandSource);
        if (ticket == null) {
            commandSource.sendMessage(Text.of(TextColors.RED, "No OTP auth queued!"));
            return CommandResult.empty();
        }
        try {
            HttpResponse<JsonNode> response = Unirest.post("https://discordapp.com/api/v6/auth/mfa/totp")
                    .header("content-type", "application/json")
                    .body(new JSONObject().put("code", String.format("%06d", code)).put("ticket", ticket))
                    .asJson();
            if (response.getStatus() != 200) {
                commandSource.sendMessage(Text.of(TextColors.RED, "Wrong auth code! Retry with '/discord loginconfirm <email> <password>'"));
                return CommandResult.empty();
            }
            String token = response.getBody().getObject().getString("token");
            if (PrepareHumanClientForCommandSource(commandSource, token)) return CommandResult.success();
        } catch (UnirestException e) {
            e.printStackTrace();
            commandSource.sendMessage(Text.of(TextColors.RED, "Unexpected error!"));
        }
        return CommandResult.empty();
    }

    public static boolean PrepareHumanClientForCommandSource(CommandSource commandSource, String token) {
        try {
            prepareHumanClient(token, commandSource);
            return true;
        } catch (LoginException e) {
            e.printStackTrace();
            logger.error("Cannot connect to Discord!", e);
            if (commandSource != null) {
                commandSource.sendMessage(Text.of(TextColors.RED, "Unable to login! Please check your login details or your email for login verification."));
            }
        } catch (InterruptedException | RateLimitedException e) {
            e.printStackTrace();
            logger.error("Cannot connect to Discord!", e);
            if (commandSource != null) {
                commandSource.sendMessage(Text.of(TextColors.RED, "Unable to login! Please try again later."));
            }
        }
        return false;
    }

    public static CommandResult logout(CommandSource commandSource, boolean isSilence) {
        if (commandSource instanceof Player) {
            Player player = (Player) commandSource;
            UUID playerId = player.getUniqueId();
            try {
                DiscordBridge.getInstance().getStorage().removeToken(playerId);
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

    private static JDA prepareBotClient(String botToken, CommandSource commandSource) throws LoginException, RateLimitedException, InterruptedException {
        GlobalConfig config = mod.getConfig();

        if (commandSource != null)
            commandSource.sendMessage(Text.of(TextColors.GOLD, TextStyles.BOLD, "Logging in..."));

        JDA client = new JDABuilder(AccountType.BOT)
                .setToken(botToken)
                .addEventListener(new MessageHandler())
                .buildBlocking();

        User user = client.getSelfUser();
        String name = "unknown";
        if (user != null)
            name = user.getName();
        String text = "Bot account " + name + " will be used for all unauthenticated users!";
        if (StringUtils.isNotBlank(config.botDiscordGame)) {
            client.getPresence().setGame(Game.playing(config.botDiscordGame));
        }
        if (commandSource != null)
            commandSource.sendMessage(Text.of(TextColors.GOLD, TextStyles.BOLD, text));
        else
            logger.info(text);

        mod.setBotClient(client);

        for (ChannelConfig channelConfig : config.channels) {
            if (StringUtils.isNotBlank(channelConfig.discordId)) {
                TextChannel channel = client.getTextChannelById(channelConfig.discordId);
                if (channel != null) {
                    channelJoined(client, config, channelConfig, channel, commandSource);
                } else {
                    ErrorMessages.CHANNEL_NOT_FOUND.log(channelConfig.discordId);
                }
            } else {
                logger.warn("Channel with empty ID!");
            }
        }

        return client;
    }

    private static JDA prepareHumanClient(String cachedToken, CommandSource commandSource) throws LoginException, InterruptedException, RateLimitedException {
        if (commandSource instanceof CommandBlockSource) {
            commandSource.sendMessage(Text.of(TextColors.GREEN, "Account is valid!"));
            return null;
        }

        GlobalConfig config = mod.getConfig();

        JDA client = new JDABuilder(AccountType.CLIENT)
                .setToken(cachedToken)
                .buildBlocking();

        try {
            String name = client.getSelfUser().getName();
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
            }

            for (ChannelConfig channelConfig : config.channels) {
                if (StringUtils.isNotBlank(channelConfig.discordId)) {
                    TextChannel channel = client.getTextChannelById(channelConfig.discordId);
                    if (channel != null) {
                        channelJoined(client, config, channelConfig, channel, commandSource);
                    } else {
                        ErrorMessages.CHANNEL_NOT_FOUND_HUMAN.log(channelConfig.discordId);
                    }
                } else {
                    logger.warn("Channel with empty ID!");
                }
            }
        } catch (IOException e) {
            logger.error("Cannot connect to Discord!", e);
        }

        return client;
    }

    private static void channelJoined(JDA client, GlobalConfig config, ChannelConfig channelConfig, TextChannel channel, CommandSource src) {

        if (channel != null && StringUtils.isNotBlank(channelConfig.discordId) && channelConfig.discord != null) {
            if (client != mod.getBotClient()) {
                String playerName = "console";
                if (src instanceof Player) {
                    Player player = (Player) src;
                    playerName = player.getName();
                }
                if (StringUtils.isNotBlank(channelConfig.discord.joinedTemplate)) {
                    String content = String.format(channelConfig.discord.joinedTemplate, playerName);
                    ChannelUtil.sendMessage(channel, content);
                }
                logger.info(playerName + " connected to Discord channel " + channelConfig.discordId + ".");
            } else {
                logger.info("Bot account has connected to Discord channel " + channelConfig.discordId + ".");
                if (StringUtils.isNotBlank(channelConfig.discord.serverUpMessage)) {
                    ChannelUtil.sendMessage(channel, channelConfig.discord.serverUpMessage);
                }
            }
        }
    }
}
