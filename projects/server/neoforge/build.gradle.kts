import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val mcVersion = "26.1.2"
val neoForgeVersion = "26.1.2.78" // https://maven.neoforged.net/releases/net/neoforged/neoforge/

plugins {
    id("net.neoforged.gradle.userdev") version "7.1.39"
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

repositories {
    maven { url = uri("https://maven.neoforged.net/releases") }
}

dependencies {
    implementation("net.neoforged:neoforge:$neoForgeVersion")
}

tasks.withType(JavaCompile::class.java) {
    options.compilerArgs.add("--enable-preview"); // Required for some reason.
}

tasks.named<ShadowJar>("shadowJar") {
    configurations = listOf()
    archiveBaseName.set("SimpleProxyChatHelper-NeoForge")
}

tasks.named<ProcessResources>("processResources") {
    filesMatching("META-INF/neoforge.mods.toml") {
        expand("version" to project.version)
    }
    inputs.property("version", project.version)
}
