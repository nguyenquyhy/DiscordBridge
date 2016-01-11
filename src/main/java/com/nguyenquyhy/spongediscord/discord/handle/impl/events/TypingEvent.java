package com.nguyenquyhy.spongediscord.discord.handle.impl.events;

import com.nguyenquyhy.spongediscord.discord.handle.IEvent;
import com.nguyenquyhy.spongediscord.discord.handle.obj.Channel;
import com.nguyenquyhy.spongediscord.discord.handle.obj.User;

/**
 * Created by Hy on 1/11/2016.
 */
public class TypingEvent implements IEvent {
    private final User user;
    private final Channel channel;

    public TypingEvent(User user, Channel channel) {
        this.user = user;
        this.channel = channel;
    }

    public User getUser() {
        return user;
    }

    public Channel getChannel() {
        return channel;
    }
}
