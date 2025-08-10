import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

version = project.property("version") as String

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    // Velocity
    compileOnly("com.velocitypowered", "velocity-api", "3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered", "velocity-api", "3.4.0-SNAPSHOT")

    // Bungee
    compileOnly("net.md-5", "bungeecord-api", "1.21-R0.3") // https://javadoc.io/doc/net.md-5/bungeecord-api/latest/index.html
    implementation("net.kyori", "adventure-api", "4.24.0")  // Convert Velocity -> Bungee https://mvnrepository.com/artifact/net.kyori/adventure-api
    implementation("net.kyori", "adventure-text-minimessage", "4.24.0")  // Convert Velocity -> Bungee https://mvnrepository.com/artifact/net.kyori/adventure-text-minimessage
    implementation("net.kyori", "adventure-text-serializer-plain", "4.24.0")  // Convert Velocity -> Bungee https://mvnrepository.com/artifact/net.kyori/adventure-text-serializer-plain
    implementation("net.kyori", "adventure-text-serializer-legacy", "4.24.0")  // Convert Velocity -> Bungee https://mvnrepository.com/artifact/net.kyori/adventure-text-serializer-legacy
    implementation("net.kyori", "adventure-text-serializer-gson", "4.24.0")  // Convert Velocity -> Bungee https://mvnrepository.com/artifact/net.kyori/adventure-text-serializer-gson
    implementation("net.kyori", "adventure-text-serializer-bungeecord", "4.4.1")  // Convert Velocity -> Bungee https://mvnrepository.com/artifact/net.kyori/adventure-platform-bungeecord

    // Discord Support
    implementation("net.dv8tion", "JDA", "5.6.1") {
        exclude(module = "opus-java")
    }

    // PremiumVanish/SuperVanish Support
    compileOnly("com.github.LeonMangler", "PremiumVanishAPI", "2.9.18-2")

    // Better YAML Support
    implementation("dev.dejvokep", "boosted-yaml", "1.3.7")

    // bStats
    implementation("org.bstats", "bstats-velocity", "3.1.0")
    implementation("org.bstats", "bstats-bungeecord", "3.1.0")

    // LuckPerms Support
    compileOnly("net.luckperms", "api", "5.4")

    // LiteBans Support
    compileOnly("com.gitlab.ruany", "LiteBansAPI", "0.6.1")

    // AdvancedBan Support
    compileOnly("com.github.DevLeoko", "AdvancedBan", "v2.3.0")

    // NetworkManager Support
    compileOnly("nl.chimpgamer.networkmanager", "api", "2.17.9")

    // Spicord Support
    compileOnly("org.spicord", "spicord-common", "5.7.2")

    // Timestamp
    implementation("joda-time", "joda-time", "2.14.0")

    // Artifact Version Comparison
    // TODO: Eventually remove this.
    implementation("org.apache.maven", "maven-artifact", "3.9.11")
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

tasks.withType<ShadowJar> {
    relocate("net.dv8tion", "com.beanbeanjuice.simpleproxychat.libs.net.dv8tion")
    relocate("dev.dejvokep", "com.beanbeanjuice.simpleproxychat.libs.dev.dejvokep")
    relocate("org.bstats", "com.beanbeanjuice.simpleproxychat.libs.org.bstats")
    relocate("joda-time", "com.beanbeanjuice.simpleproxychat.libs.joda-time")  // check
    relocate("org.apache.maven", "com.beanbeanjuice.simpleproxychat.libs.org.apache.maven")  // check
}
