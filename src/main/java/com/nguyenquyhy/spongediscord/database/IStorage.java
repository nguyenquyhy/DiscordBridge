package com.nguyenquyhy.spongediscord.database;

import java.util.UUID;

/**
 * Created by Hy on 1/5/2016.
 */
public interface IStorage {
    void putToken(UUID player, String token);
    String getToken(UUID player);
    void removeToken(UUID player);
}
