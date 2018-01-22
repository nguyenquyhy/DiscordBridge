package com.nguyenquyhy.discordbridge.utils;

import net.dv8tion.jda.core.entities.*;

import java.util.Optional;

public class DiscordUtil {

    /**
     * @param name   The name to search the server for valid a User
     * @param server The server to search through Users
     * @return The User, if any, that matches the name supplied
     */
    static Optional<Member> getMemberByName(String name, Guild server) {
        for (Member member : server.getMembers()) {
            if (member.getEffectiveName().equalsIgnoreCase(name) || (member.getNickname() != null && member.getNickname().equalsIgnoreCase(name))) {
                return Optional.of(member);
            }
        }
        return Optional.empty();
    }

    /**
     * @param name   The name to search the server for valid a Role
     * @param server The server to search through Roles
     * @return The Role, if any, that matches the name supplied
     */
    static Optional<Role> getRoleByName(String name, Guild server) {
        for (Role role : server.getRoles()) {
            if (role.getName().equalsIgnoreCase(name)) {
                return Optional.of(role);
            }
        }
        return Optional.empty();
    }

    /**
     * @param name   The name to search the server for valid a Channel
     * @param server The server to search through Roles
     * @return The Channel, if any, that matches the name supplied
     */
    static Optional<Channel> getChannelByName(String name, Guild server) {
        for (Channel channel : server.getTextChannels()) {
            if (channel.getName().equalsIgnoreCase(name)) {
                return Optional.of(channel);
            }
        }
        return Optional.empty();
    }
}
