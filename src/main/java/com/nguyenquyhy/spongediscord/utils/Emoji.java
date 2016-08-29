package com.nguyenquyhy.spongediscord.utils;

/**
 * Created by Hy on 8/29/2016.
 */
public enum Emoji {
    Smiley(":smiley:", ":)", "\ud83d\ude03"),
    Smile(":smile:", ":D", "\ud83d\ude04"),
    Joy(":joy:", ";D", "\ud83d\ude02"),
    Laughing(":laughing:", "xD", "\ud83d\ude06"),
    Frowning(":frowning:", ":(", "\ud83d\ude26"),
    Sob(":sob:", ";(", "\ud83d\ude2d"),
    TiredFace(":tired_face:", "x(", "\ud83d\ude2b"),
    Wink(":wink:", ";)", "\ud83d\ude09"),
    StuckOutTongue(":stuck_out_tongue:", ":P", "\ud83d\ude1b"),
    StuckOutTongueWinkingEye(":stuck_out_tongue_winking_eye:", ";P", "\ud83d\ude1c"),
    StuckOutTongueClosedEyes(":stuck_out_tongue_closed_eyes:", "xP", "\ud83d\ude1d"),
    OpenMouth(":open_mouth:", ":O", "\ud83d\ude2e"),
    DizzyFace(":dizzy_face:", "xO", "\ud83d\ude35"),
    NeutralFace(":neutral_face:", ":|", "\ud83d\ude10"),
    Sunglasses(":sunglasses:", "B)", "\ud83d\ude0e"),
    Kissing(":kissing:", ":*", "\ud83d\ude17"),
    Heart(":heart:", "<3", "\u2764");

    public final String discordFormat;
    public final String minecraftFormat;
    public final String unicode;

    Emoji(String discordFormat, String minecraftFormat, String unicode) {
        this.discordFormat = discordFormat;
        this.minecraftFormat = minecraftFormat;
        this.unicode = unicode;
    }
}
