import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

// Supported MC range: 26.1.2 → 26.3 (and newer 26.x), delivered as a SINGLE jar.
//
// We compile against the OLDEST version in the range (NeoForge 26.1.2.78 ↔ MC 26.1.2) so
// the mod can only reference APIs that exist on every supported server — the same strategy
// the Paper/Spigot helper uses with its pinned 1.21.11 compileOnly dep. NeoForge's event
// bus, @Mod constructor-injection, ServerChatEvent#getRawText()/getPlayer(), FMLPaths and
// NeoForge.EVENT_BUS have not changed signatures across 26.1 → 26.3, so the compiled jar
// loads unchanged on 26.1.2, 26.2 and 26.3 servers; the version-range gate in
// neoforge.mods.toml enforces the lower bound at mod-load time.
//
// Upgrade this (lower-bound bump), as well as the two versionRange entries in
// META-INF/neoforge.mods.toml. The 26.1.x line is the first NeoForge release that targets
// Java 25 (MC 26.1.2's own metadata declares `javaVersion.majorVersion = 25`), so the
// Java 25 toolchain is correct for every supported MC version in the range.
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
