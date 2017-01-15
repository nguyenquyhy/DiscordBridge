package com.nguyenquyhy.discordbridge.utils;

import com.google.common.collect.Lists;
import com.nguyenquyhy.discordbridge.models.ChannelMinecraftMentionConfig;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.permissions.Role;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DiscordUtil {

    /**
     * @param name   The name to search the server for valid a User
     * @param server The server to search through Users
     * @return The User, if any, that matches the name supplied
     */
    static Optional<User> getUserByName(String name, Server server) {
        for (User user : server.getMembers()) {
            if (user.getName().equalsIgnoreCase(name) || (user.getNickname(server) != null && user.getNickname(server).equalsIgnoreCase(name))) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    /**
     * @param name   The name to search the server for valid a Role
     * @param server The server to search through Roles
     * @return The Role, if any, that matches the name supplied
     */
    static Optional<Role> getRoleByName(String name, Server server) {
        for (Role role : server.getRoles()) {
            if (role.getName().equalsIgnoreCase(name)) {
                return Optional.of(role);
            }
        }
        return Optional.empty();
    }
}
