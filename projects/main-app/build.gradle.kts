import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.8.22" // Замените на актуальную версию Kotlin
    id("com.github.johnrengelman.shadow") version "8.1.1" // Замените на актуальную версию плагина ShadowJar
}

version = "0.5.7"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    // Velocity
    compileOnly("com.velocitypowered", "velocity-api", "3.3.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered", "velocity-api", "3.3.0-SNAPSHOT")

    // Bungee
    compileOnly("net.md-5", "bungeecord-api", "1.20-R0.2")
    implementation("net.kyori", "adventure-api", "4.17.0")
    implementation("net.kyori", "adventure-text-minimessage", "4.17.0")
    implementation("net.kyori", "adventure-text-serializer-plain", "4.17.0")
    implementation("net.kyori", "adventure-text-serializer-legacy", "4.17.0")
    implementation("net.kyori", "adventure-text-serializer-gson", "4.17.0")
    implementation("net.kyori", "adventure-text-serializer-bungeecord", "4.3.4")

    // Discord Support
    implementation("net.dv8tion", "JDA", "5.0.2") {
        exclude(module = "opus-java")
    }

    // PremiumVanish/SuperVanish Support
    compileOnly("com.github.LeonMangler", "PremiumVanishAPI", "2.9.0-4")

    // Better YAML Support
    implementation("dev.dejvokep", "boosted-yaml", "1.3.5")

    // bStats
    implementation("org.bstats", "bstats-velocity", "3.0.2")
    implementation("org.bstats", "bstats-bungeecord", "3.0.2")

    // LuckPerms Support
    compileOnly("net.luckperms", "api", "5.4")

    // LiteBans Support
    compileOnly("com.gitlab.ruany", "LiteBansAPI", "0.5.0")

    // AdvancedBan Support
    compileOnly("com.github.DevLeoko", "AdvancedBan", "v2.3.0")

    // NetworkManager Support
    compileOnly("nl.chimpgamer.networkmanager", "api", "2.14.10")

    // Spicord Support
    compileOnly("org.spicord", "spicord-common", "5.4.0")

    // Timestamp
    implementation("joda-time", "joda-time", "2.12.7")

    // Artifact Version Comparison
    implementation("org.apache.maven", "maven-artifact", "3.9.7")
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
    archiveClassifier.set("") // Устанавливаем пустой классификатор для основного JAR файла
    mergeServiceFiles()
    manifest {
        attributes(
            "Implementation-Title" to "SimpleProxyChat",
            "Implementation-Version" to version,
            "Main-Class" to "com.beanbeanjuice.simpleproxychat.MainClass" // Замените на ваш главный класс
        )
    }

    // Перемещение пакетов для уменьшения конфликтов
    relocate("net.dv8tion", "com.beanbeanjuice.simpleproxychat.libs.net.dv8tion")
    relocate("dev.dejvokep", "com.beanbeanjuice.simpleproxychat.libs.dev.dejvokep")
    relocate("org.bstats", "com.beanbeanjuice.simpleproxychat.libs.org.bstats")
    relocate("joda-time", "com.beanbeanjuice.simpleproxychat.libs.joda-time")
    relocate("org.apache.maven", "com.beanbeanjuice.simpleproxychat.libs.org.apache.maven")
}

tasks.named<Jar>("jar") {
    enabled = false // Отключаем стандартную задачу jar, так как используем ShadowJar
}
