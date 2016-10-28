package com.nguyenquyhy.discordbridge.models;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

/**
 * Created by Hy on 10/29/2016.
 */
@ConfigSerializable
public class SpongeChannelConfig {
    @Setting
    public String anonymousChatTemplate;
    @Setting
    public String authenticatedChatTemplate;
}
