pluginManagement {
    repositories {
        maven { url = uri("https://maven.fabricmc.net/") }
        maven { url = uri("https://maven.neoforged.net/releases") }
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" // Auto provision the JDK toolchain for NeoForge helper. Fails without it if its not installed locally.
}

rootProject.name = "SimpleProxyChat"

include(
    "projects:common",

    "projects:proxy:shared",
    "projects:proxy:velocity",
    "projects:proxy:bungeecord",

    "projects:server:shared",
    "projects:server:spigot",
    "projects:server:fabric",
    "projects:server:neoforge",

    "projects:combined"
)

project(":projects:proxy:velocity").name = "SimpleProxyChat-Velocity"
project(":projects:proxy:bungeecord").name = "SimpleProxyChat-BungeeCord"
project(":projects:server:spigot").name = "SimpleProxyChatHelper-Spigot"
project(":projects:server:fabric").name = "SimpleProxyChatHelper-Fabric"
project(":projects:server:neoforge").name = "SimpleProxyChatHelper-NeoForge"
project(":projects:combined").name = "SimpleProxyChat"
