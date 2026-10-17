import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.withType

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

dependencies {
    implementation(project(":projects:proxy:shared"))

    compileOnly("com.velocitypowered:velocity-api:4.2.0")
    testImplementation("com.velocitypowered:velocity-api:4.2.0")
    annotationProcessor("com.velocitypowered:velocity-api:4.2.0")
}

tasks.named<ProcessResources>("processResources") {
    filesMatching("velocity-plugin.json") {
        expand("version" to project.version)
    }
    inputs.property("version", project.version)
}

tasks.withType<ShadowJar> {
    relocate("org.bstats", "com.beanbeanjuice.simpleproxychat.libs.org.bstats")
}
