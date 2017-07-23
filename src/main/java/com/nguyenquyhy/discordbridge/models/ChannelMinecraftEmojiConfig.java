package com.nguyenquyhy.discordbridge.models;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

/**
 * Created by Hy on 12/11/2016.
 */
@ConfigSerializable
public class ChannelMinecraftEmojiConfig implements IConfigInheritable<ChannelMinecraftEmojiConfig> {
    public ChannelMinecraftEmojiConfig() {

    }

    /**
     * This is called only when the config file is first created.
     */
    void initializeDefault() {
        template = "&b:%n:&r";
        hoverTemplate = "Click to view emjoi.";
        allowLink = true;
    }

    @Setting
    public String template;
    @Setting
    public String hoverTemplate;
    @Setting
    public Boolean allowLink;

    @Override
    public void inherit(ChannelMinecraftEmojiConfig parent) {
        if (template == null) template = parent.template;
        if (hoverTemplate == null) hoverTemplate = parent.hoverTemplate;
        if (allowLink == null) allowLink = parent.allowLink;
    }
}
