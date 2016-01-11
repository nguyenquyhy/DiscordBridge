package com.nguyenquyhy.spongediscord.discord.handle.impl.events;

import com.nguyenquyhy.spongediscord.discord.handle.IEvent;
import com.nguyenquyhy.spongediscord.discord.handle.obj.Invite;
import com.nguyenquyhy.spongediscord.discord.handle.obj.Message;

/**
 * Created by nguye on 1/11/2016.
 */
public class InviteReceivedEvent implements IEvent {
    private final Invite invite;
    private final Message message;

    public InviteReceivedEvent(Invite invite, Message message) {
        this.invite = invite;
        this.message = message;
    }

    public Invite getInvite() {
        return invite;
    }

    public Message getMessage() {
        return message;
    }
}
