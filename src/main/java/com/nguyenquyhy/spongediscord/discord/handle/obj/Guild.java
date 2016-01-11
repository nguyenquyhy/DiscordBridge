package com.nguyenquyhy.spongediscord.discord.handle.obj;

import com.nguyenquyhy.spongediscord.discord.DiscordEndpoints;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hy on 1/11/2016.
 */
public class Guild {
    /**
     * All text channels in the guild.
     */
    private final List<Channel> channels;

    /**
     * All users connected to the guild.
     */
    private final List<User> users;

    /**
     * The name of the guild.
     */
    private final String name;

    /**
     * The ID of this guild.
     */
    private final String id;

    /**
     * The location of the guild icon
     */
    private String icon;

    /**
     * The url pointing to the guild icon
     */
    private String iconURL;

    /**
     * The user id for the owner of the guild
     */
    private final String ownerID;

    public Guild(String name, String id, String icon, String ownerID) {
        this.name = name;
        this.id = id;
        this.channels = new ArrayList<>();
        this.users = new ArrayList<>();
        this.icon = icon;
        this.iconURL = String.format(DiscordEndpoints.ICONS, this.id, this.icon);
        this.ownerID = ownerID;
    }

    public Guild(String name, String id, String icon, String ownerID, List<Channel> channels, List<User> users) {
        this.name = name;
        this.channels = channels;
        this.users = users;
        this.id = id;
        this.icon = icon;
        this.iconURL = String.format(DiscordEndpoints.ICONS, this.id, this.icon);
        this.ownerID = ownerID;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public String getIcon() {
        return icon;
    }

    public String getIconURL() {
        return iconURL;
    }

    public void setIcon(String icon) {
        this.icon = icon;
        this.iconURL = String.format(DiscordEndpoints.ICONS, this.id, this.icon);
    }

    /**
     * @return All channels on the server.
     */
    public List<Channel> getChannels() {
        return channels;
    }

    /**
     * @param id The ID of the channel you want to find.
     * @return The channel with given ID.
     */
    public Channel getChannelByID(String id) {
        for (Channel c : channels) {
            if (c.getID().equalsIgnoreCase(id))
                return c;
        }

        return null; // Not found, return null.
    }

    /**
     * @return All users connected to the guild.
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     * @param id ID of the user you want to find.
     * @return The user with given ID.
     */
    public User getUserByID(String id) {
        if(null == users)
            return null;
        for (User user : users) {
            if (null != user
                    && null != user.getID()
                    && user.getID().equalsIgnoreCase(id))
                return user;
        }

        return null; // Not found, return null.
    }

    /**
     * @return The name of the guild
     */
    public String getName() {
        return name;
    }

    /**
     * @return The ID of this guild.
     */
    public String getID() {
        return id;
    }

    public void addUser(User user) {
        this.users.add(user);
    }

    public void addChannel(Channel channel) {
        this.channels.add(channel);
    }
}
