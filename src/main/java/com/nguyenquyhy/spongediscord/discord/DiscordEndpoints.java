package com.nguyenquyhy.spongediscord.discord;

/**
 * Created by Hy on 1/11/2016.
 */
public class DiscordEndpoints {
    /**
     * The base URL.
     */
    public static final String BASE = "https://discordapp.com/";
    /**
     * The base API location on Discord's servers.
     */
    public static final String APIBASE = BASE + "api";

    public static final String USERS = APIBASE + "/users/";

    /**
     * Used for logging in.
     */
    public static final String LOGIN = APIBASE + "/auth/login";
    /**
     * Used for logging out.
     */
    public static final String LOGOUT = APIBASE + "/auth/logout";

    /**
     * Servers URL
     */
    public static final String SERVERS = APIBASE + "/guilds/";

    public static final String CHANNELS = APIBASE + "/channels/";

    /**
     * Used for accapting invites
     */
    public static final String INVITE = APIBASE + "/invite/";

    /**
     * Formatted string for getting avatar URLs.
     */
    public static final String AVATARS = "https://cdn.discordapp.com/avatars/%s/%s.jpg";

    /**
     * Formatted string for getting guild icon URLs.
     */
    public static final String ICONS = "https://cdn.discordapp.com/icons/%s/%s.jpg";
}
