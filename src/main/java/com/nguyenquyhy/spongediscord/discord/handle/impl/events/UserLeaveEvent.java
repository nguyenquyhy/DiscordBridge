package com.nguyenquyhy.spongediscord.discord.handle.impl.events;

import com.nguyenquyhy.spongediscord.discord.handle.IEvent;
import com.nguyenquyhy.spongediscord.discord.handle.obj.Guild;
import com.nguyenquyhy.spongediscord.discord.handle.obj.User;

/**
 * Created by Hy on 1/11/2016.
 */
public class UserLeaveEvent implements IEvent {
    private final Guild guild;
    private final User user;

    public UserLeaveEvent(Guild guild, User user) {
        this.guild = guild;
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public Guild getGuild() {
        return guild;
    }
}
