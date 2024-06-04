import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version("8.1.1")
    id("java")
}

group = "com.beanbeanjuice"
version = "0.0.2"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
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
        name = "placeholder-api-repo"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }

    maven {
        name = "jitpack"
        url = uri("https://jitpack.io/")
    }
}

dependencies {
    // Spigot. Duh.
    compileOnly("org.spigotmc", "spigot-api", "1.20.6-R0.1-SNAPSHOT")

    // PlaceholderAPI
    compileOnly("me.clip", "placeholderapi", "2.11.6")

    // Lombok
    compileOnly("org.projectlombok", "lombok", "1.18.32")
    annotationProcessor("org.projectlombok", "lombok", "1.18.32")

    // bStats
    implementation("org.bstats", "bstats-bukkit", "3.0.2")

    // Artifact Version Comparison
    implementation("org.apache.maven", "maven-artifact", "3.9.7")
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
    relocate("org.bstats", "com.beanbeanjuice.simpleproxychathelper.libs.org.bstats")
    relocate("org.apache.maven", "com.beanbeanjuice.simpleproxychathelper.libs.org.apache.maven")
    archiveBaseName.set(rootProject.name)
    archiveClassifier.set("")
    archiveVersion.set(version as String)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
