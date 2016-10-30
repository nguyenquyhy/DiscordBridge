package com.nguyenquyhy.discordbridge.models;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

/**
 * Created by Hy on 10/13/2016.
 */
@ConfigSerializable
public class ChannelConfig {
    public void initializeDefault() {
        discordId = "";
        discord = new ChannelDiscordConfig();
        discord.initializeDefault();
        minecraft = new ChannelMinecraftConfig();
        minecraft.initializeDefault();
    }

    @Setting
    public String discordId;
    @Setting
    public ChannelDiscordConfig discord;
    @Setting
    public ChannelMinecraftConfig minecraft;

    public void migrate() {
        if (discord != null)
            discord.migrate();
    }
}
