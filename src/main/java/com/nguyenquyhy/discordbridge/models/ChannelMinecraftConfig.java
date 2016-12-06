package com.nguyenquyhy.discordbridge.models;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hy on 10/13/2016.
 */
@ConfigSerializable
public class ChannelMinecraftConfig {
    void initializeDefault() {
        chatTemplate = "&7<%a> &f%s";
        attachmentTemplate = "[Attachment]";
        attachmentColor = "&3";
        attachmentHoverTemplate = "Click to open attachment.";
        defaultRole = "Member";
        roleBlacklist = new ArrayList<>();
    }

    @Setting
    public String chatTemplate;
    @Setting
    public String attachmentTemplate;
    @Setting
    public String attachmentColor;
    @Setting
    public String attachmentHoverTemplate;
    @Setting
    public String defaultRole;
    @Setting
    public List<String> roleBlacklist;
}
