package com.nguyenquyhy.discordbridge.models;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

/**
 * Created by Hy on 12/11/2016.
 */
@ConfigSerializable
public class ChannelMinecraftConfigCore {
    void initializeDefault() {
        chatTemplate = "&7<%a> &f%s";
        attachment = new ChannelMinecraftAttachmentConfig();
    }

    @Setting
    public String chatTemplate;
    @Setting
    public ChannelMinecraftAttachmentConfig attachment;
}
