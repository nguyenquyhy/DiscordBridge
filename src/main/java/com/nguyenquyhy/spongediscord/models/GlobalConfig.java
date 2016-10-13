package com.nguyenquyhy.spongediscord.models;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hy on 10/13/2016.
 */
@ConfigSerializable
public class GlobalConfig {
    public GlobalConfig() {
        channels = new ArrayList<>();
        tokenStore = TokenStore.JSON;
    }

    @Setting
    public String botToken;
    @Setting
    public TokenStore tokenStore;
    @Setting
    public List<ChannelConfig> channels;
}
