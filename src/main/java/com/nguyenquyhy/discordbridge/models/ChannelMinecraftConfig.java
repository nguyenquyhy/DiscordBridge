package com.nguyenquyhy.discordbridge.models;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Hy on 10/13/2016.
 */
@ConfigSerializable
public class ChannelMinecraftConfig extends ChannelMinecraftConfigCore {
    public ChannelMinecraftConfig() {
        initializeDefault();
    }

    @Override
    void initializeDefault() {
        super.initializeDefault();
        roles = new HashMap<>();
    }

    @Setting
    public Map<String, ChannelMinecraftConfigCore> roles;

}
