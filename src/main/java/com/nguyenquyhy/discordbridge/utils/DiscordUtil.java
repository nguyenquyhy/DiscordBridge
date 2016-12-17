package com.nguyenquyhy.discordbridge.utils;

import com.nguyenquyhy.discordbridge.models.ChannelMinecraftMentionConfig;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.permissions.Role;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

public class DiscordUtil {

    /**
     * @param name   The name to search the server for valid a User
     * @param server The server to search through Users
     * @return The User, if any, that matches the name supplied
     */
    public static Optional<User> getUserByName(String name, Server server) {
        for (User user : server.getMembers()) {
            if (user.getName().equalsIgnoreCase(name) || (user.getNickname(server.getId()) != null && user.getNickname(server.getId()).equalsIgnoreCase(name))) {
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
    public static Optional<Role> getRoleByName(String name, Server server) {
        for (Role role : server.getRoles()) {
            if (role.getName().equalsIgnoreCase(name)) {
                return Optional.of(role);
            }
        }
        return Optional.empty();
    }

    /**
     * @param user
     * @param server
     * @return
     */
    public static Optional<Role> getHighestRole(User user, Server server) {
        int position = 0;
        Optional<Role> highestRole = Optional.empty();
        for (Role role : user.getRoles(server)) {
            if (role.getPosition() > position) {
                position = role.getPosition();
                highestRole = Optional.of(role);
            }
        }
        return highestRole;
    }

    /**
     * @param message  The message that may contain User mentions
     * @param config   The mention config to be used for formatting
     * @param mentions The list of users mentioned
     * @param server   The server to be used for nickname support
     * @return The final message with User mentions formatted
     */
    public static String formatUserMentions(String message, ChannelMinecraftMentionConfig config, List<User> mentions, Server server) {
        if (mentions.isEmpty()) return message;
        for (User mention : mentions) {
            Optional<Role> role = getHighestRole(mention, server);
            String nick = (mention.getNickname(server.getId()) != null) ? mention.getNickname(server.getId()) : mention.getName();
            String color = (role.isPresent()) ? ColorUtil.getColorCode(role.get().getColor()) : null;
            String mentionString = (StringUtils.isNotBlank(color)) ?
                    ConfigUtil.get(config.userTemplate, "@%a").replace("%a", color + nick + "&r").replace("%u", color + mention.getName() + "&r") :
                    ConfigUtil.get(config.userTemplate, "@%a").replace("%a", nick).replace("%u", mention.getName());
            message = message.replaceAll("(<@!?" + mention.getId() + ">)", mentionString);
        }
        return message;
    }

    /**
     * @param message  The message that may contain Role mentions
     * @param config   The mention config to be used for formatting
     * @param mentions The list of roles mentioned
     * @return The final message with Role mentions formatted
     */
    public static String formatRoleMentions(String message, ChannelMinecraftMentionConfig config, List<Role> mentions) {
        if (mentions.isEmpty()) return message;
        for (Role mention : mentions) {
            String color = ColorUtil.getColorCode(mention.getColor());
            String mentionString = (StringUtils.isNotBlank(color)) ?
                    ConfigUtil.get(config.roleTemplate, "@%r").replace("%r", color + mention.getName() + "&r") :
                    ConfigUtil.get(config.roleTemplate, "@%r").replace("%r", mention.getName());
            message = message.replace("<@&" + mention.getId() + ">", mentionString);
        }
        return message;
    }

    /**
     * @param message The message that may contain everyone mentions
     * @param config  The mention config to be used for formatting
     * @param mention Whether everyone was mentioned
     * @return The final message with everyone mentions formatted
     */
    public static String formatEveryoneMentions(String message, ChannelMinecraftMentionConfig config, boolean mention) {
        if (!mention) return message;
        String template = ConfigUtil.get(config.everyoneTemplate, "@%a");
        return message
                .replaceAll("(@(here))", template.replace("%a", "here&r"))
                .replaceAll("(@(everyone))", template.replace("%a", "everyone&r"));
    }
}
