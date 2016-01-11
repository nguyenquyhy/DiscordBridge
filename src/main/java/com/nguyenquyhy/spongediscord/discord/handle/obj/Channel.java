package com.nguyenquyhy.spongediscord.discord.handle.obj;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hy on 1/11/2016.
 */
public class Channel {
    /**
     * User-friendly channel name (e.g. "general")
     */
    protected String name;

    /**
     * Channel ID.
     */
    protected final String id;

    /**
     * Messages that have been sent into this channel
     */
    protected final List<Message> messages;

    /**
     * Indicates whether or not this channel is a PM channel.
     */
    protected boolean isPrivate;

    /**
     * The guild this channel belongs to.
     */
    private final Guild parent;

    public Channel(String name, String id, Guild parent) {
        this(name, id, parent, new ArrayList<>());
    }

    public Channel(String name, String id, Guild parent, List<Message> messages) {
        this.name = name;
        this.id = id;
        this.messages = messages;
        this.parent = parent;
        this.isPrivate = false;
    }

    // Getters.

    public String getName() {
        return name;
    }

    public String getID() {
        return id;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public Message getMessageByID(String messageID) {
        for (Message message : messages) {
            if (message.getID().equalsIgnoreCase(messageID))
                return message;
        }

        return null;
    }

    public void addMessage(Message message) {
        if (message.getChannel().getID().equalsIgnoreCase(this.getID())) {
            messages.add(message);
        }
    }

    public Guild getParent() {
        return parent;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    // still STOLEN from hydrabolt :P
    public String mention() {
        return "<#" + this.getID() + ">";
    }

    @Override public String toString() {
        return mention();
    }
}
