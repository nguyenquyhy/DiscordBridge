package com.nguyenquyhy.discordbridge.models;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

/**
 * Created by Hy on 12/11/2016.
 */
@ConfigSerializable
public class ChannelMinecraftMentionConfig implements IConfigInheritable<ChannelMinecraftMentionConfig> {
    public ChannelMinecraftMentionConfig() {

    }

    /**
     * This is called only when the config file is first created.
     */
    void initializeDefault() {
        userTemplate = "&6@&e%a";
        roleTemplate = "&6@&a%r";
        everyoneTemplate = "&6@&3%a";
    }

    @Setting
    public String userTemplate;
    @Setting
    public String roleTemplate;
    @Setting
    public String everyoneTemplate;

    @Override
    public void inherit(ChannelMinecraftMentionConfig parent) {
        if (userTemplate == null) userTemplate = parent.userTemplate;
        if (roleTemplate == null) roleTemplate = parent.roleTemplate;
        if (everyoneTemplate == null) everyoneTemplate = parent.everyoneTemplate;
    }
}
