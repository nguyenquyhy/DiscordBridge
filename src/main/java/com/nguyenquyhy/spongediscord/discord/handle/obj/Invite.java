package com.nguyenquyhy.spongediscord.discord.handle.obj;

import com.nguyenquyhy.spongediscord.SpongeDiscord;
import com.nguyenquyhy.spongediscord.discord.DiscordClient;
import com.nguyenquyhy.spongediscord.discord.DiscordEndpoints;
import com.nguyenquyhy.spongediscord.discord.util.HttpException;
import com.nguyenquyhy.spongediscord.discord.util.Requests;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Created by Hy on 1/11/2016.
 */
public class Invite {
    /**
     * An invite code, AKA an invite URL minus the https://discord.gg/
     */
    private final String inviteCode;
    private final DiscordClient client;

    public Invite(String inviteCode, DiscordClient client) {
        this.inviteCode = inviteCode;
        this.client = client;
    }
    public Invite(String inviteCode) {
        this(inviteCode, DiscordClient.get());
    }

    /**
     * @return The invite code
     */
    public String getInviteCode() {
        return inviteCode;
    }

    /**
     * @return Accepts the invite and returns relevant information,
     *         such as the Guild ID and name, and the channel the invite
     *         was created from.
     * @throws Exception
     */
    public void accept() throws HttpException {
        if (client.isReady()) {
            String response = Requests.POST.makeRequest(DiscordEndpoints.INVITE + inviteCode,
                    new BasicNameValuePair("authorization", client.getToken()));
        } else {
            SpongeDiscord.getInstance().getLogger().error("Bot has not signed in yet!");
        }
    }

    /**
     * Gains the same information as accepting,
     * but doesn't actually accept the invite.
     *
     * @return an InviteResponse containing the invite's details.
     * @throws Exception
     */
    public InviteResponse details() throws Exception {
        if (client.isReady()) {
            String response = Requests.GET.makeRequest(DiscordEndpoints.INVITE + inviteCode,
                    new BasicNameValuePair("authorization", client.getToken()));

            JSONObject object1 = (JSONObject) new JSONParser().parse(response);
            JSONObject guild = (JSONObject) object1.get("guild");
            JSONObject channel = (JSONObject) object1.get("channel");

            return new InviteResponse((String) guild.get("id"),
                    (String) guild.get("name"),
                    (String) channel.get("id"),
                    (String) channel.get("name"));
        } else {
            SpongeDiscord.getInstance().getLogger().error("Bot has not signed in yet!");
            return null;
        }
    }

    public class InviteResponse {
        /**
         * ID of the guild you were invited to.
         */
        private final String guildID;

        /**
         * Name of the guild you were invited to.
         */
        private final String guildName;

        /**
         * ID of the channel you were invited from.
         */
        private final String channelID;

        /**
         * Name of the channel you were invited from.
         */
        private final String channelName;

        //TODO replace with objects. Need to figure out logistics, as the GUILD_CREATE is sent after MESSAGE_CREATE and after we accept the invite
        public InviteResponse(String guildID, String guildName, String channelID, String channelName) {
            this.guildID = guildID;
            this.guildName = guildName;
            this.channelID = channelID;
            this.channelName = channelName;
        }

        public String getGuildID() {
            return guildID;
        }

        public String getGuildName() {
            return guildName;
        }

        public String getChannelID() {
            return channelID;
        }

        public String getChannelName() {
            return channelName;
        }
    }
}
