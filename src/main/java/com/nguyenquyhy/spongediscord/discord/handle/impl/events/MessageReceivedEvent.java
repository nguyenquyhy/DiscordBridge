package com.nguyenquyhy.spongediscord.discord.handle.impl.events;

import com.nguyenquyhy.spongediscord.discord.handle.IEvent;
import com.nguyenquyhy.spongediscord.discord.handle.obj.Message;

/**
 * Created by Hy on 1/11/2016.
 */
public class MessageReceivedEvent implements IEvent {
    private final Message message;

    public MessageReceivedEvent(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
