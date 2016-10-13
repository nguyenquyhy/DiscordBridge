package com.nguyenquyhy.spongediscord.models;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

/**
 * Created by Hy on 10/13/2016.
 */
@ConfigSerializable
public class ChannelConfig {
    public ChannelConfig() {
        discord = new ChannelDiscordConfig();
        minecraft = new ChannelMinecraftConfig();

        discordId = "";
    }

    @Setting
    public String discordId;
    @Setting
    public String discordInviteCode;
    @Setting
    public ChannelDiscordConfig discord;
    @Setting
    public ChannelMinecraftConfig minecraft;
}
