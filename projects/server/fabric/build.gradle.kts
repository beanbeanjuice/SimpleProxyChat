val minecraftVersion = "26.3"
val fabricLoaderVersion = "0.19.5"
val fabricApiVersion = "0.163.0+"

plugins {
    // https://maven.fabricmc.net/net/fabricmc/fabric-loom/
    id("net.fabricmc.fabric-loom") version "1.19.1"
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

repositories {
    maven { url = uri("https://maven.fabricmc.net/") }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")

    implementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")

    compileOnly("com.google.code.gson:gson:2.14.0")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

tasks.named<Jar>("jar") {
    archiveBaseName.set("SimpleProxyChatHelper-Fabric")
    archiveClassifier.set("")
    destinationDirectory.set(rootProject.layout.projectDirectory.dir("libs"))
}

// Disable shadowJar for this subproject.
tasks.named("shadowJar") {
    enabled = false
}

tasks.named<ProcessResources>("processResources") {
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
    inputs.property("version", project.version)
}
