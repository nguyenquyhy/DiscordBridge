package com.nguyenquyhy.spongediscord.discord.handle.obj;

import com.nguyenquyhy.spongediscord.discord.DiscordClient;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Created by Hy on 1/11/2016.
 */
public class Message {
    /**
     * The ID of the message. Used for message updating.
     */
    private final String messageID;

    /**
     * The actual message (what you see
     * on your screen, the content).
     */
    private String content;

    private String nonce;

    /**
     * The User who sent the message.
     */
    private final User author;

    /**
     * The ID of the channel the message was sent in.
     */
    private final Channel channel;

    /**
     * The time the message was received.
     */
    private final LocalDateTime timestamp;

    public Message(String messageID, String content, String nonce, User user, Channel channel, LocalDateTime timestamp) {
        this.messageID = messageID;
        this.content = content;
        this.nonce = nonce;
        this.author = user;
        this.channel = channel;
        this.timestamp = timestamp;
    }

    // Getters and Setters. Boring.

    public String getContent() {
        return content;
    }

    public String getNonce() { return nonce; }

    public void setContent(String content) {
        this.content = content;
    }

    public Channel getChannel() {
        return channel;
    }

    public User getAuthor() {
        return author;
    }

    public String getID() {
        return messageID;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Adds an @mention to the author of the referenced Message
     * object before your content
     *
     * @param content Message to send.
     */
    public void reply(String content, String nonce) throws IOException, ParseException {
        DiscordClient.get().sendMessage(String.format("%s, %s", this.getAuthor(), content), nonce, this.getChannel().getID());
    }
}
