package fr.skyost.owngarden

import com.google.common.base.Joiner
import fr.skyost.owngarden.command.OwnGardenCommand__
import fr.skyost.owngarden.config.PluginConfig__
import fr.skyost.owngarden.listener.GlobalEventsListener__
import fr.skyost.owngarden.util.Skyupdater
import fr.skyost.owngarden.worldedit.WorldEditOperations__
import org.bstats.bukkit.MetricsLite
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.zeroturnaround.zip.ZipUtil
import java.io.File

/**
 * The OwnGarden plugin class.
 */
class OwnGarden__ : JavaPlugin() {
    /**
     * The plugin config.
     */
    var pluginConfig: PluginConfig__? = null

    /**
     * The WorldEdit operations.
     */
    var worldEditOperations: WorldEditOperations__? = null

    override fun onEnable() {
        try {
            /* WORLDEDIT HOOK : */
            if (Bukkit.getPluginManager().getPlugin("WorldEdit") == null) {
                log(ChatColor.RED, "WorldEdit must be installed on your server !")
                Bukkit.getPluginManager().disablePlugin(this)
                return
            }
            worldEditOperations = WorldEditOperations__(this)
            if (!worldEditOperations!!.checkWorldEditVersion()) {
                log(ChatColor.RED, "Incorrect WorldEdit version. Current accepted ones are : " + Joiner.on(", ").join(WorldEditOperations__.WORLDEDIT_VERSIONS) + ".")
                Bukkit.getPluginManager().disablePlugin(this)
                return
            }

            /* CONFIGURATION : */
            log(ChatColor.GOLD, "Loading the configuration...")

            val pluginConfig = PluginConfig__(dataFolder)
            this.pluginConfig = pluginConfig
            pluginConfig.load()
            if (pluginConfig.enableUpdater) {
                Skyupdater(this, 103296, file, true, true)
            }
            if (pluginConfig.enableMetrics) {
                MetricsLite(this)
            }
            log(ChatColor.GOLD, "Configuration loaded !")

            /* EXTRACTING DEFAULT SCHEMATICS IF NEEDED : */
            val shematicsDirectory = File(pluginConfig.schematicsDirectory)
            if (!shematicsDirectory.exists() || !shematicsDirectory.isDirectory) {
                shematicsDirectory.mkdirs()
            }
            if (shematicsDirectory.list()?.size == 0) {
                log(ChatColor.GOLD, "Extracting samples schematics...")
                extractSamples(shematicsDirectory)
                log(ChatColor.GOLD, "Done !")
            }

            /* TESTING SCHEMATICS : */
            log(ChatColor.GOLD, "Testing schematics...")
            val invalidSchematics = worldEditOperations!!.testSchematics()
            if (invalidSchematics.isNotEmpty()) {
                log(ChatColor.RED, "There are some invalid schematics :")
                for (invalidSchematic in invalidSchematics) {
                    log(ChatColor.RED, invalidSchematic)
                    pluginConfig.saplingOakSchematics.remove(invalidSchematic)
                    pluginConfig.saplingSpruceSchematics.remove(invalidSchematic)
                    pluginConfig.saplingBirchSchematics.remove(invalidSchematic)
                    pluginConfig.saplingJungleSchematics.remove(invalidSchematic)
                    pluginConfig.saplingAcaciaSchematics.remove(invalidSchematic)
                    pluginConfig.saplingDarkOakSchematics.remove(invalidSchematic)
                    pluginConfig.mushroomBrownSchematics.remove(invalidSchematic)
                    pluginConfig.mushroomRedSchematics.remove(invalidSchematic)
                }
                log(ChatColor.RED, "They are not going to be used by the plugin. Please fix them and restart your server.")
            } else {
                log(ChatColor.GOLD, "Done, no error.")
            }

            /* REGISTERING EVENTS : */
            Bukkit.getPluginManager().registerEvents(GlobalEventsListener__(this), this)

            /* REGISTERING COMMANDS : */
            getCommand("owngarden")!!.setExecutor(OwnGardenCommand__(this))
            val description = description
            log(ChatColor.RESET, "Enabled " + ChatColor.GREEN + name + " v" + description.version + ChatColor.GOLD + " by " + Joiner.on(' ').join(description.authors) + ChatColor.RESET + " !")
        } catch (ex: Exception) {
            log(ChatColor.RED, "Unable to start the plugin !")
            ex.printStackTrace()
        }
    }

    /**
     * Extracts the samples to the specified directory.
     *
     * @param schematicsDirectory The schematics directory.
     */
    private fun extractSamples(schematicsDirectory: File?) {
        ZipUtil.unpack(file, schematicsDirectory) {
            name: String -> if (name.startsWith("schematics/")) name.replaceFirst("schematics/".toRegex(), "") else null
        }
    }

    /**
     * Logs a message to the console.
     *
     * @param color The color (after the [plugin-name]).
     * @param message The message.
     * @param sender The sender.
     */
    @JvmOverloads
    fun log(color: ChatColor, message: String, sender: CommandSender = Bukkit.getConsoleSender()) {
        sender.sendMessage("[$name] $color$message")
    }
}