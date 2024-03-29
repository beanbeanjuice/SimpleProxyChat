<p align="center">
  <img src="https://github.com/beanbeanjuice/SimpleProxyChat/blob/master/Images/Finished/SimpleProxyChat.png?raw=true" alt="SimpleProxyChat Logo"/>
</p>
<center>
  A simple plugin to allow <b>global</b> <i>cross-server</i> communication and messaging with support for <b>LuckPerms</b>, <b>LiteBans</b>, and <b>Discord</b> integration.
</center>

---

<p align="center">
  <img src="https://github.com/beanbeanjuice/SimpleProxyChat/blob/master/Images/Finished/Installation.png?raw=true" alt="installation"/>
</p>

### Without Discord
1) Simply place the plugin in your `plugins` folder on **BungeeCord/Waterfall/Velocity** and restart your proxy!

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
* **Discord Support**
* **Velocity/Waterfall/BungeeCord Support**
* **Colored Chat**
* **Cross-Server Communication**

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

# Discord Bot Token (IGNORE IF use_discord = false). The reload command does not work with this.
BOT-TOKEN: "TOKEN_HERE"

# Channel to send Discord messages to (IGNORE IF use_discord = false). The reload command does not work with this.
CHANNEL-ID: "GLOBAL_CHANNEL_ID"

bot-activity:
  # Valid Types: PLAYING, STREAMING, LISTENING, WATCHING, COMPETING
  type: "COMPETING"
  text: "SimpleProxyChat by beanbeanjuice"

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
# simpleproxychat.read.join - Read join messages.
# simpleproxychat.read.leave - Read leave messages.
# simpleproxychat.read.fake - Read the fake join/leave messages. Players must also have the REAL join/leave permission.
# simpleproxychat.read.switch - Read switch messages.
# simpleproxychat.reload - ALWAYS Active whether use-permissions is false or not.
use-permissions: false

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

# DO NOT TOUCH THIS
file-version: 8
```

**messages.yml**
```YAML
# ==========================================================
#                       INFORMATION
#                 HEX Values are Supported
#  Example: <#FFFFFF>Some text</#FFFFFF> this is a message!
#         Supports Mini-Message/Legacy Color Codes
# ==========================================================

# Minecraft Stuff
minecraft:
  join:
    use: true
    message: "&e%player% &ahas joined the network. (%server%)"
  leave:
    use: true
    message: "&e%player% &chas left the network. (%server%)"
  message: "&8[&3%server%&8] &e%player% &9» &7%message%"
  discord:
    message: "**%server%** %player% » %message%"
    embed:
      use: false
      title: "[%server%] %player%"
      message: "%message%"
      color: "#FFC0CB"
      use-timestamp: true
  switch:
    use: true
    default: "&e%player% &7moved from &c%from% &7to &a%to%&7."
    no-from: "&e%player% &7moved &7to &a%to%&7."
  successful-reload: "&aThe plugin has been successfully reloaded!"
  no-permission: "&cSorry, you do not have permission to run this command."

# Discord Stuff
discord:
  join:
    use: true
    message: "%player% has joined the network. (%server%)"
    use-timestamp: true
  leave:
    use: true
    message: "%player% has left the network. (%server%)"
    use-timestamp: true
  switch:
    use: true
    message: "%player% has switched from %from% to %to%."
    use-timestamp: true
  minecraft-message: "&8[&bDiscord&8] %role% &f%user% &9» &7%message%"
  topic:
    online: "There are %online% players online."
    offline: "The proxy is offline."
  proxy-status:
    enabled: "✅ Proxy enabled!"
    disabled: "⛔ Proxy disabled."
    title: "Server Status"
    message: "Status: "
    online: "Online ✅"
    offline: "Offline ⛔"
    use-timestamp: true

# DO NOT TOUCH THIS
file-version: 4
```

---

<p align="center">
  <img src="https://github.com/beanbeanjuice/SimpleProxyChat/blob/master/Images/Finished/Commands.png?raw=true" alt="commands"/>
</p>

* `/spc-reload` - Reloads the config files.

---

<p align="center">
  <img src="https://github.com/beanbeanjuice/SimpleProxyChat/blob/master/Images/Finished/Permissions.png?raw=true" alt="permissions"/>
</p>

* `simpleproxychat.read.join` - Read join messages.
* `simpleproxychat.read.leave` - Read leave messages.
* `simpleproxychat.read.fake` - Read fake join/leave messages. Must have the real permission too.
* `simpleproxychat.read.switch` - Read switch messages.
* `simpleproxychat.read.update` - Read update notifications.
* `simpleproxychat.reload` - Reload the config.

---

<p align="center">
  <img src="https://github.com/beanbeanjuice/SimpleProxyChat/blob/master/Images/Finished/Placeholders.png?raw=true" alt="placeholders"/>
</p>

* `%server%` - The current connected server. Uses the alias if one is specified.
* `%original_server%` - Same as `%server%`, but does not use the alias.
* `%to%` - The current connected server. Uses the alias if one is specified.
* `%original_to%` - Same as `%to%`, but does not use the alias.
* `%from%` - The server the player just disconnected from. Uses the alias if one is specified.
* `%original_from%` - Same as `%from%`, but does not use the alias.
* `%player%` - The player's Minecraft username.
* `%user%` - The player's Discord username.
* `%role%` - The player's Discord role.
* `%prefix%` - The player's prefix. **LuckPerms Only**
* `%suffix%` - The player's suffix. **LuckPerms Only**
* `%message%` - The player's message.
* `%epoch%` - This get's the current time (in milliseconds). Formats can be used like [this](https://gist.github.com/LeviSnoot/d9147767abeef2f770e9ddcd91eb85aa). An example would be `<t:%epoch%>`. **Discord Only**
* `%time%` - Similar to `%epoch%`, but uses a special formatting and timezone found in `config.yml`. **Discord and Minecraft**

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
