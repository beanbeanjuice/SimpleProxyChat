import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.withType

dependencies {
    // BungeeCord Sockets
    compileOnly("net.md-5", "bungeecord-api", "1.21-R0.3") // https://javadoc.io/doc/net.md-5/bungeecord-api/latest/index.html
    testImplementation("net.md-5", "bungeecord-api", "1.21-R0.3") // https://javadoc.io/doc/net.md-5/bungeecord-api/latest/index.html



    // Timestamp
    implementation("joda-time", "joda-time", "2.14.0")
}

tasks.withType<ShadowJar> {
    relocate("net.dv8tion", "com.beanbeanjuice.simpleproxychat.libs.net.dv8tion")
    relocate("joda-time", "com.beanbeanjuice.simpleproxychat.libs.joda-time")  // check
}
