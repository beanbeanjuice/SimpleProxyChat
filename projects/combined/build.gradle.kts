import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    implementation(project(":projects:proxy:SimpleProxyChat-Velocity"))
    implementation(project(":projects:proxy:SimpleProxyChat-BungeeCord"))
    implementation(project(":projects:server:SimpleProxyChatHelper-Spigot"))
}

tasks.named<ShadowJar>("shadowJar") {
    relocate("dev.dejvokep", "com.beanbeanjuice.simpleproxychat.libs.dev.dejvokep")
    relocate("org.bstats", "com.beanbeanjuice.simpleproxychat.libs.org.bstats")
    relocate("org.apache.maven", "com.beanbeanjuice.simpleproxychat.libs.org.apache.maven")

    minimize {
        exclude(project(":projects:proxy:SimpleProxyChat-Velocity"))
        exclude(project(":projects:proxy:SimpleProxyChat-BungeeCord"))
        exclude(project(":projects:proxy:shared"))
        exclude(project(":projects:server:SimpleProxyChatHelper-Spigot"))
        exclude(project(":projects:server:shared"))
        exclude(project(":projects:common"))
    }
}
