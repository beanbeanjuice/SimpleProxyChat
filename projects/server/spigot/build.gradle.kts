import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    implementation(project(":projects:server:shared"))

    // JSON for TCP protocol (provided at runtime by Paper/Spigot).
    compileOnly("com.google.code.gson:gson:2.10.1")

    // GSON Used for tests only.
    testImplementation("com.google.code.gson:gson:2.11.0")
}

tasks.named<ProcessResources>("processResources") {
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
    inputs.property("version", project.version)
}

// Keep gson out of the shaded jar
tasks.withType<ShadowJar> {
    dependencies {
        exclude(dependency("com.google.code.gson:gson:.*"))
    }
}
