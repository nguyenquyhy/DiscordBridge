# Sponge-Discord
This is a Sponge plugin to integrate Minecraft server with a Discord channel.
Player's chat messages in Minecraft are showed in a specified Discord channel, and chat messages in that Discord channel are also showed to all logged in players in Minecraft.  

## Build

Build Discord4J dependency
1. Clone this fork of Discord4J at https://github.com/nguyenquyhy/Discord4J
1. Download and install maven (http://maven.apache.org)
1. Run `mvn install` to build and install theDiscord4J into local maven repository

Build Sponge-Discord
1. Clone this repository
1. Run `gradle build` (or `gradlew build` for Windows)

## Commands

- `/discord login <email> <password>`: login to Discord and bind the Discord account to the Minecraft account for automatic login in the future. The email and password will not be stored; instead, the access token of the user will be stored in the config folder on the server.
- `/discord logout`: logout of Discord and unbind the Discord account from the Minecraft account. 

## Configuration

- `Channel`: the ID of the Discord number (usually a 18-digit number)
- `InviteCode`: an invitation code that does not expire (usually the part after `https://discord.gg/` in the invitation link)
- `JoinedMessage`: a message that will be posted to Discord when a player joins the server
- `LeftMessage`: a message that will be posted to Discord when a player leaves the server