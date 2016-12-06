package com.nguyenquyhy.discordbridge.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.nguyenquyhy.discordbridge.DiscordBridge;
import com.nguyenquyhy.discordbridge.models.ChannelConfig;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.permissions.Role;
import org.spongepowered.api.text.Text;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Hy on 8/29/2016.
 */
public class TextUtil {
    private static final Pattern urlPattern =
            Pattern.compile("(?<first>(^|\\s))(?<colour>(&[0-9a-flmnork])+)?(?<url>(http(s)?://)?([A-Za-z0-9]+\\.)+[A-Za-z0-9]{2,}\\S*)", Pattern.CASE_INSENSITIVE);

    private static final StyleTuple EMPTY = new StyleTuple(TextColors.NONE, TextStyles.NONE);

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

    public static String formatForMinecraft(ChannelConfig config, Message message) {
        // Replace %a with Message author's Name
        String s = config.minecraft.chatTemplate.replace("%a", message.getAuthor().getName());

        // Replace %n with Message author's Nickname (Not yet supported)
        //s = s.replace("%n", message.getAuthor().getNickname());

        // Get author's highest role
        int position = 0;
        String roleName = config.minecraft.defaultRole;
        Color roleColor = Color.WHITE;
        for (Role role: message.getAuthor().getRoles(message.getChannelReceiver().getServer())){
            if (role.getPosition() > position && !config.minecraft.roleBlacklist.contains(role.getName())) {
                position = role.getPosition();
                roleName = role.getName();
                roleColor = role.getColor();
            }
        }
        // Replace %r with Message author's highest role
        s = s.replace("%r", roleName);
        // Replace %c with Message MC color code if compatible with author's highest role color
        s = s.replace("%c", ColorUtil.getColorCode(roleColor));
        // Replace %g with Message author's game
        String game = message.getAuthor().getGame();
        if (game != null) s = s.replace("%g", game);

        s = String.format(s, message.getContent());

        // Replace Mentions with readable names
        for (User mention: message.getMentions()) {
            s = s.replace("<@"+mention.getId()+">","@" + mention.getName());
            s = s.replace("<@!"+mention.getId()+">","@" + mention.getName()); // Change to getNickname() when supported
        }

        return TextUtil.formatDiscordMessage(s);
    }

    public static Text formatUrl(String message) {
        Preconditions.checkNotNull(message, "message");
        if (message.isEmpty()) {
            return Text.EMPTY;
        }

        Matcher m = urlPattern.matcher(message);
        if (!m.find()) {
            return TextSerializers.FORMATTING_CODE.deserialize(message);
        }

        List<Text> texts = Lists.newArrayList();
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

        // Join it all together.
        return Text.join(texts);
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

    private static final class StyleTuple {
        final TextColor colour;
        final TextStyle style;

        StyleTuple(TextColor colour, TextStyle style) {
            this.colour = colour;
            this.style = style;
        }
    }
}
