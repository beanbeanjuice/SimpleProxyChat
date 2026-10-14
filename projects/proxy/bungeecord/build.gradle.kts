import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.withType

dependencies {
    implementation(project(":projects:proxy:shared"))
}

tasks.named<ProcessResources>("processResources") {
    filesMatching("bungee.yml") {
        expand("version" to project.version)
    }
}

tasks.withType<ShadowJar> {
    relocate("org.bstats", "com.beanbeanjuice.simpleproxychat.libs.org.bstats")
}
