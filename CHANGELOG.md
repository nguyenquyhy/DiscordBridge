# CHANGE LOG

## 2.0.0
- Configuration is now stored in `config.json`. Old `config.conf` will be migrated automatically. 
- Support for multiple channels.
- Remove support for default account. Bot is compulsory now.
- Remove support for Invite token. You have to add permission for the Bot to your channels before using the plugin.
- `/discord login` command now accepts no parameters and will give out warning and instructions to proceed.
- `/discord broadcast` command now uses templates in configuration.
- Replace the underlying library for Discord API to reduce incompatibility with Sponge and Forge.

## 1.4.0
- URL from Discord is clickable in Minecraft.
- Emoji is translated properly between Discord and Minecraft.
- Bot no longer tries to use invitation link.

## 1.3.1
- Auto re-login for expired sessions on receiving new messages
- Clean up error log

## 1.3.0
- Rename to Discord Bridge
- Update Discord4J
- Escape player with underscore in their name

## 1.1.1
- Update due to changes in Discord API.

## 1.1.0

- Emojis are converted between Minecraft (`:)`, `:P`) and Discord format (`:smiley:`, `:smile:`).
- Added permissions for `broadcast` and `reload` commands.

## 1.0.0

- Player can now send/receive messages between Minecraft and a specific Discord channel.
