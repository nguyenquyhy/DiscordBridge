package com.nguyenquyhy.discordbridge.database;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Hy on 1/5/2016.
 */
public class InMemoryStorage implements IStorage {
    private final Map<UUID, String> tokens = new HashMap<>();
    @SuppressWarnings("unused")
	private String defaultToken = null;

    @Override
    public void putToken(UUID player, String token) {
        tokens.put(player, token);
    }

    @Override
    public String getToken(UUID player) {
        if (tokens.containsKey(player))
            return tokens.get(player);
        else
            return null;
    }

    @Override
    public void removeToken(UUID player) {
        tokens.remove(player);
    }
}
