plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.2.3"
    java
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
}

group = "fr.skyost"
val grp = group
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
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit")
    compileOnly("com.eclipsesource.minimal-json:minimal-json:0.9.5")
    compileOnly(platform("com.intellectualsites.bom:bom-newest:1.42"))

    implementation("net.kyori:adventure-text-minimessage:4.16.0")
    implementation("org.zeroturnaround:zt-zip:1.14")
}

tasks {
    register<Zip>("createZip") {
        from("schematics")
        archiveFileName.set("schematics.zip")
        destinationDirectory.set(File("src/main/resources"))
    }

    processResources {
        dependsOn("createZip")
    }

    shadowJar {
        archiveClassifier = ""
        archiveVersion = ""
        mergeServiceFiles()
        dependencies {
            include(dependency("org.zeroturnaround::"))
            include(dependency("net.kyori:adventure-text-minimessage"))
            relocate("org.zeroturnaround.zip", "$grp.zip")
            relocate("net.kyori.adventure.text.minimessage", "$grp.minimessage")
        }
    }
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.20.4")
    }
}