package com.nguyenquyhy.discordbridge.models;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.List;

/**
 * Created by Hy on 10/13/2016.
 */
@ConfigSerializable
public class ChannelDiscordConfig {
    void initializeDefault() {
        joinedTemplate = "_%s just joined the server_";
        leftTemplate = "_%s just left the server_";
        publicChat = new SpongeChannelConfig();
        publicChat.authenticatedChatTemplate = "%s";
        publicChat.anonymousChatTemplate = "`%a:` %s";
        serverUpMessage = "Server has started.";
        serverDownMessage = "Server has stopped.";
        broadcastTemplate = "_<BROADCAST> %s_";
    }

    @Setting
    public String joinedTemplate;
    @Setting
    public String leftTemplate;
    @Setting
    public SpongeChannelConfig publicChat;
    @Setting
    public SpongeChannelConfig staffChat;
    @Setting
    public List<SpongeChannelConfig> clansChat;
    @Setting
    public String serverUpMessage;
    @Setting
    public String serverDownMessage;
    @Setting
    public String broadcastTemplate;
}
