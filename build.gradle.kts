import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.gradleup.shadow") version("9.0.1")
    id("java")
}

allprojects {
    group = "com.beanbeanjuice"
    val mockitoAgent by configurations.creating

    apply(plugin = "java")

    repositories {
        mavenCentral()

        maven {
            name = "sonatype"
            url = uri("https://oss.sonatype.org/content/groups/public/")
        }

        maven {
            name = "jitpack"
            url = uri("https://jitpack.io")
        }

        maven {
            name = "papermc-repo"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }

        maven {
            name = "networkmanager-repo"
            url = uri("https://repo.networkmanager.xyz/repository/maven-public/")
        }

        maven {
            name = "spicord-repo"
            url = uri("https://repo.spicord.org/")
        }

        maven {
            name = "advanced-ban requirement"
            url = uri("https://maven.elmakers.com/repository/")
        }

        maven {
            name = "spigotmc-repo"
            url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        }

        maven {
            name = "placeholder-api-repo"
            url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        }
    }

    dependencies {
        // Lombok
        compileOnly("org.projectlombok", "lombok", "1.18.38")
        annotationProcessor("org.projectlombok", "lombok", "1.18.38")

        // Unit Testing
        testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.13.4") // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api
        testImplementation("org.junit.jupiter", "junit-jupiter", "5.13.4") // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.13.4")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")

        // Mockito
        testImplementation("org.mockito", "mockito-core", "5.18.0") // https://mvnrepository.com/artifact/org.mockito/mockito-core
        testImplementation("org.mockito", "mockito-inline", "+") // https://mvnrepository.com/artifact/org.mockito/mockito-core

        mockitoAgent("org.mockito:mockito-core:5.18.0") {
            isTransitive = false
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()

        // Add the mockito agent as a javaagent JVM argument
        jvmArgs("-javaagent:${mockitoAgent.singleFile.absolutePath}")
    }
//    tasks.withType<Test> {
//        useJUnitPlatform()
//    }
}

subprojects {
    apply(plugin = "com.gradleup.shadow")

    tasks.withType<ShadowJar> {
        minimize()
        archiveClassifier.set("")
        archiveBaseName.set(project.name)
        destinationDirectory.set(File(rootProject.projectDir, "libs"))

        doLast {
            archiveVersion.set(project.version as String)
            println("Compiling: " + project.name + "-" + project.version + ".jar")
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}

tasks.clean {
    doLast {
        file("libs").deleteRecursively()
    }
}
