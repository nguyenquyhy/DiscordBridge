package com.nguyenquyhy.discordbridge.models;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

/**
 * Created by Hy on 12/11/2016.
 */
@ConfigSerializable
public class ChannelMinecraftAttachmentConfig implements IConfigInheritable<ChannelMinecraftAttachmentConfig> {
    public ChannelMinecraftAttachmentConfig() {

    }

    /**
     * This is called only when the config file is first created.
     */
    void initializeDefault() {
        template = "&3[Attachment]&r";
        hoverTemplate = "Click to open attachment.";
        allowLink = true;
    }

    @Setting
    public String template;
    @Setting
    public String hoverTemplate;
    @Setting
    public Boolean allowLink;

    @Override
    public void inherit(ChannelMinecraftAttachmentConfig parent) {
        if (template == null) template = parent.template;
        if (hoverTemplate == null) hoverTemplate = parent.hoverTemplate;
        if (allowLink == null) allowLink = parent.allowLink;
    }
}
