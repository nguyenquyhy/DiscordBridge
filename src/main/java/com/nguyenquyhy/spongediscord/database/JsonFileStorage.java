package com.nguyenquyhy.spongediscord.database;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by Hy on 1/6/2016.
 */
public class JsonFileStorage implements IStorage {
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
            configNode.getNode("tokens").setValue(new HashMap<UUID, String>());
            configLoader.save(configNode);
        }
        configNode = configLoader.load();
    }

    @Override
    public void putToken(UUID player, String token) throws IOException {
        configNode.getNode("tokens").getNode(player.toString()).setValue(token);
        configLoader.save(configNode);
    }

    @Override
    public String getToken(UUID player) {
        return configNode.getNode("tokens").getNode(player.toString()).getString();
    }

    @Override
    public void removeToken(UUID player) throws IOException {
        configNode.getNode("tokens").removeChild(player.toString());
        configLoader.save(configNode);
    }
}
