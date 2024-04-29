import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.2.3"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    java
    id("io.papermc.hangar-publish-plugin") version "0.1.2"
    id("com.modrinth.minotaur") version "2.+"
}

group = "fr.skyost"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.codemc.org/repository/maven-public")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit")
    compileOnly("com.eclipsesource.minimal-json:minimal-json:0.9.5")
    compileOnly(platform("com.intellectualsites.bom:bom-newest:1.42"))

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
        mergeServiceFiles()
    }

    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.20.4")
    }
}

bukkit {
    name = "OwnGarden"
    main = "fr.skyost.owngarden.OwnGarden"
    description = "Grow custom trees !"
    defaultPermission = BukkitPluginDescription.Permission.Default.OP
    apiVersion = "1.19"
    authors = listOf("TheMeinerLP", "OneLiteFeatherNET", "ElloWorld", "Romindous")
    depend = listOf("FastAsyncWorldEdit")
    website = "https://github.com/OneLiteFeatherNET/OwnGarden"
    commands {
        register("owngarden") {
            aliases = listOf("own-garden")
            description = "Main command of OwnGarden."
            permission = "owngarden.command"
            usage = "/owngarden"
        }
    }
    permissions {
        register("owngarden.command") {
            default = BukkitPluginDescription.Permission.Default.OP
            description = "Allows you to use /owngarden."
        }
    }
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    versionNumber.set(rootProject.version.toString())
    projectId.set("lI72N7Bx")
    versionType.set("release")
    uploadFile.set(tasks.shadowJar)
    gameVersions.addAll(listOf("1.20.5","1.20.4","1.20.3", "1.20.2", "1.20.1", "1.20", "1.19.4", "1.19.3", "1.19.2", "1.19.1", "1.19"))
    loaders.add("paper")
}

hangarPublish {
    publications.register("OwnGarden") {
        version.set(rootProject.version.toString())
        channel.set("Release")
        apiKey.set(System.getenv("HANGAR_KEY"))
        id.set("OwnGarden")
        platforms {
            paper {
                jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                platformVersions.set(listOf("1.20.5","1.20.4","1.20.3", "1.20.2", "1.20.1", "1.20", "1.19.4", "1.19.3", "1.19.2", "1.19.1", "1.19"))
            }
        }
    }
}