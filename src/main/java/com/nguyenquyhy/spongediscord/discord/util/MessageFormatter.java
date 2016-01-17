package com.nguyenquyhy.spongediscord.discord.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Hy on 1/16/2016.
 */
public class MessageFormatter {

    private static final Map<String, String> emojiMinecraftToDiscord;
    private static final Map<String, String> emojiDiscordToMinecraft;

    static {
        emojiMinecraftToDiscord = Collections.unmodifiableMap(new HashMap<String, String>() {{
            put(":)", ":smiley:");
            put(":)", ":smiley:");
            put(":D", ":smile:");
            put(":d", ":smile:");
            put(";D", ":joy:");
            put(";d", ":joy:");
            put("xD", ":laughing:");
            put("XD", ":laughing:");
            put(":(", ":frowning:");
            put(";(", ":sob:");
            put("x(", ":tired_face:");
            put("X(", ":tired_face:");
            put(";)", ":wink:");
            put(":P", ":stuck_out_tongue:");
            put(":p", ":stuck_out_tongue:");
            put("xP", ":stuck_out_tongue_closed_eyes:");
            put(";P", ":stuck_out_tongue_winking_eye:");
            put(";p", ":stuck_out_tongue_winking_eye:");
            put(":O", ":open_mouth:");
            put(":o", ":open_mouth:");
            put("xO", ":dizzy_face:");
            put(":|", ":neutral_face:");
            put("<3", ":heart:");
            put("B)", ":sunglasses:");
            put(":*", ":kissing:");
        }});

        emojiDiscordToMinecraft = Collections.unmodifiableMap(new HashMap<String, String>() {{
            put("\ud83d\ude03", ":)");
            put("\ud83d\ude04", ":D");
            put("\ud83d\ude02", ";D");
            put("\ud83d\ude06", "xD");
            put("\ud83d\ude26", ":(");
            put("\ud83d\ude2d", ";(");
            put("\ud83d\ude2b", "x(");
            put("\ud83d\ude09", ";)");
            put("\ud83d\ude1b", ":P");
            put("\ud83d\ude1c", ";P");
            put("\ud83d\ude1d", "xP");
            put("\ud83d\ude2e", ":O");
            put("\ud83d\ude35", "xO");
            put("\ud83d\ude10", ":|");
            put("\ud83d\ude0e", "B)");
            put("\ud83d\ude17", ":*");
            put("\u2764", "<3");
        }});
    }

    public static String convertMinecraftToDiscord(String message) {
        message = " " + message + " ";
        for (String key : emojiMinecraftToDiscord.keySet()) {
            message = message.replace(" " + key + " ", emojiMinecraftToDiscord.get(key).replace(":", "š"));
        }
        message = message.replace("š", ":").trim();
        return message;
    }

    public static String convertDiscordToMinecraft(String message) {
        for (String key : emojiDiscordToMinecraft.keySet()) {
            message = message.replace(key, emojiDiscordToMinecraft.get(key));
        }
        return message;
    }
}
