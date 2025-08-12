import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.withType

dependencies {
    implementation(project(":projects:proxy:shared"))

    compileOnly("com.velocitypowered", "velocity-api", "3.4.0-SNAPSHOT")
    testImplementation("com.velocitypowered", "velocity-api", "3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered", "velocity-api", "3.4.0-SNAPSHOT")
}

tasks.withType<ShadowJar> {
    relocate("org.bstats", "com.beanbeanjuice.simpleproxychat.libs.org.bstats")
}
