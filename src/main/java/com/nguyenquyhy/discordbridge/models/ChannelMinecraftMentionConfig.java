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
        userTemplate = "@%s";
        roleTemplate = "@%s";
        everyoneTemplate = "&6@%s";
        channelTemplate = "&9#%s";
    }

    @Setting
    public String userTemplate;
    @Setting
    public String roleTemplate;
    @Setting
    public String everyoneTemplate;
    @Setting
    public String channelTemplate;

    @Override
    public void inherit(ChannelMinecraftMentionConfig parent) {
        if (userTemplate == null) userTemplate = parent.userTemplate;
        if (roleTemplate == null) roleTemplate = parent.roleTemplate;
        if (everyoneTemplate == null) everyoneTemplate = parent.everyoneTemplate;
        if (channelTemplate == null) channelTemplate = parent.channelTemplate;
    }
}
