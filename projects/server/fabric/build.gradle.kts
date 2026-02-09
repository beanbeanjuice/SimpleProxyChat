plugins {
    id("fabric-loom") version "1.7-SNAPSHOT"
    id("maven-publish")
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.1")
    mappings("net.fabricmc:yarn:1.21.1+build.3:v2")
    modImplementation("net.fabricmc:fabric-loader:0.16.5")

    modImplementation("net.fabricmc.fabric-api:fabric-api:0.102.1+1.21.1")
    
    implementation(project(":projects:server:shared"))
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}
