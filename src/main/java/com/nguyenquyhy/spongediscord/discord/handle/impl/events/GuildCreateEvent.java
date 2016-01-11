package com.nguyenquyhy.spongediscord.discord.handle.impl.events;

import com.nguyenquyhy.spongediscord.discord.handle.IEvent;
import com.nguyenquyhy.spongediscord.discord.handle.obj.Guild;

/**
 * Created by Hy on 1/11/2016.
 */
public class GuildCreateEvent implements IEvent {
    private final Guild guild;

    public GuildCreateEvent(Guild guild) {
        this.guild = guild;
    }

    public Guild getGuild() {
        return guild;
    }
}
