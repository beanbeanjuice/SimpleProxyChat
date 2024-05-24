import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version("8.1.1")
    id("java")
}

group = "com.beanbeanjuice"
version = "0.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()

    maven {
        name = "spigotmc-repo"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }

    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }

    maven {
        name = "jitpack"
        url = uri("https://jitpack.io/")
    }
}

dependencies {
    // Spigot. Duh.
    compileOnly("org.spigotmc", "spigot-api", "1.20.6-R0.1-SNAPSHOT")

    // Vault
    compileOnly("com.github.MilkBowl", "VaultAPI", "1.7")

    // TODO: bstats

    // Lombok
    compileOnly("org.projectlombok", "lombok", "1.18.32")
    annotationProcessor("org.projectlombok", "lombok", "1.18.32")
}

configure<ProcessResources>("processResources") {
    filesMatching("plugin.yml") {
        expand(project.properties)
    }
}

inline fun <reified C> Project.configure(name: String, configuration: C.() -> Unit) {
    (this.tasks.getByName(name) as C).configuration()
}

tasks.withType<ShadowJar> {
    minimize()
    archiveBaseName.set(rootProject.name)
    archiveClassifier.set("")
    archiveVersion.set(version as String)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
