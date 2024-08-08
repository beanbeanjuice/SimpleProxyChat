import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("io.github.goooler.shadow") version("8.1.7")
    id("java")
}

allprojects {
    group = "com.beanbeanjuice"

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
        compileOnly("org.projectlombok", "lombok", "1.18.32")
        annotationProcessor("org.projectlombok", "lombok", "1.18.32")
    }
}

subprojects {
    apply(plugin = "io.github.goooler.shadow")

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
