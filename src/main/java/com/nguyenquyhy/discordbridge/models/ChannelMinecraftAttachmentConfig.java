package com.nguyenquyhy.discordbridge.models;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

/**
 * Created by Hy on 12/11/2016.
 */
@ConfigSerializable
public class ChannelMinecraftAttachmentConfig {
    public ChannelMinecraftAttachmentConfig() {
        template = "&3[Attachment]&r";
        hoverTemplate = "Click to open attachment.";
        allowLink = true;
    }

    @Setting
    public String template;
    @Setting
    public String hoverTemplate;
    @Setting
    public boolean allowLink;
}
