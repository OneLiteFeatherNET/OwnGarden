plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    java
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
}

group = "fr.skyost"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.codemc.org/repository/maven-public")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit-lite:1.5")
    implementation("org.zeroturnaround:zt-zip:1.14")
    implementation("com.eclipsesource.minimal-json:minimal-json:0.9.5")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.0")
}

tasks {
    shadowJar {
        mergeServiceFiles()
        relocate("org.bstats.bukkit", "fr.skyost.owngarden.util.bstats")
    }
    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
}