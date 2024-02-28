# ProxyChat (BungeeCord/Waterfall/Velocity)


A simple plugin to send chat messages to/from all servers on the network. This also includes to/from a Discord server.

---

## Setup Information (No Discord)
1) Simply place the plugin in your `plugins` folder on **BungeeCord/Velocity** and restart your server!

## Setup Information (Discord)
1) Go to discord.com/developers and select **New Application**. You can select *any* name you want.
1) Go to the **OAuth2** section.
1) Go to the **OAuth2 URL Generator** and give it the **bot** scope.
1) Under **bot permissions** select;
    - Manage Channels (**REQUIRED**: General Permissions)
    - Send Messages (**REQUIRED**: Text Permissions)
    - Manage Messages (**REQUIRED**: Text Permissions)
    - Read Message History (**REQUIRED**: Text Permissions)
    - *+ Any others that you want to add...*
1) Copy the **Generated URL** at the bottom.
1) Paste the generated URL into a web browser.
1) Invite the bot to your server.
1) Go back to discord.com/developers.
1) Select your bot.
1) Click on **bot**.
1) Click **Reset Token** and copy the token somewhere safe.
1) Scroll down to **Privileged Gateway Intents**.
1) Select **SERVER MEMBER INTENT** and **MESSAGE CONTENT INTENT**.
1) Place the plugin in the `plugins` folder on **BungeeCord/Velocity**.
1) Start your server once *fully*, then stop it. There *will* be errors, this is normal.
1) In the generated `ProxyChat/config.yml` file, paste your **Bot Token** and choose the appropriate channel you want messages being sent to.
1) Restart your server!

---

## Requirements

* Java Version: 17+ (**REQUIRED**)
* PremiumVanish (**OPTIONAL**)
* SuperVanish (**OPTIONAL**)

---

## Caveats
As of right now, vanish support is only available on *BungeeCord/Waterfall*. The plugin will still function as normal, but if you go into vanish then it won't send a fake join/leave message.

---

## Configuration
```YAML
# ==========================================================
#                       INFORMATION
#                 HEX Values are Supported
#  Example: <#FFFFFF>Some text</#FFFFFF> this is a message!
#  If using the color codes ("&"), only use the COLOR codes.
# ==========================================================

# True if you will be using Discord
use_discord: false

# Discord Bot Token (IGNORE IF use_discord = false)
BOT_TOKEN: "TOKEN_HERE"

# Channel to send Discord messages to (IGNORE IF use_discord = false)
CHANNEL_ID: "GLOBAL_CHANNEL_ID"

# Minecraft to Minecraft
join-format: "&e%player% &ahas joined the network."
leave-format: "&e%player% &chas left the network."
message-format: "&8[&3%server%&8] &e%player% &9» &7%message%"

# Message Sent to Console
switch-format: "&e%player% &7moved from &c%from% &7to &a%to%&7."

# Message Sent in Minecraft
switch-format_NO_FROM: "&e%player% &7moved &7to &a%to%&7."

# Minecraft to Discord
minecraft_to_discord_join: "%player% has joined the network."
minecraft_to_discord_leave: "%player% has left the network."
minecraft_to_discord_switch: "%player% has switched from %from% to %to%."
minecraft_to_discord_message: "**%server%** %player% » %message%"

# Discord to Minecraft
discord_to_minecraft_message: "&8[&bDiscord&8] %role% &f%user% &9» &7%message%"

# DO NOT TOUCH THIS
file-version: 2

```

---

## Statistics
### BungeeCord/Waterfall Graph
![alt text](https://bstats.org/signatures/bungeecord/SimpleProxyChat.svg)

### Velocity Graph
![alt text](https://bstats.org/signatures/velocity/SimpleProxyChat.svg)
