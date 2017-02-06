# Discord Bridge
This is a [Sponge](http://spongepowered.com) plugin to integrate [Minecraft](https://minecraft.net) server with a [Discord](https://discordapp.com) channel. 

## Features

- Player's chat messages in Minecraft are sent to specified Discord channels, and chat messages in specific Discord channels are also sent to online players in Minecraft.
- Admins and mods can log in to their own Discord account, so that chat messages show under their names in Discord.
- Emoji is converted between Minecraft and Discord format. Details are showed in [EMOJI.md](EMOJI.md).
- Clickable URL.
- Multiple channels with custom configuration for each channel. E.g.:
  - 1 public channel to send & receive messages between Discord and Minecraft
  - 1 monitoring channel to record only server start/stop and player join/leave events
  - 1 staff-only channel that send message one-way from Discord to Minecraft with a special announcement template
- Set game activity of the bot
- Ignore Discord messages from all bots and/or blacklist certain prefixes
- Support One-Time Password
- **New in 2.3.0**
  - Mentions in Discord show properly in Minecraft with configurable templates
  - Mentions from Minecraft are supported with permission control (check **Additional Permissions**)
  - Attachments in Discord shows proper links in Minecraft
  - Support Minecraft templates based on Discord roles

## Getting Started for server owners and players

[GETTING STARTED.md](GETTING STARTED.md)

## Migrating from 1.x.x or 2.0.0

[MIGRATE.md](MIGRATE.md)

## Build your own .jar

1. Clone this repository
1. Run `gradlew`
1. The jar file will be in `build/libs/DiscordBridge-{version}-all.jar`.

## Commands

- `/discord login`: login to Discord and bind the Discord account to the Minecraft account for automatic login in the future. The email and password will not be stored; instead, the access token of the user will be stored in the config folder on the server.
- `/discord otp`: One-time password for Discord login _(thanks Prototik)_.
- `/discord logout`: logout of Discord and unbind the Discord account from the Minecraft account. 
- `/discord broadcast`: as this plugin cannot capture server's `/say` at the moment, this command is to send a message to all online players and Discord. This command requires having the default account set up.
- `/discord status`: show current connection status.
- `/discord reload`: reload configurations.
- `/discord reconnect`: reconnect Discord connection.

A short summary is below:

| Command | Shorthand | Permission |
|---------|-----------|------------|
| `/discord login` | `/d l` | `discordbridge.login` |
| `/discord otp` | `/d otp` | `discordbridge.login` |
| `/discord logout` | `/d lo` | `discordbridge.login` |
| `/discord broadcast <message>` | `/d b <message>` | `discordbridge.broadcast` |
| `/discord status` | `/d s` | `discordbridge.status` |
| `/discord reload` | `/d reload` | `discordbridge.reload` |
| `/discord reconnect` | `/d reconnect` | `discordbridge.reconnect` |

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
  - `channels`: a list of channel configurations
- Channel config
  - `discordId`: the ID of the Discord channel (usually a 18-digit number)
  - `discord`: templates in Discord
    - `joinedTemplate`: (optional) template for a message in Discord when a player joins the server
    - `leftTemplate`: (optional) template for a message in Discord when a player leaves the server
    - `anonymousChatTemplate`: (optional) template for messages from Minecraft to Discord for unauthenticated user
    - `authenticatedChatTemplate`: (optional) template for messages from Minecraft to Discord for authenticated user
    - `broadcastTemplate`: (optional) template for messages in Discord from `/discord broadcast` command
    - `deathTemplate`: (optional) template for a message in Discord when a player dies _(thanks, Mohron)_
  - `minecraft`: templates in Minecraft
    - `chatTemplate`: (optional) template for messages from Discord to Minecraft. For supporting placeholders in the template, check the section **Chat placeholder** 
    - `attachment`: _(thanks, Mohron)_
      - `template`: template for Discord attachments linked in Minecraft 
      - `hoverTemplate`: template for the message shown when you hover over an attachment link
      - `allowLink`: adds a clickable link in game for attachments sent via discord
    - `mention`: _(thanks, Mohron)_
      - `userTemplate`: template for @user mentions - accepts `%s`/`%u` 
      - `roleTemplate`: template for @role mentions - accepts `%s`
      - `everyoneTemplate`: template for @here & @everyone mentions - accepts `%s`
      - `channelTemplate`: template for @here & @everyone mentions - accepts `%s`
    - `roles`: `minecraft` configurations that are for a specific Discord role

You can find some example configurations in `examples` folders.

### Chat Placeholders
- `%s` - the message sent via discord
- `%a` - the nickname of the message author or username if nickname is unavailable
- `%u` - the username of the author. This is used if you want to disallow Discord nickname.
- `%r` - the name of the highest Discord role held by the message author. Color of the role will also be translated into Minecraft color automatically.
- `%g` - the current game of the message author

### Additional Permissions
 *NOTE: The below permissions are applicable only to unathenticated users. Authenticated users chat under their own Discord accounts, so you can restrict using Text permission of Discord roles.*

| Permission | Use |
|---------|-----------|
| `discordbridge.mention.name` <br> `discordbridge.mention.name.<name>` | Allows `@username`/`@nickname` mentions to be sent from Minecraft |
| `discordbridge.mention.role` <br> `discordbridge.mention.role.<role>`  | Allows `@role` mentions - the role must have "Allow anyone to @mention" set |
| `discordbridge.mention.channel` <br> `discordbridge.mention.channel.<channel>` | Allows `#channel` mention |
| `discordbridge.mention.here` | Allows the `@here` mention<sup>1</sup> |
| `discordbridge.mention.everyone` | Allows the `@everyone` mention<sup>1</sup> |
>  <sup>1</sup> The bot must have permission to "Mention Everyone" in order to use `@here` & `@everyone`.

## Frequently Asked Questions

### How to get channel ID

1. Open `User Settings` in Discord, then open `Appearance` section and tick `Developer Mode`
1. Right click any channel and click `Copy ID`

## CHANGELOG

[CHANGELOG.md](CHANGELOG.md)

## TODO

* 2.4.0
- [ ] New config to allow executing Minecraft command from Discord

* Future
- [ ] MySQL token store
- [ ] Group-based prefix
- [ ] Handle custom Sponge channels (e.g. MCClan and staff chat of Nucleus)
- [ ] A command to check Bot connection status
- [ ] New config to route Minecraft server log to Discord
