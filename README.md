# Discord Bridge
This is a [Sponge](http://spongepowered.com) plugin to integrate [Minecraft](https://minecraft.net) server with a [Discord](https://discordapp.com) channel. 

## Features

- Player's chat messages in Minecraft are sent to specified Discord channels, and chat messages in specific Discord channels are also sent to online players in Minecraft.
- Admins and mods can log in to their own Discord account, so that chat messages show under their names in Discord.
- Emoji is converted between Minecraft and Discord format. Details are showed in [EMOJI.md](EMOJI.md).
- Clickable URL.
- **New in 2.0.0** Multiple channels with custom configuration for each channel. E.g.:
  - 1 public channel to send & receive messages between Discord and Minecraft
  - 1 monitoring channel to record only server start/stop and player join/leave events
  - 1 staff-only channel that send message one-way from Discord to Minecraft with a special announcement template
- **New in 2.2.0**
  - Set game activity of the bot
  - Ignore Discord messages from all bots and/or blacklist certain prefixes
  - Support One-Time Password

## Getting Started for server owners and players

[GETTING STARTED.md](GETTING STARTED.md)

## Migrating from version 1.x.x
- `/discord default` command and default account are no longer available. You must setup a Discord Bot for the plugin to properly function.
- Your current configuration will be migrated automatically from `config.conf` into `config.json`.
- Invite code has been removed. Please contact me if you have specific need for that.
- Default anonymouss chat template is changed to ```"`%a:\` %s"```, which looks nicer in my opinion.

## Build your own .jar

1. Clone this repository
1. Run `gradlew`
1. The jar file will be in `build/libs/Sponge-Discord-{version}-all.jar`.

## Commands

- `/discord login`: login to Discord and bind the Discord account to the Minecraft account for automatic login in the future. The email and password will not be stored; instead, the access token of the user will be stored in the config folder on the server.
- `/discord otp`: One-time password for Discord login _(thanks Prototik)_.
- `/discord logout`: logout of Discord and unbind the Discord account from the Minecraft account. 
- `/discord broadcast`: as this plugin cannot capture server's `/say` at the moment, this command is to send a message to all online players and Discord. This command requires having the default account set up.
- `/discord status`: show current connection status.
- `/discord reload`: reload configurations.

A short summary is below:

| Command | Shorthand | Permission |
|---------|-----------|------------|
| `/discord login` | `/d l` | `discordbridge.login` |
| `/discord otp` | `/d otp` | `discordbridge.login` |
| `/discord logout` | `/d lo` | `discordbridge.login` |
| `/discord broadcast <message>` | `/d b <message>` | `discordbridge.broadcast` |
| `/discord status` | `/d s` | `discordbridge.status` |
| `/discord reload` | `/d reload` | `discordbridge.reload` |

Some ideas for future commands

| Command | Note |
|---------|------|
| `/discord config` | Show current configuration |
| `/discord status` | Show current Discord account |

## Configurations

Configuration is stored in `config.json` file. 

- Global config
  - `botToken`: App Bot User's token
  - `botDiscordGame`: sets the current game activity of the bot in Discord _(thanks, Vankka)_
  - `tokenStore`: `JSON` (default) or `NONE` (user authentication will be disabled) or `InMemory` (mainly for testing). This is used for player authentication.
  - `minecraftBroadcastTemplate`: template for messages in Minecraft from `/discord broadcast` command
  - `prefixBlacklist`: a list of prefix string (e.g. `["!"]`) that will be ignored by the plugin _(thanks, Vankka)_
  - `ignoreBots`: ignore all messages from any Discord Bots _(thanks, Vankka)_
  - `linkDiscordAttachments`: adds a clickable link in game for attachments sent via discord _(thanks, Mohron)_
  - `channels`: a list of channel configurations
- Channel config
  - `discordId`: the ID of the Discord channel (usually a 18-digit number)
  - `discord`: templates in Discord
    - `joinedTemplate`: (optional) template for a message in Discord when a player joins the server
    - `leftTemplate`: (optional) template for a message in Discord when a player leaves the server
    - `anonymousChatTemplate`: (optional) template for messages from Minecraft to Discord for unauthenticated user
    - `authenticatedChatTemplate`: (optional) template for messages from Minecraft to Discord for authenticated user
    - `broadcastTemplate`: (optional) template for messages in Discord from `/discord broadcast` command
  - `minecraft`: templates in Minecraft
    - `chatTemplate`: (optional) template for messages from Discord to Minecraft
        - **Supported Placeholders**
        - %s - the message sent via discord
        - %a - the username of the message author
        - %r - the name of the highest role held by the message author (See also defaultRole & roleBlacklist)
        - %c - the color code of the highest role if set to a Minecraft compatible color
        - %g - the current game of the message author
        - %s - the message sent from discord
    - `attachmentTemplate`: (required for linkDiscordAttachments) template for Discord attachments linked in Minecraft _(thanks, Mohron)_
    - `attachmentColor`: (optional) designate a color to be applied to attachmentTemplate (ie `&0` or `§0`) _(thanks, Mohron)_
    - `attachmentHoverTemplate`: (optional) template for the message shown when you hover over an attachment link _(thanks, Mohron)_
    - `defaultRole`: (optional) a default role and formatting to be used when a user's has no non-blacklisted roles _(thanks, Mohron)_
    - `roleBlacklist`: (optional) a list of roles to ignore when calculating a user's highest rank _(thanks, Mohron)_

You can find some example configurations in `examples` folders.

## Notes

### How to get channel ID

1. Open `User Settings` in Discord, then open `Appearance` section and tick `Developer Mode`
1. Right click any channel and click `Copy ID`

## CHANGELOG

[CHANGELOG.md](CHANGELOG.md)

## TODO

* 2.3.0
- [X] Mentions in Discord should show proper names in Minecraft
- [X] Attachments in Discord should show proper links in Minecraft

* Future
- [ ] MySQL token store
- [ ] Group-based prefix
- [ ] Handle custom Sponge channels (e.g. MCClan and staff chat of Nucleus)
- [ ] A command to check Bot connection status
- [ ] New config to allow executing Minecraft command from Discord
- [ ] New config to route Minecraft server log to Discord
