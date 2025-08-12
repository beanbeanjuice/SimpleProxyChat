rootProject.name = "SimpleProxyChat"

include(
    "projects:common",

    "projects:proxy:velocity",

    "projects:server:spigot"
)

project(":projects:proxy:velocity").name = "SimpleProxyChat-Velocity"
project(":projects:server:spigot").name = "SimpleProxyChatHelper-Spigot"
