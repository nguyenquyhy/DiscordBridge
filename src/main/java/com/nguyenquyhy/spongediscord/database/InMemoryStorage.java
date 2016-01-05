package com.nguyenquyhy.spongediscord.database;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Hy on 1/5/2016.
 */
public class InMemoryStorage implements IStorage {
    private Map<UUID, String> tokens = new HashMap<UUID, String>();

    public void putToken(UUID player, String token) {
        tokens.put(player, token);
    }

    public String getToken(UUID player) {
        if (tokens.containsKey(player))
            return tokens.get(player);
        else
            return null;
    }

    public void removeToken(UUID player) {
        tokens.remove(player);
    }
}
