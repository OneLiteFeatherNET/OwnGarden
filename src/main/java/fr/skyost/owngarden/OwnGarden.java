package fr.skyost.owngarden;

import com.google.common.base.Joiner;
import fr.skyost.owngarden.command.OwnGardenCommand;
import fr.skyost.owngarden.config.PluginConfig;
import fr.skyost.owngarden.listener.GlobalEventsListener;
import fr.skyost.owngarden.worldedit.DefaultUtils;
import fr.skyost.owngarden.worldedit.Utils;
import fr.skyost.owngarden.worldedit.WorldEditUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;

/**
 * The OwnGarden plugin class.
 */

public class OwnGarden extends JavaPlugin {
    /**
     * The plugin config.
     */
    public PluginConfig pluginConfig = null;

    /**
     * The WorldEdit operations.
     */
    public Utils operations = null;

    @Override
    public void onEnable() {
        /* WORLDEDIT HOOK : */
        if (Utils.checkWorldEditVersion(this)) {
            operations = new WorldEditUtils(this);
        } else operations = new DefaultUtils();

        /* CONFIGURATION : */
        log(NamedTextColor.GOLD, "Loading the configuration...");

        this.pluginConfig = new PluginConfig(getDataFolder());
        try {
            pluginConfig.load();
            /*if (pluginConfig.enableUpdater) {
                new Skyupdater(this, 103296, getFile(), true, true);
            }*/ // off for now, no updates D:
            /*if (pluginConfig.enableMetrics) {
                new MetricsLite(this);
            }*/
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
        log(NamedTextColor.GOLD, "Configuration loaded !");

        /* EXTRACTING DEFAULT SCHEMATICS IF NEEDED : */
        final File shematicsDirectory = new File(pluginConfig.schematicsDirectory);
        if (!shematicsDirectory.exists() || !shematicsDirectory.isDirectory()) {
            shematicsDirectory.mkdirs();
        }
        if (shematicsDirectory.list().length != 0) {
            log(NamedTextColor.GOLD, "Extracting samples schematics...");
            extractSamples(shematicsDirectory);
            log(NamedTextColor.GOLD, "Done !");
        }

        /* TESTING SCHEMATICS : */
        log(NamedTextColor.GOLD, "Testing schematics...");
        final String[] invalidSchematics = operations.testSchematics();
        if (invalidSchematics.length != 0) {
            log(NamedTextColor.RED, "There are some invalid schematics :");
            for (final String invalidSchematic : invalidSchematics) {
                log(NamedTextColor.RED, invalidSchematic);
                pluginConfig.saplingOakSchematics.remove(invalidSchematic);
                pluginConfig.saplingSpruceSchematics.remove(invalidSchematic);
                pluginConfig.saplingBirchSchematics.remove(invalidSchematic);
                pluginConfig.saplingJungleSchematics.remove(invalidSchematic);
                pluginConfig.saplingAcaciaSchematics.remove(invalidSchematic);
                pluginConfig.saplingDarkOakSchematics.remove(invalidSchematic);
                pluginConfig.mushroomBrownSchematics.remove(invalidSchematic);
                pluginConfig.mushroomRedSchematics.remove(invalidSchematic);
            }
            log(NamedTextColor.RED, "They are not going to be used by the plugin. Please fix them and restart your server.");
        } else {
            log(NamedTextColor.GOLD, "Done, no error.");
        }

        /* REGISTERING EVENTS : */
        Bukkit.getPluginManager().registerEvents(new GlobalEventsListener(this), this);

        /* REGISTERING COMMANDS : */
        getCommand("owngarden").setExecutor(new OwnGardenCommand(this));
        log(null, "§7Enabled §a" + getName() + " v" + getPluginMeta().getVersion()
            + " §7by §6" + Joiner.on(' ').join(getPluginMeta().getAuthors()) + " §7!");
    }

    /**
     * Extracts the samples to the specified directory.
     *
     * @param schematicsDirectory The schematics directory.
     */
    private void extractSamples(final File schematicsDirectory) {
        ZipUtil.unpack(getFile(), schematicsDirectory, name -> {
            return name.startsWith("schematics/") && name.length() > "schematics/".length()
                ? name.substring("schematics/".length()) : null;
        });
    }

    /**
     * Logs a message to the console.
     *
     * @param color The color (after the [plugin-name]).
     * @param message The message.
     */
    public void log(final NamedTextColor color, final String message) {
        Bukkit.getConsoleSender().sendMessage(Component.text("[" + getName() + "]").append(Component.text(message, color)));
    }
}