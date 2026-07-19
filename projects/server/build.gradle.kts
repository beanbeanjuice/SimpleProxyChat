import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import groovy.json.JsonSlurper
import java.nio.file.Paths

val manifestFile = Paths.get(rootDir.path, ".release-please-manifest.json").toFile()
val manifestJson = JsonSlurper().parse(manifestFile) as Map<*, *>
val rootVersion = manifestJson["projects/server"] as String

allprojects {
    version = rootVersion

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }


    dependencies {
        implementation(project(":projects:common"))

        // Artifact Version Comparison
        // TODO: Eventually remove this.
        implementation("org.apache.maven", "maven-artifact", "3.9.11")
    }

    tasks.withType<ShadowJar> {
        relocate("org.bstats", "com.beanbeanjuice.simpleproxychathelper.libs.org.bstats")
        relocate("org.apache.maven", "com.beanbeanjuice.simpleproxychathelper.libs.org.apache.maven")
    }
}

subprojects {
    configure<ProcessResources>("processResources") {
        filesMatching("plugin.yml") {
            expand(project.properties)
        }
    }
}

inline fun <reified C> Project.configure(name: String, configuration: C.() -> Unit) {
    (this.tasks.getByName(name) as C).configuration()
}
