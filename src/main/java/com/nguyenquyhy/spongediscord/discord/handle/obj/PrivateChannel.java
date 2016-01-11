package com.nguyenquyhy.spongediscord.discord.handle.obj;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hy on 1/11/2016.
 */
public class PrivateChannel extends Channel {
    private final User recipient;

    public PrivateChannel(User recipient, String id) {
        this(recipient, id, new ArrayList<>());
    }

    public PrivateChannel(User recipient, String id, List<Message> messages) {
        super(recipient.getName(), id, null, messages);
        this.recipient = recipient;
        this.isPrivate = true;
    }

    /**
     * Indicates the user with whom you are communicating.
     * @return
     */
    public User getRecipient() {
        return recipient;
    }
}
