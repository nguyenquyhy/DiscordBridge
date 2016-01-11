package com.nguyenquyhy.spongediscord.discord.handle.impl.events;

import com.nguyenquyhy.spongediscord.discord.handle.IEvent;
import com.nguyenquyhy.spongediscord.discord.handle.obj.Channel;

/**
 * Created by nguye on 1/11/2016.
 */
public class ChannelCreateEvent implements IEvent {
    private final Channel channel;

    public ChannelCreateEvent(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }
}
