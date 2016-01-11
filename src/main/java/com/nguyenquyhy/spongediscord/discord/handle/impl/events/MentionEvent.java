package com.nguyenquyhy.spongediscord.discord.handle.impl.events;

import com.nguyenquyhy.spongediscord.discord.handle.IEvent;
import com.nguyenquyhy.spongediscord.discord.handle.obj.Message;

/**
 * Created by nguye on 1/11/2016.
 */
public class MentionEvent implements IEvent {
    private final Message message;

    public MentionEvent(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
