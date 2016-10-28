package com.nguyenquyhy.discordbridge.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.nguyenquyhy.discordbridge.DiscordBridge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Hy on 8/29/2016.
 */
public class TextUtil {
    public static final String SPECIAL_CHAR = "\u2062";
    private static final Pattern urlPattern =
            Pattern.compile("(?<first>(^|\\s))(?<colour>(&[0-9a-flmnork])+)?(?<url>(http(s)?://)?([A-Za-z0-9]+\\.)+[A-Za-z0-9]{2,}\\S*)", Pattern.CASE_INSENSITIVE);

    private static final StyleTuple EMPTY = new StyleTuple(TextColors.NONE, TextStyles.NONE);

    public static String formatDiscordEmoji(String message) {
        for (Emoji emoji : Emoji.values()) {
            message = message.replace(emoji.unicode, emoji.minecraftFormat);
        }
        return message;
    }

    public static String formatMinecraftMessage(String message) {
        for (Emoji emoji : Emoji.values()) {
            message = message.replace(emoji.minecraftFormat, emoji.discordFormat);
        }
        return message + SPECIAL_CHAR;
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
