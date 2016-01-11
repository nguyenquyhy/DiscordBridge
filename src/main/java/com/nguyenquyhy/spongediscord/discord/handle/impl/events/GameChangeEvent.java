package com.nguyenquyhy.spongediscord.discord.handle.impl.events;

import com.nguyenquyhy.spongediscord.discord.handle.IEvent;
import com.nguyenquyhy.spongediscord.discord.handle.obj.Guild;
import com.nguyenquyhy.spongediscord.discord.handle.obj.User;

/**
 * Created by Hy on 1/11/2016.
 */
public class GameChangeEvent implements IEvent {
    private final Guild guild;
    private final User user;
    private final Long oldGameID, newGameID;

    public GameChangeEvent(Guild guild, User user, Long oldGameID, Long newGameID) {
        this.guild = guild;
        this.user = user;
        this.oldGameID = oldGameID;
        this.newGameID = newGameID;
    }

    public long getNewGameID() {
        return newGameID;
    }

    public User getUser() {
        return user;
    }

    public long getOldGameID() {
        return oldGameID;
    }

    public Guild getGuild() {
        return guild;
    }
}
