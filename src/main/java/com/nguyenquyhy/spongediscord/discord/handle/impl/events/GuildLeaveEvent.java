package com.nguyenquyhy.spongediscord.discord.handle.impl.events;

import com.nguyenquyhy.spongediscord.discord.handle.IEvent;
import com.nguyenquyhy.spongediscord.discord.handle.obj.Guild;

/**
 * Created by Hy on 1/11/2016.
 */
public class GuildLeaveEvent implements IEvent {
    private final Guild guild;

    public GuildLeaveEvent(Guild guild) {
        this.guild = guild;
    }

    public Guild getGuild() {
        return guild;
    }
}
