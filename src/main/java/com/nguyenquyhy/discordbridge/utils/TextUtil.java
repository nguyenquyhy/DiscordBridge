package com.nguyenquyhy.discordbridge.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.nguyenquyhy.discordbridge.DiscordBridge;
import com.nguyenquyhy.discordbridge.models.ChannelMinecraftConfigCore;
import com.nguyenquyhy.discordbridge.models.ChannelMinecraftMentionConfig;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.permissions.Role;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Hy on 8/29/2016.
 */
public class TextUtil {
    private static final Pattern urlPattern =
            Pattern.compile("(?<first>(^|\\s))(?<colour>(&[0-9a-flmnork])+)?(?<url>(http(s)?://)?([A-Za-z0-9]+\\.)+[A-Za-z0-9]{2,}\\S*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern mentionPattern =
            Pattern.compile("([@#]\\S*)");

    public static final StyleTuple EMPTY = new StyleTuple(TextColors.NONE, TextStyles.NONE);

    public static String formatDiscordMessage(String message) {
        for (Emoji emoji : Emoji.values()) {
            message = message.replace(emoji.unicode, emoji.minecraftFormat);
        }
        return message;
    }

    public static String formatMinecraftMessage(String message) {
        for (Emoji emoji : Emoji.values()) {
            message = message.replace(emoji.minecraftFormat, emoji.discordFormat);
        }
        return message;
    }

    /**
     * @param message the Minecraft message to be checked for valid mentions
     * @param server the server to search through Users and Roles
     * @param player the player who's permissions to check
     * @param isBot used to ignore permission checks for authenticated users
     * @return the message with mentions properly formatted for Discord, if allowed
     */
    public static String formatMinecraftMention(String message, Server server, Player player, boolean isBot) {
        Matcher m = mentionPattern.matcher(message);
        Logger logger = DiscordBridge.getInstance().getLogger();

        while (m.find()) {
            String mention = m.group();
            if (mention.contains("@")) {
                String mentionName = mention.replace("@", "");
                if ((mentionName.equalsIgnoreCase("here") && isBot && !player.hasPermission("discordbridge.mention.here")) ||
                        (mentionName.equalsIgnoreCase("everyone") && isBot && !player.hasPermission("discordbridge.mention.everyone"))) {
                    message = message.replace(mention, mentionName);
                    continue;
                }
                if (!isBot || player.hasPermission("discordbridge.mention.name." + mentionName.toLowerCase())) {
                    Optional<User> user = DiscordUtil.getUserByName(mentionName, server);
                    logger.debug(String.format("Found user %s: %s", mentionName, user.isPresent()));
                    if (user.isPresent()) {
                        message = message.replace(mention, "<@" + user.get().getId() + ">");
                        continue;
                    }
                }
                if (!isBot || player.hasPermission("discordbridge.mention.role." + mentionName.toLowerCase())) {
                    Optional<Role> role = DiscordUtil.getRoleByName(mentionName, server);
                    logger.debug(String.format("Found role %s: %s", mentionName, role.isPresent()));
                    if (role.isPresent() && role.get().isMentionable()) {
                        message = message.replace(mention, "<@&" + role.get().getId() + ">");
                    }
                }
            } else if (mention.contains("#")) {
                String mentionName = mention.replace("#", "");
                if (!isBot || player.hasPermission("discordbridge.mention.channel." + mentionName.toLowerCase())) {
                    Optional<Channel> channel = DiscordUtil.getChannelByName(mentionName, server);
                    logger.debug(String.format("Found channel %s: %s", mentionName, channel.isPresent()));
                    if (channel.isPresent()) {
                        message = message.replace(mention, "<#" + channel.get().getId() + ">");
                    }
                }
            }
        }
        return message;
    }

    private static Map<String, Map<String, Boolean>> needReplacementMap = new HashMap<>();

    public static String escapeForDiscord(String text, String template, String token) {
        if (!needReplacementMap.containsKey(token)) {
            needReplacementMap.put(token, new HashMap<>());
        }
        Map<String, Boolean> needReplacement = needReplacementMap.get(token);
        if (!needReplacement.containsKey(template)) {
            boolean need = !Pattern.matches(".*`.*" + token + ".*`.*", template)
                    && Pattern.matches(".*_.*" + token + ".*_.*", template);
            needReplacement.put(template, need);
        }
        if (needReplacement.get(template)) text = text.replace("_", "\\_");
        return text;
    }

    /**
     * @param config
     * @param message
     * @return
     */
    public static Text formatForMinecraft(ChannelMinecraftConfigCore config, Message message) {
        Server server = message.getChannelReceiver().getServer();
        User author = message.getAuthor();

        // Replace %u with author's username
        String s = ConfigUtil.get(config.chatTemplate, "&7<%a> &f%s").replace("%u", author.getName());

        // Replace %n with author's nickname or username
        String nickname = (author.getNickname(server) != null) ? author.getNickname(server) : author.getName();
        s = s.replace("%a", nickname);

        // Get author's highest role
        Optional<Role> highestRole = getHighestRole(author, server);
        String roleName = "Discord"; //(config.roles.containsKey("everyone")) ? config.roles.get("everyone").name : "Member";
        Color roleColor = Color.WHITE;
        if (highestRole.isPresent()) {
            roleName = highestRole.get().getName();
            roleColor = highestRole.get().getColor();
        }
        // Replace %r with Message author's highest role
        String colorString = ColorUtil.getColorCode(roleColor);
        s = (StringUtils.isNotBlank(colorString)) ? s.replace("%r", colorString + roleName + "&r") : s.replace("%r", roleName);
        // Replace %g with Message author's game
        String game = author.getGame();
        if (game != null) s = s.replace("%g", game);

        // Add the actual message
        s = String.format(s, message.getContent());

        // Replace Discord-specific stuffs
        s = TextUtil.formatDiscordMessage(s);

        // Format URL
        List<Text> texts = formatUrl(s);
        // Replace user mentions with readable names
        texts = formatUserMentions(texts, config.mention, message.getMentions(), server);
        // Replace role mentions
        texts = formatRoleMentions(texts, config.mention, message.getMentionedRoles());
        // Format @here/@everyone mentions
        texts = formatEveryoneMentions(texts, config.mention, message.isMentioningEveryone());
        // Format #channel mentions
        texts = formatChannelMentions(texts, config.mention);

        return Text.join(texts);
    }

    /**
     * @param texts  The message that may contain User mentions
     * @param config   The mention config to be used for formatting
     * @param mentions The list of users mentioned
     * @param server   The server to be used for nickname support
     * @return The final message with User mentions formatted
     */
    private static List<Text> formatUserMentions(List<Text> texts, ChannelMinecraftMentionConfig config, List<User> mentions, Server server) {
        if (mentions.isEmpty()) return texts;
        // Prepare the text builders
        Map<User, Text.Builder> formattedMentioning = new HashMap<>();
        for (User mention : mentions) {
            Optional<Role> role = getHighestRole(mention, server);
            String nick = (mention.getNickname(server) != null) ? mention.getNickname(server) : mention.getName();
            String mentionString = ConfigUtil.get(config.userTemplate, "@%a")
                    .replace("%s", nick).replace("%a", nick)
                    .replace("%u", mention.getName());
            Text.Builder formatted = Text.builder().append(TextSerializers.FORMATTING_CODE.deserialize(mentionString))
                    .onHover(TextActions.showText(Text.of("Mentioning user " + nick + ".")));
            if (role.isPresent()) {
                formatted = formatted.color(ColorUtil.getColor(role.get().getColor()));
            }
            formattedMentioning.put(mention, formatted);
        }

        // Replace the mention
        for (User mention : mentions) {
            String mentionString = "<@" + mention.getId() + ">";
            texts = replaceMention(texts, mentionString, formattedMentioning.get(mention));
        }

        return texts;
    }

    /**
     * @param texts  The message that may contain Role mentions
     * @param config   The mention config to be used for formatting
     * @param mentions The list of roles mentioned
     * @return The final message with Role mentions formatted
     */
    private static List<Text> formatRoleMentions(List<Text> texts, ChannelMinecraftMentionConfig config, List<Role> mentions) {
        if (mentions.isEmpty()) return texts;

        // Prepare the text builders
        Map<Role, Text.Builder> formattedMentioning = new HashMap<>();
        for (Role mention : mentions) {
            String mentionString = ConfigUtil.get(config.roleTemplate, "@%s").replace("%s", mention.getName());
            Text.Builder builder = Text.builder().append(TextSerializers.FORMATTING_CODE.deserialize(mentionString))
                    .onHover(TextActions.showText(Text.of("Mentioning role " + mention.getName() + ".")));
            if (mention.getColor() != null) {
                builder.color(ColorUtil.getColor(mention.getColor()));
            }
            formattedMentioning.put(mention, builder);
        }

        for (Role mention : mentions) {
            String mentionString = "<@&" + mention.getId() + ">";
            texts = replaceMention(texts, mentionString, formattedMentioning.get(mention));
        }

        return texts;
    }

    /**
     * @param texts The message that may contain everyone mentions
     * @param config  The mention config to be used for formatting
     * @param mention Whether everyone was mentioned
     * @return The final message with everyone mentions formatted
     */
    private static List<Text> formatEveryoneMentions(List<Text> texts, ChannelMinecraftMentionConfig config, boolean mention) {
        if (!mention) return texts;
        String mentionText = ConfigUtil.get(config.everyoneTemplate, "@%s").replace("%s", "everyone");
        Text.Builder builder = Text.builder().append(TextSerializers.FORMATTING_CODE.deserialize(mentionText))
                .onHover(TextActions.showText(Text.of("Mentioning everyone.")));
        texts = replaceMention(texts, "(@(everyone))", builder);

        mentionText = ConfigUtil.get(config.everyoneTemplate, "@%s").replace("%s", "here");
        builder = Text.builder().append(TextSerializers.FORMATTING_CODE.deserialize(mentionText))
                .onHover(TextActions.showText(Text.of("Mentioning online people.")));
        texts = replaceMention(texts, "(@(here))", builder);

        return texts;
    }

    private static Pattern channelPattern = Pattern.compile("<#[0-9]+>");

    /**
     * @param texts The message that may contain everyone mentions
     * @param config  The mention config to be used for formatting
     * @return The final message with everyone mentions formatted
     */
    private static List<Text> formatChannelMentions(List<Text> texts, ChannelMinecraftMentionConfig config) {
        Map<String, Text.Builder> formattedMentions = new HashMap<>();
        for (Text text : texts) {
            String serialized = TextSerializers.FORMATTING_CODE.serialize(text);
            Matcher matcher = channelPattern.matcher(serialized);
            while (matcher.find()) {
                String channelId = serialized.substring(matcher.start() + 2, matcher.end() - 1);
                if (!formattedMentions.containsKey(channelId)) {
                    Channel channel = DiscordBridge.getInstance().getBotClient().getChannelById(channelId);
                    String mentionText = ConfigUtil.get(config.channelTemplate, "#%s").replace("%s", channel.getName());
                    Text.Builder builder = Text.builder().append(TextSerializers.FORMATTING_CODE.deserialize(mentionText))
                            .onHover(TextActions.showText(Text.of("Mentioning channel " + channel.getName() + ".")));
                    formattedMentions.put(channelId, builder);
                }
            }
        }

        for (String channelId : formattedMentions.keySet()) {
            texts = replaceMention(texts, "<#" + channelId + ">", formattedMentions.get(channelId));
        }
        return texts;
    }

    public static List<Text> formatUrl(String message) {
        Preconditions.checkNotNull(message, "message");
        List<Text> texts = Lists.newArrayList();
        if (message.isEmpty()) {
            texts.add(Text.EMPTY);
            return texts;
        }

        Matcher m = urlPattern.matcher(message);
        if (!m.find()) {
            texts.add(TextSerializers.FORMATTING_CODE.deserialize(message));
            return texts;
        }

        String remaining = message;
        StyleTuple st = EMPTY;
        do {

            // We found a URL. We split on the URL that we have.
            String[] textArray = remaining.split(urlPattern.pattern(), 2);
            Text first = Text.builder().color(st.colour).style(st.style)
                    .append(TextSerializers.FORMATTING_CODE.deserialize(textArray[0])).build();

            // Add this text to the list regardless.
            texts.add(first);

            // If we have more to do, shove it into the "remaining" variable.
            if (textArray.length == 2) {
                remaining = textArray[1];
            } else {
                remaining = null;
            }

            // Get the last colour & styles
            String colourMatch = m.group("colour");
            if (colourMatch != null && !colourMatch.isEmpty()) {
                first = TextSerializers.FORMATTING_CODE.deserialize(m.group("colour") + " ");
            }

            st = getLastColourAndStyle(first, st);

            // Build the URL
            String url = m.group("url");
            String toParse = TextSerializers.FORMATTING_CODE.stripCodes(url);
            String whiteSpace = m.group("first");
            texts.add(Text.of(whiteSpace));

            try {
                URL urlObj;
                if (!toParse.startsWith("http://") && !toParse.startsWith("https://")) {
                    urlObj = new URL("http://" + toParse);
                } else {
                    urlObj = new URL(toParse);
                }

                texts.add(Text.builder(url).color(TextColors.DARK_AQUA).style(TextStyles.UNDERLINE)
                        .onHover(TextActions.showText(Text.of("Click to open " + url)))
                        .onClick(TextActions.openUrl(urlObj))
                        .build());
            } catch (MalformedURLException e) {
                // URL parsing failed, just put the original text in here.
                DiscordBridge.getInstance().getLogger().warn("Malform: " + url);
                texts.add(Text.builder(url).color(st.colour).style(st.style).build());
            }
        } while (remaining != null && m.find());

        // Add the last bit.
        if (remaining != null) {
            texts.add(Text.builder().color(st.colour).style(st.style)
                    .append(TextSerializers.FORMATTING_CODE.deserialize(remaining)).build());
        }

        return texts;
    }

    private static List<Text> replaceMention(List<Text> texts, String mentionString, Text.Builder mentionBuilder) {
        List<Text> result = Lists.newArrayList();
        StyleTuple st = EMPTY;
        for (Text text : texts) {
            Text remaining = text;
            while (remaining != null) {
                String serialized = TextSerializers.FORMATTING_CODE.serialize(remaining);
                String[] splitted = serialized.split(mentionString, 2);
                if (splitted.length == 2) {
                    // Add first part
                    Text first = TextSerializers.FORMATTING_CODE.deserialize(splitted[0]);
                    result.add(first);

                    // Add the mention
                    result.add(mentionBuilder.build());

                    // Calculate the remaining
                    st = TextUtil.getLastColourAndStyle(first, st);
                    remaining = Text.builder().color(st.colour).style(st.style)
                            .append(TextSerializers.FORMATTING_CODE.deserialize(splitted[1])).build();
                } else {
                    result.add(remaining);
                    break;
                }
            }
        }
        return result;
    }

    private static StyleTuple getLastColourAndStyle(Text text, StyleTuple current) {
        List<Text> texts = flatten(text);
        TextColor tc = TextColors.NONE;
        TextStyle ts = TextStyles.NONE;
        for (int i = texts.size() - 1; i > -1; i--) {
            // If we have both a Text Colour and a Text Style, then break out.
            if (tc != TextColors.NONE && ts != TextStyles.NONE) {
                break;
            }

            if (tc == TextColors.NONE) {
                tc = texts.get(i).getColor();
            }

            if (ts == TextStyles.NONE) {
                ts = texts.get(i).getStyle();
            }
        }

        if (current == null) {
            return new StyleTuple(tc, ts);
        }

        return new StyleTuple(tc != TextColors.NONE ? tc : current.colour, ts != TextStyles.NONE ? ts : current.style);
    }

    private static List<Text> flatten(Text text) {
        List<Text> texts = Lists.newArrayList(text);
        if (!text.getChildren().isEmpty()) {
            text.getChildren().forEach(x -> texts.addAll(flatten(x)));
        }

        return texts;
    }

    /**
     * @param user
     * @param server
     * @return
     */
    public static Optional<Role> getHighestRole(User user, Server server) {
        int position = 0;
        Optional<Role> highestRole = Optional.empty();
        for (Role role : user.getRoles(server)) {
            if (role.getPosition() > position) {
                position = role.getPosition();
                highestRole = Optional.of(role);
            }
        }
        return highestRole;
    }

    private static final class StyleTuple {
        final TextColor colour;
        final TextStyle style;

        StyleTuple(TextColor colour, TextStyle style) {
            this.colour = colour;
            this.style = style;
        }
    }
}
