import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

allprojects {
    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    repositories {
        mavenCentral()
        maven {
            name = "papermc-repo"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
    }

    // Fabric / NeoForge have their own platform-specific setup via fabric-loom / NeoGradle;
    if (name == "SimpleProxyChatHelper-Fabric") return@allprojects
    if (name == "SimpleProxyChatHelper-NeoForge") return@allprojects

    dependencies {
        implementation(project(":projects:common"))

        // Spigot
        compileOnly("org.spigotmc:spigot-api:1.21.11-R0.1-SNAPSHOT")
        testImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")

        // PlaceholderAPI
        compileOnly("me.clip:placeholderapi:2.11.6")

        // bStats
        implementation("org.bstats:bstats-bukkit:3.1.0")

        // Mock Testing
        testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.116.3")

        // Artifact Version Comparison
        // TODO: Eventually remove this.
        implementation("org.apache.maven:maven-artifact:3.9.11")
    }

    tasks.withType<ShadowJar> {
        relocate("org.bstats", "com.beanbeanjuice.simpleproxychathelper.libs.org.bstats")
        relocate("org.apache.maven", "com.beanbeanjuice.simpleproxychathelper.libs.org.apache.maven")
    }
}
