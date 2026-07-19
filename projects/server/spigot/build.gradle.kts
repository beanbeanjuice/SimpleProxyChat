dependencies {
    implementation(project(":projects:server:shared"))

    // Spigot. Duh.
    compileOnly("org.spigotmc", "spigot-api", "1.21.8-R0.1-SNAPSHOT")

    // PlaceholderAPI
    compileOnly(files("libs/PlaceholderAPI-2.11.6.jar"))

    // bStats
    implementation("org.bstats", "bstats-bukkit", "3.1.0")

    // Mock Testing
    testImplementation("org.mockbukkit.mockbukkit", "mockbukkit-v1.21", "4.72.6")
}
