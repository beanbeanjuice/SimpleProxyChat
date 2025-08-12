rootProject.name = "SimpleProxyChat"

include(
    "projects:common",

    "projects:proxy:shared",
    "projects:proxy:velocity",
    "projects:proxy:bungeecord",

    "projects:server:shared",
    "projects:server:spigot"
)

project(":projects:proxy:velocity").name = "SimpleProxyChat-Velocity"
project(":projects:proxy:bungeecord").name = "SimpleProxyChat-BungeeCord"
project(":projects:server:spigot").name = "SimpleProxyChatHelper-Spigot"
