# GETTING STARTED

## How to setup for servers
1. Setup a Minecraft server with compatible [SpongeVanilla](https://docs.spongepowered.org/en/server/getting-started/implementations/spongevanilla.html) or [SpongeForge](https://docs.spongepowered.org/en/server/getting-started/implementations/spongeforge.html)
1. Download Discord Bridge [latest release](https://github.com/nguyenquyhy/Sponge-Discord/releases) and put it in your server's mod folder
1. Start the server to create a default config file at `configs/com.nguyenquyhy.spongediscord/config.conf`
1. Set compulsory values in the newly created config file
    - `Channel`: the ID of your Discord channel. Check our 
[README.md](README.md) if you don't know how to obtain it. 
    - `InviteCode`: the invitation code to the channel above. You should also remove the expiring time of the code.
1. Restart the server or run `/discord reload`
1. Setup default/bot account (this is optional, but necessary if you want all players in your server able to send/receive Discord messages)
    - Go to https://discordapp.com/developers to create a new Discord Applications and App Bot User
    - Add the Bot to your channel https://discordapp.com/developers/docs/topics/oauth2#bots
    - Set the Bot's Token in Discord Bridge config

Legacy bot account setup (not recommended): 
    - Run `/discord default login <bot_Discord_username> <bot_Discord_password>`
    - Discord Bridge will automatically re-login the default/bot account if your server restarts. 
    
## How to use for players
- You can chat in Discord in the specific channel above
    - Your messages will be broadcast to all players if the server owners has set up a default/bot account.
    - Otherwise, your messages will be broadcast to all __authenticated__ players.
- You can chat in Minecraft:
    - Your messages will show up in the Discord channel under your Discord name if you have __authenticated__.
    - Your messages will show up in the Discord channel under the bot name if you have not __authenticated__ and server owner has set up a default/bot account.
    - Otherwise, your messages from Minecraft will not be sent to Discord.
- To __authenticate__, run this command in Minecraft `/discord login <your_Discord_username> <your_Discord_password>`
    - **WARNING: some server logs all command, so be careful not to leak your Discord credentials on untrusted servers.**