package com.nguyenquyhy.spongediscord.discord.handle.obj;

import com.nguyenquyhy.spongediscord.discord.DiscordEndpoints;
import com.nguyenquyhy.spongediscord.discord.util.Presences;

import java.util.Optional;

/**
 * Created by Hy on 1/11/2016.
 */
public class User {
    /**
     * Display name of the user.
     */
    private String name;

    /**
     * The user's avatar location.
     */
    private String avatar;

    /**
     * The id of the game the user is playing, either null or a Long id
     */
    private Long gameID;

    /**
     * User ID.
     */
    private final String id;

    /**
     * User discriminator.
     * Distinguishes users with the same name.
     * <p>
     * This is here in case it becomes necessary.
     */
    private int discriminator;

    /**
     * This user's presence.
     * One of [online/idle/offline].
     */
    private Presences presence;

    /**
     * The user's avatar in URL form.
     */
    private String avatarURL;

    public User(String name, String id, String avatar) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.avatarURL = String.format(DiscordEndpoints.AVATARS, this.id, this.avatar);
    }

    // -- Getters and setters. Pretty boring.

    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Optional<Long> getGameID() {
        return Optional.ofNullable(gameID);
    }

    public void setGameID(Long gameID) {
        this.gameID = gameID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getAvatarURL() {
        return avatarURL;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
        this.avatarURL = String.format(DiscordEndpoints.AVATARS, this.id, this.avatar);
    }

    public Presences getPresence() {
        return presence;
    }

    public void setPresence(Presences presence) {
        this.presence = presence;
    }

    // STOLEN: idea from hydrabolt :P
    public String mention() {
        return "<@" + id + ">";
    }

    @Override public String toString() {
        return mention();
    }
}
