# GETTING STARTED

## How to setup for servers
1. Setup a Discord Application and a App Bot user (http://discordapp.com/developers/applications/me)
1. Allow the bot to access the channels you will use (https://discordapp.com/developers/docs/topics/oauth2#adding-bots-to-guilds)
1. Setup a Minecraft server with compatible [SpongeVanilla](https://docs.spongepowered.org/en/server/getting-started/implementations/spongevanilla.html) or [SpongeForge](https://docs.spongepowered.org/en/server/getting-started/implementations/spongeforge.html)
1. Download Discord Bridge [latest release](https://github.com/nguyenquyhy/Sponge-Discord/releases) and put it in your server's mod folder
1. Start the server to create a default config file at `configs/discordbridge/config.json`
1. Set compulsory values in the newly created config file
    - `botToken`: token of your _App Bot User_ of your Bot in http://discordapp.com/developers/applications/me
    - `discordId` of each channel: the ID of your Discord channel. Check our 
[README.md](README.md) if you don't know how to obtain it. 
1. Restart the server or run `/discord reload`
    
## How to use for players
- You can chat in Discord in the any channel that has `minecraft` section set up. Your messages will be broadcast to all players if the server owners has set up a default/bot account.
- You can chat in Minecraft:
    - Your messages will show up in the Discord channel under your Discord name if you have __authenticated__.
    - Otherwise, your messages will show up in the Discord channel under the bot name if you have not __authenticated__.
- To __authenticate__, run this command in Minecraft `/discord login <your_Discord_username> <your_Discord_password>`
    - **WARNING: the server may log all commands, so be careful not to leak your Discord credentials on untrusted servers.**