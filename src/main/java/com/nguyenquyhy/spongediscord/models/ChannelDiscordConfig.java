package com.nguyenquyhy.spongediscord.models;

import ninja.leaping.configurate.objectmapping.Setting;

/**
 * Created by Hy on 10/13/2016.
 */
public class ChannelDiscordConfig {
    public ChannelDiscordConfig() {
        joinedTemplate = "_%s just joined the server_";
        leftTemplate = "_%s just left the server_";
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
