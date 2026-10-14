<p align="center">
  <img src="https://github.com/beanbeanjuice/SimpleProxyChat/blob/master/Images/Finished/SimpleProxyChat.png?raw=true" alt="SimpleProxyChat Logo"/>
</p>
<center>
  This is a Bungeecord Chat Sync, Velocity Chat Sync, and Proxy Chat Sync plugin. It is a simple plugin to allow <b>global</b> <i>cross-server</i> communication and messaging with support for <b>PlaceholderAPI</b>, <b>LuckPerms</b>, <b>LiteBans</b>, <b>AdvancedBan</b>, <b>NetworkManager</b>, and <b>Discord</b>.
</center>

---

### Supported Minecraft Versions

| Platform | Supported Versions                                      |
|---|---------------------------------------------------------|
| **Velocity** (proxy) | Depends on your Velocity install + ViaVersion if needed |
| **BungeeCord/Waterfall** (proxy) | Depends on your proxy install + ViaVersion if needed    |
| **Spigot/Paper/Bukkit** (helper) | 1.21.11 – 26.3                                          |
| **Fabric** (helper) | 26.1+                                                   |
| **NeoForge** (helper) | 26.1.2+                                                 |

---

<p align="center">
  <img src="https://github.com/beanbeanjuice/SimpleProxyChat/blob/master/Images/Finished/Installation.png?raw=true" alt="installation"/>
</p>

### Standalone (Proxy Only, No Helper)
This is the simplest setup and is **all you need** for cross-server chat on a standard BungeeCord/Velocity network.

1) Drop the plugin jar into the `plugins` folder on your **BungeeCord/Waterfall/Velocity** proxy.
1) Restart the proxy. It should just work out of the box!

> In standalone mode, the proxy intercepts chat on the way through. If you want chat to be relayed only **after** backend plugins (e.g. LiteBans on each server) have processed it, if you want **PlaceholderAPI** support, or if you want backend-specific hooks for **Fabric/NeoForge** servers, install the helper plugin below.

### With the Helper Plugin
The helper runs on each backend server and forwards chat to the proxy over TCP, **after** backend plugins have processed it. It is also what enables **PlaceholderAPI** support (Spigot/Paper/Bukkit only).

**Proxy-side setup (required before installing any helper):**
1) Install the proxy plugin on **BungeeCord/Waterfall/Velocity** as in the standalone section above (so `ProxyChat/config.yml` exists).
1) Edit `ProxyChat/config.yml` and set:
    * `use-websocket: true`
    * `websocket-port` (default `8766`) - any free TCP port; this must match on each helper.
    * `websocket-secret` (default `"changeme"`) - change this to something strong; it must match on each helper.
1) Restart the proxy (or run `/spc-reload`).

Then, on each backend server, install the matching helper build. Config keys are the same across all three platforms; only the file location and the default secret differ slightly.

#### Spigot/Paper/Bukkit
1) Download the helper plugin. This is the same as the main plugin for Velocity/Bungeecord.
1) Drop it into the server's `plugins` folder and start the server once to generate `plugins/SimpleProxyChatHelper/config.yml`.
1) Edit the generated config to point at your proxy:
    ```yaml
    proxy-host: "localhost"       # host where the proxy runs
    proxy-port: 8766              # must match websocket-port in the proxy config
    proxy-secret: "changeme"      # must match websocket-secret in the proxy config
    server-name: "survival"       # must match this server's name in velocity.toml / BungeeCord's config.yml
    ```
1) (Optional) Install **PlaceholderAPI** on the server for placeholder resolution in Minecraft chat messages.
1) Restart the server.

#### Fabric
1) Grab the matching **Fabric** helper mod for your server version.
1) Drop the mod jar into the server's `mods` folder and start the server once to generate `config/simpleproxychathelper.yml`.
1) Edit the generated file with the same keys as the Spigot example above (`proxy-host`, `proxy-port`, `proxy-secret`, `server-name`).
1) Restart the server.

#### NeoForge
1) Grab the matching **NeoForge** helper mod for your server version.
1) Drop the mod jar into the server's `mods` folder and start the server once to generate `config/simpleproxychathelper.yml`.
1) Edit the generated file with the same keys as the Spigot example above (`proxy-host`, `proxy-port`, `proxy-secret`, `server-name`).
1) Restart the server.

> For **PlaceholderAPI** support, the helper plugin is required, and placeholders can only be used in **Minecraft chat messages**. They will **not currently work** for join/leave messages. PlaceholderAPI is only available on the **Spigot/Paper/Bukkit** helper (there is no equivalent for Fabric/NeoForge).

### With Discord
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
1) Restart your proxy!

---

<p align="center">
  <img src="https://github.com/beanbeanjuice/SimpleProxyChat/blob/master/Images/Finished/Features.png?raw=true" alt="features"/>
</p>

* **Global Network Chat**
* **LuckPerms Support**
* **LiteBans Support**
* **AdvancedBan Support**
* **NetworkManager Support**
* **Discord Support**
* **Velocity/Waterfall/BungeeCord Support**
* **Colored Chat**
* **Cross-Server Communication**
* **PlaceholderAPI Support**
* **...and more!**

---

<p align="center">
  <img src="https://github.com/beanbeanjuice/SimpleProxyChat/blob/master/Images/Finished/Configuration.png?raw=true" alt="configuration"/>
</p>

**config.yml**
```YAML
# ==========================================================
#                       INFORMATION
#                 HEX Values are Supported
#  Example: <#FFFFFF>Some text</#FFFFFF> this is a message!
#         Supports Mini-Message/Legacy Color Codes
# ==========================================================

# True if you will be using Discord. The reload command does not work with this.
use-discord: false

# Discord Bot Token (IGNORE IF use_discord = false).
BOT-TOKEN: "TOKEN_HERE"

# Channel to send Discord messages to (IGNORE IF use_discord = false).
CHANNEL-ID: "GLOBAL_CHANNEL_ID"

bot-activity:
   # Valid Types: ONLINE, DO_NOT_DISTURB, IDLE, INVISIBLE
   status: ONLINE
   # Valid Types: PLAYING, STREAMING, LISTENING, WATCHING, COMPETING, CUSTOM_STATUS
   type: "CUSTOM_STATUS"
   # Valid placeholders are %online% and/or %max-players% (UPDATES EVERY 5 MINUTES)
   text: "%online%/%max-players% - SimpleProxyChat by beanbeanjuice"

# The amount of seconds to check if a server is online/offline.
# Smaller numbers can cause errors. Beware.
server-update-interval: 3

# Use this if you want to change the default aliases.
# It MUST be the same name you have in your bungee/velocity config.
# Simply set it to disabled: disabled to disable it.
aliases:
   ServerInConfigExample: ServerAliasExample
   hub: Hub1
   smp: smp1

# Whether to use the permission system.
# Some permissions (denoted with ➕) are always active even if this is false.
# simpleproxychat.read.chat - Read chat messages.
# simpleproxychat.read.join - Read join messages.
# simpleproxychat.read.leave - Read leave messages.
# simpleproxychat.read.fake - Read the fake join/leave messages. Players must also have the REAL join/leave permission.
# simpleproxychat.read.switch - Read switch messages.
# simpleproxychat.read.update - Read update messages. ➕
# simpleproxychat.toggle.chat - Toggle proxy chat for a single server. ➕
# simpleproxychat.toggle.chat.all - Toggle proxy chat for all servers. ➕
# simpleproxychat.reload - Reload the config. ➕
# simpleproxychat.ban - Ban a player from the proxy. ➕
# simpleproxychat.unban - Unban a player from the proxy. ➕
# simpleproxychat.whisper - Whisper to another player on the proxy. ➕
# simpleproxychat.broadcast - Broadcast a message to everyone on the server. ➕
use-permissions: false

# Only messages that start with this character will be sent through the plugin.
# Set to '' to disable.
# Example: If it is set to '$', then when a player sends $hello, it will be sent through the proxy.
proxy-message-prefix: ''

# Whether to send if the statuses of the servers connected to the proxy when the proxy starts up.
# THIS IS FOR DISCORD MESSAGES ONLY.
use-initial-server-status: true

# Whether to send a fake join/leave message when vanishing/unvanishing.
use-fake-messages: true

# Format: https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
# Timezone: https://www.joda.org/joda-time/timezones.html
timestamp:
   format: "hh:mm a"
   timezone: "America/Los_Angeles"

# True if you will be using the helper plugin via TCP WebSocket.
# When enabled, the proxy will NOT intercept PlayerChatEvent directly.
# Instead, chat is forwarded by the helper plugin after backend processing
# (allowing plugins like LiteBans to cancel messages before they reach the proxy).
use-websocket: false
websocket-port: 8766
websocket-secret: "changeme"

update-notifications: true

# It is HIGHLY recommended to use a more robust proxy-wide banning system such as LiteBans or AdvancedBan.
# However, if you would rather a light-weight, simple, banning system. You can enable it here.
# A FULL PROXY RESTART IS REQUIRED TO USE THIS.
use-simple-proxy-chat-banning-system: false

# This will store and re-send the last few chat messages when a player switches servers.
# This is here because sometimes Velocity/Bungee does not keep the previous messages when switching.
# This WILL retain old formatting if you change the formatting prior to reloading.
send-previous-messages-on-switch:
   enabled: false
   amount: 15

# These require a restart in order to take place.
commands:
   reload-aliases:
      - "spcreload"
   chat-toggle-aliases:
      - "chattoggle"
   ban-aliases:
      - "spcban"
   unban-aliases:
      - "spcunban"
   whisper-aliases:
      - "spc-msg"
   reply-aliases:
      - "spc-r"
   broadcast-aliases:
      - "spc-bc"
      - "broadcast"

# Disable servers from sending/receiving proxy messages.
disabled-servers:
   - example1

# Disable this if you don't want to receive switch messages in all servers.
proxy-switch: true

# DO NOT TOUCH THIS
file-version: 18
```

**messages.yml**
```YAML
# ==========================================================
#                       INFORMATION
#                 HEX Values are Supported
#  Example: <#FFFFFF>Some text</#FFFFFF> this is a message!
#         Supports Mini-Message/Legacy Color Codes
# ==========================================================

# Prefix for the plugin. %plugin-prefix% usable anywhere.
plugin-prefix: "&8[<bold><rainbow>SimpleProxyChat&r&8]"

# Minecraft Stuff
minecraft:
   join:
      enabled: true
      message: "&e%player% &ahas joined the network. (%server%)"
   leave:
      enabled: true
      message: "&e%player% &chas left the network. (%server%)"
   chat:
      enabled: true
      message: "&8[&3%server%&8] &e%player% &9» &7%message%"
      vanished: "&cYou cannot send proxy messages while vanished. Your message must end with a '&e/&c' to speak."
   switch:
      enabled: true
      default: "&e%player% &7moved from &c%from% &7to &a%to%&7."
      no-from: "&e%player% &7moved &7to &a%to%&7."
   whisper:
      send: "&8[&dyou&8] &f⇒ &8[&d%receiver%&8] &9» &e%message%"
      receive: "&8[&d%sender%&8] &f⇒ &8[&dyou&8] &9» &e%message%"
      error: "&c/spc-whisper (user) (message)"
   discord:
      enabled: true
      message: "**%server%** %player% » %message%"
      embed:
         use: false
         title: "[%server%] %player%"
         message: "%message%"
         color: "#FFC0CB"
         use-timestamp: true
   command:
      no-permission: "%plugin-prefix% &cSorry, you do not have permission to run this command."
      unknown: "%plugin-prefix% &cUnknown command."
      must-be-player: "%plugin-prefix% &cYou must be a player to run this command."
      reload: "%plugin-prefix% &aThe plugin has been successfully reloaded!"
      chat-lock:
         usage: "%plugin-prefix% &cCorrect usage is &a/spc-chat all lock/unlock &c or &a/spc-chat lock/unlock"
         single:
            locked: "%plugin-prefix% &6%server% &cwill no longer send proxy chat messages."
            unlocked: "%plugin-prefix% &6%server% &awill now send proxy chat messages."
         all:
            locked: "%plugin-prefix% &cAll servers will no longer send proxy chat messages."
            unlocked: "%plugin-prefix% &aAll servers will now send proxy chat messages."
      proxy-ban:
         usage: "%plugin-prefix% &c/(un)ban (player)"
         banned: "%plugin-prefix% &c%player% &7has been banned."
         unbanned: "%plugin-prefix% &c%player% &7has been unbanned."
         login-message: "&cYou have been banned from the proxy."
      broadcast:
         usage: "%plugin-prefix% &c/spc-broadcast (message)"
         message: "%plugin-prefix% &8[&a&lBROADCAST&r&8] &6%message%"

# Discord Stuff
discord:
   join:
      enabled: true
      message: "%player% has joined the network. (%server%)"
      use-timestamp: true
      # Incompatible with use-timestamp.
      use-embed: true
   leave:
      enabled: true
      message: "%player% has left the network. (%server%)"
      use-timestamp: true
      # Incompatible with use-timestamp.
      use-embed: true
   switch:
      enabled: true
      message: "%player% has switched from %from% to %to%."
      use-timestamp: true
      # Incompatible with use-timestamp.
      use-embed: true
   chat:
      enabled: true
      minecraft-message: "&8[&bDiscord&8] %role% &f%user% &9» &7%message%"
   topic:
      online: "There are %online% players online."
      offline: "The proxy is offline."
   proxy-status:
      enabled: true
      messages:
         enabled: "✅ Proxy enabled!"
         disabled: "⛔ Proxy disabled."
         title: "Server Status"
         message: "Status: "
         online: "Online ✅"
         offline: "Offline ⛔"
         use-timestamp: true

# Console Stuff - Uses Minecraft Messages
console:
   chat: true
   join: true
   leave: true
   switch: true
   discord-chat: true
   server-status: true

# The message for any updates that are sent.
# The plugin-prefix is automatically appended to the beginning of this message.
update-message: "&7There is an update! You are on &c%old%. New version is &a%new%&7: &6%link%"

# DO NOT TOUCH THIS
file-version: 10
```

---

<p align="center">
  <img src="https://github.com/beanbeanjuice/SimpleProxyChat/blob/master/Images/Finished/Commands.png?raw=true" alt="commands"/>
</p>

* `/spc-reload` - Reloads the config files.
* `/spc-chat` - Lock/unlock the chat.
* `/spc-whisper` - Send a private message to someone.
* `/spc-reply` - Reply to a private message without specifying a user.
* `/spc-ban` - Ban a player from the proxy.
* `/spc-unban` - Unban a player from the proxy.
* `/spc-broadcast` - Broadcast a message to every player on the network.

---

<p align="center">
  <img src="https://github.com/beanbeanjuice/SimpleProxyChat/blob/master/Images/Finished/Permissions.png?raw=true" alt="permissions"/>
</p>

* `simpleproxychat.read.chat` - Read chat messages.
* `simpleproxychat.read.join` - Read join messages.
* `simpleproxychat.read.leave` - Read leave messages.
* `simpleproxychat.read.fake` - Read fake join/leave messages. Must have the real permission too.
* `simpleproxychat.read.switch` - Read switch messages.
* `simpleproxychat.read.update` - Read update notifications.
* `simpleproxychat.toggle.chat` - Toggle proxy chat for a single server.
* `simpleproxychat.toggle.chat.all` - Toggle proxy chat for all servers.
* `simpleproxychat.reload` - Reload the config.
* `simpleproxychat.ban` - Ban someone.
* `simpleproxychat.unban` - Unban someone.
* `simpleproxychat.whisper` - Private messaging permissions.
* `simpleproxychat.broadcast` - Broadcast a message to everyone on the server.

---

<p align="center">
  <img src="https://github.com/beanbeanjuice/SimpleProxyChat/blob/master/Images/Finished/Placeholders.png?raw=true" alt="placeholders"/>
</p>

* `%plugin-prefix%` - The plugin's designated prefix.
* `%server%` - The current connected server. Uses the alias if one is specified.
* `%original_server%` - Same as `%server%`, but does not use the alias.
* `%to%` - The current connected server. Uses the alias if one is specified.
* `%original_to%` - Same as `%to%`, but does not use the alias.
* `%from%` - The server the player just disconnected from. Uses the alias if one is specified.
* `%original_from%` - Same as `%from%`, but does not use the alias.
* `%player%` - The player's Minecraft username.
* `%escaped_player%` - The escaped player's Minecraft username. (Adds '\\' before the '_' to prevent italics.)
* `%sender%` - The person sending the private message. **Private Messaging Only**
* `%receiver%` - The person receiving the private message. **Private Messaging Only**
* `%user%` - The player's Discord username.
* `%nick%` - The player's Discord nickname.
* `%role%` - The player's Discord role.
* `%prefix%` - The player's prefix. **LuckPerms Only**
* `%suffix%` - The player's suffix. **LuckPerms Only**
* `%message%` - The player's message.
* `%epoch%` - This gets the current time (in milliseconds). Formats can be used like [this](https://gist.github.com/LeviSnoot/d9147767abeef2f770e9ddcd91eb85aa). An example would be `<t:%epoch%>`. **Discord Only**
* `%time%` - Similar to `%epoch%`, but uses a special formatting and timezone found in `config.yml`.
* `%online%` - **Discord Status Only**
* `%max-players%` - **Discord Status Only**

---

<p align="center">
  <img src="https://github.com/beanbeanjuice/SimpleProxyChat/blob/master/Images/Finished/Caveats.png?raw=true" alt="caveats"/>
</p>

1) As of right now, vanish support is only available on *BungeeCord/Waterfall*. The plugin will still function as normal, but if you go into vanish then it won't send a fake join/leave message.
1) In order for prefixes and suffixes to work, you **must** have LuckPerms installed on the proxy. Then, you can use `%prefix%` and `%suffix%`.
1) `%epoch%` and the timestamps only work in certain places on Discord. As an alternative, you can select some of the embeds to have `use-timestamp: true`. This is out of my control sadly... 😔

---

<p align="center">
  <img src="https://github.com/beanbeanjuice/SimpleProxyChat/blob/master/Images/Finished/Statistics.png?raw=true" alt="statistics"/>
</p>

### Velocity Statistics
![velocity statistics](https://bstats.org/signatures/velocity/SimpleProxyChat.svg)

### BungeeCord/Waterfall Statistics
![bungeecord/waterfall statistics](https://bstats.org/signatures/bungeecord/SimpleProxyChat.svg)
