import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

version = "0.0.3"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    // Spigot. Duh.
    compileOnly("org.spigotmc", "spigot-api", "1.20.6-R0.1-SNAPSHOT")

    // PlaceholderAPI
    compileOnly("me.clip", "placeholderapi", "2.11.6")

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
    relocate("org.bstats", "com.beanbeanjuice.simpleproxychathelper.libs.org.bstats")
    relocate("org.apache.maven", "com.beanbeanjuice.simpleproxychathelper.libs.org.apache.maven")
}
