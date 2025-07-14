plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.plugin.serialization)
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.kapt")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

    group = "net.deechael"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven {
            name = "papermc"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
        maven {
            url = uri("https://repo.codemc.io/repository/maven-releases/")
        }
        maven {
            url = uri("https://repo.codemc.io/repository/maven-snapshots/")
        }
        maven {
            url = uri("https://repo.extendedclip.com/releases/")
        }
        maven("https://repo.momirealms.net/releases/")
        maven("https://maven.nostal.ink/repository/maven-public")
        maven("https://maven.elytrium.net/repo/")
        maven("https://repo.minebench.de/")
        maven("https://repo.rosewooddev.io/repository/public/")
        maven("https://jitpack.io")
    }

    dependencies {
    }

    kotlin {
        jvmToolchain(21)
    }
}