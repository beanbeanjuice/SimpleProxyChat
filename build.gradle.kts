import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.papermc.hangarpublishplugin.HangarPublishTask
import io.papermc.hangarpublishplugin.model.Platforms
import java.io.ByteArrayOutputStream

plugins {
    id("com.github.johnrengelman.shadow") version("8.1.1")
    id("java")
    id("io.papermc.hangar-publish-plugin") version("0.1.2")
}

group = "com.beanbeanjuice"
version = "0.3.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()

    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }

    maven {
        name = "jitpack"
        url = uri("https://jitpack.io")
    }

    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    // Velocity
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")

    // Bungee
    compileOnly("net.md-5:bungeecord-api:1.20-R0.1-SNAPSHOT")
    implementation("net.kyori:adventure-api:4.16.0")  // Convert Velocity -> Bungee
    implementation("net.kyori:adventure-text-minimessage:4.16.0")  // Convert Velocity -> Bungee
    implementation("net.kyori:adventure-text-serializer-plain:4.16.0")  // Convert Velocity -> Bungee
    implementation("net.kyori:adventure-text-serializer-legacy:4.16.0")  // Convert Velocity -> Bungee
    implementation("net.kyori:adventure-text-serializer-gson:4.16.0")  // Convert Velocity -> Bungee
    implementation("net.kyori:adventure-text-serializer-bungeecord:4.3.2")  // Convert Velocity -> Bungee

    // Discord Support
    implementation("net.dv8tion:JDA:5.0.0-beta.20") {
        exclude(module = "opus-java")
    }

    // PremiumVanish/SuperVanish Support
    compileOnly("com.github.LeonMangler:PremiumVanishAPI:2.9.0-4")

    // Better YAML Support
    implementation("dev.dejvokep:boosted-yaml:1.3.1")

    // bStats
    implementation("org.bstats:bstats-velocity:3.0.2")
    implementation("org.bstats:bstats-bungeecord:3.0.2")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    // LuckPerms Support
    compileOnly("net.luckperms:api:5.4")

    // LiteBans Support
    compileOnly("com.gitlab.ruany", "LiteBansAPI", "0.5.0")

    // Timestamp
    implementation("joda-time:joda-time:2.12.7")
}

configure<ProcessResources>("processResources") {
    filesMatching("bungee.yml") {
        expand(project.properties)
    }
    filesMatching("velocity-plugin.json") {
        expand(project.properties)
    }
}

inline fun <reified C> Project.configure(name: String, configuration: C.() -> Unit) {
    (this.tasks.getByName(name) as C).configuration()
}

// Helper methods
fun executeGitCommand(vararg command: String): String {
    val byteOut = ByteArrayOutputStream()
    exec {
        commandLine = listOf("git", *command)
        standardOutput = byteOut
    }
    return byteOut.toString(Charsets.UTF_8.name()).trim()
}

fun latestCommitMessage(): String {
    return executeGitCommand("log", "-1", "--pretty=%B")
}

val versionString: String = (version as String) + (System.getenv("DEVELOPMENT_STRING") ?: "")
val isRelease: Boolean = !versionString.contains('-')

val suffixedVersion: String = if (isRelease) {
    versionString
} else {
    // Give the version a unique name by using the GitHub Actions run number
    versionString + "+" + System.getenv("GITHUB_RUN_NUMBER")
}

// Use the commit description for the changelog
val changelogContent: String = latestCommitMessage()

// If you would like to publish releases with their proper changelogs manually, simply add an if statement with the `isRelease` variable here.
hangarPublish {
    publications.register("plugin") {
        version.set(suffixedVersion)
        channel.set(if (isRelease) "Release" else "Development")
        changelog.set(changelogContent)
        id.set(rootProject.name)
        apiKey.set(System.getenv("HANGAR_API_TOKEN"))
        platforms {

            // Velocity
            register(Platforms.VELOCITY) {
                // Set the jar file to upload
                jar.set(tasks.shadowJar.flatMap { it.archiveFile })

                // Set platform versions from gradle.properties file
                val versions: List<String> = (property("velocityVersion") as String)
                        .split(",")
                        .map { it.trim() }
                platformVersions.set(versions)

                dependencies {
                    url("PremiumVanish", "https://www.spigotmc.org/resources/premiumvanish-stay-hidden-bungee-velocity-support.14404/") {
                        required.set(false)
                    }

                    url("SuperVanish", "https://www.spigotmc.org/resources/supervanish-be-invisible.1331/") {
                        required.set(false)
                    }

                    url("LuckPerms", "https://luckperms.net") {
                        required.set(false)
                    }

                    url("LiteBans", "https://www.spigotmc.org/resources/litebans.3715/") {
                        required.set(false)
                    }
                }
            }

            // Waterfall
            register(Platforms.WATERFALL) {
                // Set the jar file to upload
                jar.set(tasks.shadowJar.flatMap { it.archiveFile })

                // Set platform versions from gradle.properties file
                val versions: List<String> = (property("waterfallVersion") as String)
                        .split(",")
                        .map { it.trim() }
                platformVersions.set(versions)

                dependencies {
                    url("PremiumVanish", "https://www.spigotmc.org/resources/premiumvanish-stay-hidden-bungee-velocity-support.14404/") {
                        required.set(false)
                    }

                    url("SuperVanish", "https://www.spigotmc.org/resources/supervanish-be-invisible.1331/") {
                        required.set(false)
                    }

                    url("LuckPerms", "https://luckperms.net") {
                        required.set(false)
                    }

                    url("LiteBans", "https://www.spigotmc.org/resources/litebans.3715/") {
                        required.set(false)
                    }
                }
            }
        }
    }
}

tasks.withType<ShadowJar> {
    minimize()
    relocate("org.bstats", "com.beanbeanjuice.simpleproxychat.libs.org.bstats")
    archiveBaseName.set(rootProject.name)
    archiveClassifier.set("")
    archiveVersion.set(version as String)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
