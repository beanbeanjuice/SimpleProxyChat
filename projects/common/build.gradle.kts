java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation("net.kyori", "adventure-api", "4.24.0")  // Convert Velocity -> Bungee https://mvnrepository.com/artifact/net.kyori/adventure-api
    implementation("net.kyori", "adventure-text-minimessage", "4.24.0")  // Convert Velocity -> Bungee https://mvnrepository.com/artifact/net.kyori/adventure-text-minimessage
    implementation("net.kyori", "adventure-text-serializer-plain", "4.24.0")  // Convert Velocity -> Bungee https://mvnrepository.com/artifact/net.kyori/adventure-text-serializer-plain
    implementation("net.kyori", "adventure-text-serializer-legacy", "4.24.0")  // Convert Velocity -> Bungee https://mvnrepository.com/artifact/net.kyori/adventure-text-serializer-legacy
    implementation("net.kyori", "adventure-text-serializer-gson", "4.24.0")  // Convert Velocity -> Bungee https://mvnrepository.com/artifact/net.kyori/adventure-text-serializer-gson
    implementation("net.kyori", "adventure-text-serializer-bungeecord", "4.4.1")  // Convert Velocity -> Bungee https://mvnrepository.com/artifact/net.kyori/adventure-platform-bungeecord

    // Artifact Version Comparison
    // TODO: Eventually remove this.
    implementation("org.apache.maven", "maven-artifact", "3.9.11")
}
