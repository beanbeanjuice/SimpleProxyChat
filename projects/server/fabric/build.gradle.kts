// Fabric helper (SimpleProxyChatHelper-Fabric): targets MC 26.1+ only.
// Minecraft is unobfuscated from 26.1 onwards, so Loom uses Mojang mappings by
// default and no remap step is needed. Bump these to jump versions:
val minecraftVersion = "26.3"
val fabricLoaderVersion = "0.19.5"
val fabricApiVersion = "0.162.0+26.3"

plugins {
    // https://maven.fabricmc.net/net/fabricmc/fabric-loom/
    // NOTE: "fabric-loom" (short) is the classic remapping plugin.
    // "net.fabricmc.fabric-loom" (full) is the no-remap marker plugin required
    // for unobfuscated Minecraft (26.1+) — it auto-sets disableObfuscation=true
    // and skips the mappings requirement.
    id("net.fabricmc.fabric-loom") version "1.17.20"
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
    // No mappings() line — the "net.fabricmc.fabric-loom" plugin marks the
    // project as no-remap, so Loom does not require (or accept) a mappings dep.

    // implementation (not modImplementation): no remap is applied to these; the
    // classes ship with the deployed jar as-is.
    implementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")

    compileOnly("com.google.code.gson:gson:2.10.1")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

// The no-remap Loom plugin does not create a `remapJar` task — the standard
// `jar` output is the deployable. Route it to the root libs/ directory so
// buildAll picks it up.
tasks.named<Jar>("jar") {
    archiveBaseName.set("SimpleProxyChatHelper-Fabric")
    archiveClassifier.set("")
    destinationDirectory.set(rootProject.layout.projectDirectory.dir("libs"))
}

// Disable shadowJar for this subproject.
//
// The root build applies `com.gradleup.shadow` to every subproject and turns
// on `minimize()` for all ShadowJar tasks. For this module the shaded jar
// is never used — the Fabric deployable is the plain `jar` task above
// (buildAll depends on `:...SimpleProxyChatHelper-Fabric:jar`, not shadowJar).
//
// `minimize()` runs jdependency's Clazzpath over the full runtime
// classpath, and jdependency bundles its own (shaded) ASM that does not
// recognise Java 27 bytecode. Minecraft 26.3 transitively pulls in
// lwjgl 3.4.3, which is a multi-release jar containing Java 27 class
// files under `META-INF/versions/27/` (e.g. `MemoryBackendFFM27`), so
// the scan dies with "Unsupported class file major version 71".
//
// Disabling the task here side-steps the broken scan without affecting
// any other module (every other subproject's shadowJar still runs
// normally when the root `gradle shadowJar` task fans out).
tasks.named("shadowJar") {
    enabled = false
}

tasks.named<ProcessResources>("processResources") {
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
    inputs.property("version", project.version)
}
