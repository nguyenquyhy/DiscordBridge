package com.nguyenquyhy.spongediscord.discord.handle.impl.events;

import com.nguyenquyhy.spongediscord.discord.handle.IEvent;
import com.nguyenquyhy.spongediscord.discord.handle.obj.Message;

/**
 * Created by Hy on 1/11/2016.
 */
public class MessageSendEvent implements IEvent {
    private Message message;

    public MessageSendEvent(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
