package com.nguyenquyhy.discordbridge.models;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

/**
 * Created by Hy on 12/11/2016.
 */
@ConfigSerializable
public class ChannelMinecraftConfigCore implements IConfigInheritable<ChannelMinecraftConfigCore> {
    void initializeDefault() {
        chatTemplate = "&7<%a> &f%s";
        attachment = new ChannelMinecraftAttachmentConfig();
        attachment.initializeDefault();
        emoji = new ChannelMinecraftEmojiConfig();
        emoji.initializeDefault();
        mention = new ChannelMinecraftMentionConfig();
        mention.initializeDefault();
    }

    @Setting
    public String chatTemplate;
    @Setting
    public ChannelMinecraftAttachmentConfig attachment;
    @Setting
    public ChannelMinecraftEmojiConfig emoji;
    @Setting
    public ChannelMinecraftMentionConfig mention;

    @Override
    public void inherit(ChannelMinecraftConfigCore parent) {
        if (chatTemplate == null) chatTemplate = parent.chatTemplate;
        if (attachment == null) attachment = parent.attachment;
        else attachment.inherit(parent.attachment);
        if (mention == null) mention = parent.mention;
        else mention.inherit(parent.mention);
    }
}
