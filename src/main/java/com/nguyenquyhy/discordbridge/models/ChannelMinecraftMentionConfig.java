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
        template = "&6@&3%a";
        userTemplate = "&6@&e%a";
        roleTemplate = "&6@&a%r";
    }

    @Setting
    public String template;
    @Setting
    public String userTemplate;
    @Setting
    public String roleTemplate;

    @Override
    public void inherit(ChannelMinecraftMentionConfig parent) {
        if (template == null) template = parent.template;
        if (userTemplate == null) userTemplate = parent.userTemplate;
        if (roleTemplate == null) roleTemplate = parent.roleTemplate;
    }
}
