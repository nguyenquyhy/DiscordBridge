# Sponge-Discord
This is a Sponge plugin to integrate Minecraft server with a Discord channel.

Player's chat messages in Minecraft are sent a specified Discord channel, and chat messages in that Discord channel are also sent to online players in Minecraft.
Each player can log in to their own Discord account, so that chat messages show under their names, or server owners can setup a default account as a bot for all unauthenticated players.

## Build

Build Sponge-Discord
1. Clone this repository
1. Run `gradle` (or `gradlew` for Windows)
1. The jar file will be in `build/libs/Sponge-Discord-{version}-all.jar.

## Commands

- `/discord login <email> <password>`: login to Discord and bind the Discord account to the Minecraft account for automatic login in the future. The email and password will not be stored; instead, the access token of the user will be stored in the config folder on the server.
- `/discord logout`: logout of Discord and unbind the Discord account from the Minecraft account. 
- `/discord default login <email> <password>`: login to a default Discord account. This account will be used as a bot for unauthenticated players to send and receive Discord messsages in Minecraft.
- `/discord default logout`: logout of the default Discord account.
- `/discord broadcast`: as this plugin cannot capture server's `/say` at the moment, this command is to send a message to all online players and Discord. This command requires having the default account set up.  
- `/discord reload`: reload configurations.

A short summary is below:

| Command | Shorthand | Permission |
|---------|-----------|------------|
| `/discord login <e> <p>` | `/d l <e> <p>` | &nbsp; |
| `/discord logout` | `/d lo` | &nbsp; |
| `/discord default login <e> <p>` | `/d d l <e> <p>` | `spongediscord.default` |
| `/discord default logout` | `/d d lo` | `spongediscord.default` |
| `/discord broadcast <message>` | `/d b <message>` | &nbsp; |
| `/discord reload` | &nbsp; | &nbsp; |

Some ideas for future commands

| Command | Note |
|---------|------|
| `/discord config` | Show current configuration |
| `/discord status` | Show current Discord account |

## Configurations

- `Channel`: the ID of the Discord number (usually a 18-digit number)
- `InviteCode`: an invitation code that does not expire (usually the part after `https://discord.gg/` in the invitation link)
- `JoinedMessage`: (optional) a message that will be posted to Discord when a player joins the server
- `LeftMessage`: (optional) a message that will be posted to Discord when a player leaves the server
- `MessageInDiscordPrefix`: (optioal) a prefix for message from Minecraft to Discord. You can use Discord markdown format here.
- `MessageInMinecraftPrefix`: (optional) a prefix for message from Discord to Minecraft. You can use Minecraft formatting here.
- `TokenStore`: either `InMemory` (mainly for testing) or `JSON`