package com.nguyenquyhy.spongediscord.database;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Hy on 1/6/2016.
 */
public class JsonFileStorage implements IStorage {
    private final String DEFAULT_NODE = "DEFAULT";

    private ConfigurationLoader<? extends ConfigurationNode> configLoader;
    private ConfigurationNode configNode;

    public JsonFileStorage(Path configDir) throws IOException {
        Path configFile = Paths.get(configDir + "/tokens.json");
        configLoader = GsonConfigurationLoader.builder()
                .setPath(configFile)
                .setIndent(4)
                .setLenient(true)
                .build();

        if (!Files.exists(configFile)) {
            Files.createFile(configFile);
            configNode = configLoader.load();
            getCachedTokens().setValue(new HashMap<UUID, String>());
            configLoader.save(configNode);
        }
        configNode = configLoader.load();
    }

    @Override
    public void putToken(UUID player, String token) throws IOException {
        getCachedTokens().getNode(player.toString()).setValue(token);
        configLoader.save(configNode);
    }

    @Override
    public String getToken(UUID player) {
        return getCachedTokens().getNode(player.toString()).getString();
    }

    @Override
    public void removeToken(UUID player) throws IOException {
        getCachedTokens().removeChild(player.toString());
        configLoader.save(configNode);
    }

    @Override
    public void putDefaultToken(String token) throws IOException {
        configNode.getNode(DEFAULT_NODE).setValue(token);
        configLoader.save(configNode);
    }

    @Override
    public String getDefaultToken() {
        return configNode.getNode(DEFAULT_NODE).getString();
    }

    @Override
    public void removeDefaultToken() throws IOException {
        configNode.removeChild(DEFAULT_NODE);
        configLoader.save(configNode);
    }

    private ConfigurationNode getCachedTokens() {
        return configNode.getNode("tokens");
    }
}
