plugins {
    id("net.minecraftforge.gradle") version "6.0.+"
    id("org.spongepowered.mixin") version "0.7.+"
}

minecraft {
    mappings(channel: "official", version: "1.21.1")
}

dependencies {
    minecraft("net.minecraftforge:forge:1.21.1-52.0.0")
    implementation(project(":projects:server:shared"))
}
