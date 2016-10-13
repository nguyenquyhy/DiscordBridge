package com.nguyenquyhy.spongediscord.models;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

/**
 * Created by Hy on 10/13/2016.
 */
@ConfigSerializable
public class ChannelDiscordConfig {
    public void initializeDefault() {
        joinedTemplate = "_%s just joined the server_";
        leftTemplate = "_%s just left the server_";
        authenticatedChatTemplate = "%s";
        anonymousChatTemplate = "`<%a>` %s";
        serverUpMessage = "Server has started.";
        serverDownMessage = "Server has stopped.";
    }

    @Setting
    public String joinedTemplate;
    @Setting
    public String leftTemplate;
    @Setting
    public String anonymousChatTemplate;
    @Setting
    public String authenticatedChatTemplate;
    @Setting
    public String serverUpMessage;
    @Setting
    public String serverDownMessage;
}
