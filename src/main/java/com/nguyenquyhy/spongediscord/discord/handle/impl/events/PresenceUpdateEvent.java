package com.nguyenquyhy.spongediscord.discord.handle.impl.events;

import com.nguyenquyhy.spongediscord.discord.handle.IEvent;
import com.nguyenquyhy.spongediscord.discord.handle.obj.Guild;
import com.nguyenquyhy.spongediscord.discord.handle.obj.User;
import com.nguyenquyhy.spongediscord.discord.util.Presences;

/**
 * Created by Hy on 1/11/2016.
 */
public class PresenceUpdateEvent implements IEvent {
    private final Guild guild;
    private final User user;
    private final Presences oldPresence, newPresence;

    public PresenceUpdateEvent(Guild guild, User user, Presences oldPresence, Presences newPresence) {
        this.guild = guild;
        this.user = user;
        this.oldPresence = oldPresence;
        this.newPresence = newPresence;
    }

    public Presences getNewPresence() {
        return newPresence;
    }

    public Presences getOldPresence() {
        return oldPresence;
    }

    public User getUser() {
        return user;
    }

    public Guild getGuild() {
        return guild;
    }
}
