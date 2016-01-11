package com.nguyenquyhy.spongediscord.discord.handle.impl.events;

import com.nguyenquyhy.spongediscord.discord.handle.IEvent;
import com.nguyenquyhy.spongediscord.discord.handle.obj.Guild;
import com.nguyenquyhy.spongediscord.discord.handle.obj.User;

import java.time.LocalDateTime;

/**
 * Created by nguye on 1/11/2016.
 */
public class UserJoinEvent implements IEvent {
    private final Guild guild;
    private final LocalDateTime joinTime;
    private final User userJoined;

    public UserJoinEvent(Guild guild, User user, LocalDateTime when) {
        this.guild = guild;
        this.joinTime = when;
        this.userJoined = user;
    }

    public LocalDateTime getJoinTime() {
        return joinTime;
    }

    public User getUser() {
        return userJoined;
    }

    public Guild getGuild() {
        return guild;
    }
}
